package com.lyc.sstcontrol.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ReportRepository {

    private val db = FirebaseFirestore.getInstance()

    fun submit(
        inspectionId: String,
        responses: List<ItemResponse>,
        photos: Map<String, String>,
        generalNotes: String,
        gpsStart: GpsPoint?,
        gpsEnd: GpsPoint?
    ) {
        val status = if (responses.any { it.status == ItemStatus.BAD }) "RISK" else "APPROVED"
        val inspectionRef = db.collection("inspections").document(inspectionId)
        val batch = db.batch()

        batch.update(
            inspectionRef,
            mapOf(
                "status" to status,
                "responses" to responses.map {
                    mapOf(
                        "itemId" to it.itemId,
                        "title" to it.title,
                        "status" to it.status.name,
                        "note" to it.note,
                        "hasPhoto" to it.hasPhoto
                    )
                },
                "generalNotes" to generalNotes,
                "submittedAt" to FieldValue.serverTimestamp(),
                "gpsStart" to gpsStart?.toMap(),
                "gpsEnd" to gpsEnd?.toMap()
            )
        )
        photos.forEach { (itemId, data) ->
            batch.set(
                inspectionRef.collection("photos").document(itemId),
                mapOf("itemId" to itemId, "data" to data)
            )
        }
        batch.commit()
    }

    fun loadDetail(inspectionId: String, onResult: (Result<InspectionDetail>) -> Unit) {
        val ref = db.collection("inspections").document(inspectionId)
        ref.get()
            .addOnSuccessListener { doc ->
                ref.collection("photos").get()
                    .addOnSuccessListener { photoSnapshot ->
                        val photos = photoSnapshot.documents.associate { it.id to it.getString("data") }
                        val raw = doc.get("responses") as? List<*> ?: emptyList<Any>()
                        val responses = raw.mapNotNull { item ->
                            val map = item as? Map<*, *> ?: return@mapNotNull null
                            val itemId = map["itemId"] as? String ?: return@mapNotNull null
                            DetailResponse(
                                itemId = itemId,
                                title = map["title"] as? String ?: itemId,
                                status = ItemStatus.entries.firstOrNull { it.name == map["status"] }
                                    ?: ItemStatus.GOOD,
                                note = map["note"] as? String ?: "",
                                photo = photos[itemId]
                            )
                        }
                        onResult(
                            Result.success(
                                InspectionDetail(
                                    responses = responses,
                                    generalNotes = doc.getString("generalNotes").orEmpty(),
                                    gpsStart = (doc.get("gpsStart") as? Map<*, *>)?.toGpsPoint(),
                                    gpsEnd = (doc.get("gpsEnd") as? Map<*, *>)?.toGpsPoint(),
                                    submittedAt = doc.getTimestamp("submittedAt")
                                )
                            )
                        )
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun listenByInspector(
        inspectorId: String,
        onChange: (List<Inspection>) -> Unit
    ): ListenerRegistration =
        db.collection("inspections")
            .whereEqualTo("inspectorId", inspectorId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    onChange(
                        snapshot.documents
                            .mapNotNull { it.toInspection() }
                            .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                    )
                }
            }
}