package com.lyc.sstcontrol.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

enum class InspectionType { EPP, HEIGHTS, ELECTRICAL }

data class Inspection(
    val id: String,
    val type: InspectionType,
    val location: String,
    val dueDate: Timestamp?,
    val inspectorId: String,
    val inspectorName: String,
    val status: String,
    val createdAt: Timestamp?
)

fun DocumentSnapshot.toInspection(): Inspection? {
    val type = InspectionType.entries.firstOrNull { it.name == getString("type") } ?: return null
    return Inspection(
        id = id,
        type = type,
        location = getString("location").orEmpty(),
        dueDate = getTimestamp("dueDate"),
        inspectorId = getString("inspectorId").orEmpty(),
        inspectorName = getString("inspectorName").orEmpty(),
        status = getString("status").orEmpty(),
        createdAt = getTimestamp("createdAt")
    )
}