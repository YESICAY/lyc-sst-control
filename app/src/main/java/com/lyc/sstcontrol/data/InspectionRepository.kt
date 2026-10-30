package com.lyc.sstcontrol.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

enum class AssignError { INCOMPLETE, NO_NETWORK, NOT_ALLOWED, UNKNOWN }

class AssignException(val error: AssignError) : Exception()

class InspectionRepository {

    private val db = FirebaseFirestore.getInstance()

    fun loadActiveInspectors(onResult: (Result<List<UserProfile>>) -> Unit) {
        db.collection("users")
            .whereEqualTo("role", "inspector")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val inspectors = snapshot.documents
                    .map { doc ->
                        UserProfile(
                            uid = doc.id,
                            fullName = doc.getString("fullName").orEmpty(),
                            email = doc.getString("email").orEmpty(),
                            role = UserRole.INSPECTOR,
                            active = true
                        )
                    }
                    .sortedBy { it.fullName.lowercase() }
                onResult(Result.success(inspectors))
            }
            .addOnFailureListener { onResult(Result.failure(it.toAssignException())) }
    }

    fun assign(
        inspector: UserProfile,
        type: InspectionType,
        location: String,
        dueDate: Timestamp,
        assignedBy: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val data = mapOf(
            "type" to type.name,
            "location" to location,
            "dueDate" to dueDate,
            "inspectorId" to inspector.uid,
            "inspectorName" to inspector.fullName,
            "assignedBy" to assignedBy,
            "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("inspections").add(data)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it.toAssignException())) }
    }

    fun listenRecent(limit: Long, onChange: (List<Inspection>) -> Unit): ListenerRegistration =
        db.collection("inspections")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    onChange(snapshot.documents.mapNotNull { it.toInspection() })
                }
            }

    fun listenPendingFor(
        inspectorId: String,
        onChange: (List<Inspection>) -> Unit
    ): ListenerRegistration =
        db.collection("inspections")
            .whereEqualTo("inspectorId", inspectorId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    onChange(
                        snapshot.documents
                            .mapNotNull { it.toInspection() }
                            .sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }
                    )
                }
            }

    private fun Throwable.toAssignException(): AssignException = when (this) {
        is AssignException -> this
        is FirebaseNetworkException -> AssignException(AssignError.NO_NETWORK)
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                AssignException(AssignError.NOT_ALLOWED)
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                AssignException(AssignError.NO_NETWORK)
            else -> AssignException(AssignError.UNKNOWN)
        }
        else -> AssignException(AssignError.UNKNOWN)
    }
}