package com.lyc.sstcontrol.data

import com.google.firebase.Timestamp

enum class ItemStatus { GOOD, REGULAR, BAD }

data class ChecklistItem(val id: String, val title: String)

data class ChecklistSection(val title: String, val items: List<ChecklistItem>)

data class GpsPoint(val latitude: Double, val longitude: Double, val accuracy: Double) {
    fun toMap(): Map<String, Any> = mapOf(
        "lat" to latitude,
        "lng" to longitude,
        "accuracy" to accuracy
    )
}

fun Map<*, *>.toGpsPoint(): GpsPoint? {
    val lat = (this["lat"] as? Number)?.toDouble() ?: return null
    val lng = (this["lng"] as? Number)?.toDouble() ?: return null
    val accuracy = (this["accuracy"] as? Number)?.toDouble() ?: 0.0
    return GpsPoint(lat, lng, accuracy)
}

data class ItemResponse(
    val itemId: String,
    val title: String,
    val status: ItemStatus,
    val note: String,
    val hasPhoto: Boolean
)

data class DetailResponse(
    val itemId: String,
    val title: String,
    val status: ItemStatus,
    val note: String,
    val photo: String?
)

data class InspectionDetail(
    val responses: List<DetailResponse>,
    val generalNotes: String,
    val gpsStart: GpsPoint?,
    val gpsEnd: GpsPoint?,
    val submittedAt: Timestamp?
)

object Checklists {
    fun forType(type: InspectionType): List<ChecklistSection> = when (type) {
        InspectionType.EPP -> listOf(
            ChecklistSection(
                "Equipo de protección individual",
                listOf(
                    ChecklistItem("helmet", "Casco de seguridad"),
                    ChecklistItem("gloves", "Guantes de protección"),
                    ChecklistItem("glasses", "Gafas de seguridad"),
                    ChecklistItem("boots", "Calzado de seguridad")
                )
            ),
            ChecklistSection(
                "Condiciones del área",
                listOf(
                    ChecklistItem("order", "Orden y limpieza"),
                    ChecklistItem("signage", "Señalización de seguridad"),
                    ChecklistItem("extinguisher", "Extintor disponible y vigente")
                )
            )
        )

        InspectionType.HEIGHTS -> listOf(
            ChecklistSection(
                "Equipos de protección contra caídas",
                listOf(
                    ChecklistItem("harness", "Arnés de cuerpo completo"),
                    ChecklistItem("lifeline", "Línea de vida"),
                    ChecklistItem("anchors", "Puntos de anclaje"),
                    ChecklistItem("helmet", "Casco con barbuquejo")
                )
            ),
            ChecklistSection(
                "Condiciones del lugar de trabajo",
                listOf(
                    ChecklistItem("scaffold", "Andamios y escaleras en buen estado"),
                    ChecklistItem("guardrails", "Barandas y protecciones de borde"),
                    ChecklistItem("demarcation", "Área demarcada y señalizada")
                )
            )
        )

        InspectionType.ELECTRICAL -> listOf(
            ChecklistSection(
                "Elementos de protección y herramientas",
                listOf(
                    ChecklistItem("gloves", "Guantes dieléctricos"),
                    ChecklistItem("boots", "Calzado dieléctrico"),
                    ChecklistItem("tools", "Herramientas aisladas"),
                    ChecklistItem("detector", "Detector de tensión")
                )
            ),
            ChecklistSection(
                "Condiciones de la instalación",
                listOf(
                    ChecklistItem("panels", "Tableros cerrados y señalizados"),
                    ChecklistItem("wiring", "Cableado sin daños visibles"),
                    ChecklistItem("loto", "Bloqueo y etiquetado (LOTO)")
                )
            )
        )
    }
}