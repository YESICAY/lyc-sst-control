package com.lyc.sstcontrol.ui.inspector

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lyc.sstcontrol.data.ChecklistSection
import com.lyc.sstcontrol.data.Checklists
import com.lyc.sstcontrol.data.GpsPoint
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.ItemResponse
import com.lyc.sstcontrol.data.ItemStatus
import com.lyc.sstcontrol.data.ReportRepository
import java.io.File

data class ItemDraft(
    val status: ItemStatus? = null,
    val note: String = "",
    val photo: String? = null
)

class InspectionFormViewModel : ViewModel() {

    private val repository = ReportRepository()
    private var draftOwnerId: String? = null

    var inspection by mutableStateOf<Inspection?>(null)
        private set
    var sections by mutableStateOf<List<ChecklistSection>>(emptyList())
        private set
    val drafts = mutableStateMapOf<String, ItemDraft>()
    var step by mutableStateOf(0)
        private set
    var generalNotes by mutableStateOf("")
        private set
    var gpsStart by mutableStateOf<GpsPoint?>(null)
        private set
    var gpsPending by mutableStateOf(true)
        private set
    var showIncompleteError by mutableStateOf(false)
        private set
    var photoError by mutableStateOf(false)
        private set
    var isSubmitting by mutableStateOf(false)
        private set
    var finished by mutableStateOf(false)
        private set

    var pendingPhotoItemId: String? = null
    var pendingPhotoFile: File? = null

    val totalSteps: Int
        get() = sections.size + 1

    val progressPercent: Int
        get() {
            val total = sections.sumOf { it.items.size }
            if (total == 0) return 0
            val answered = drafts.values.count { it.status != null }
            return answered * 100 / total
        }

    fun start(task: Inspection) {
        if (draftOwnerId != task.id) {
            drafts.clear()
            generalNotes = ""
            gpsStart = null
            gpsPending = true
            step = 0
            draftOwnerId = task.id
        }
        sections = Checklists.forType(task.type)
        showIncompleteError = false
        photoError = false
        isSubmitting = false
        finished = false
        inspection = task
    }

    fun onBack() {
        if (step > 0) {
            step -= 1
            showIncompleteError = false
        } else {
            inspection = null
        }
    }

    fun onStatusSelected(itemId: String, status: ItemStatus) {
        drafts[itemId] = (drafts[itemId] ?: ItemDraft()).copy(status = status)
        showIncompleteError = false
    }

    fun onNoteChange(itemId: String, note: String) {
        drafts[itemId] = (drafts[itemId] ?: ItemDraft()).copy(note = note)
        showIncompleteError = false
    }

    fun onPhotoTaken(itemId: String, base64: String) {
        drafts[itemId] = (drafts[itemId] ?: ItemDraft()).copy(photo = base64)
        photoError = false
        showIncompleteError = false
    }

    fun onPhotoFailed() {
        photoError = true
    }

    fun onGeneralNotesChange(value: String) {
        generalNotes = value
    }

    fun onGpsStart(point: GpsPoint?) {
        gpsStart = point
        gpsPending = false
    }

    private fun isStepComplete(index: Int): Boolean =
        sections[index].items.all { item ->
            val draft = drafts[item.id]
            draft != null && draft.status != null &&
                    (draft.status == ItemStatus.GOOD || (draft.note.isNotBlank() && draft.photo != null))
        }

    fun onNext() {
        if (step < sections.size) {
            if (isStepComplete(step)) {
                step += 1
                showIncompleteError = false
            } else {
                showIncompleteError = true
            }
        }
    }

    fun counts(): Triple<Int, Int, Int> {
        val values = drafts.values
        return Triple(
            values.count { it.status == ItemStatus.GOOD },
            values.count { it.status == ItemStatus.REGULAR },
            values.count { it.status == ItemStatus.BAD }
        )
    }

    fun beginSubmit() {
        isSubmitting = true
    }

    fun submit(gpsEnd: GpsPoint?) {
        val current = inspection ?: return
        val responses = sections.flatMap { it.items }.map { item ->
            val draft = drafts[item.id] ?: ItemDraft()
            ItemResponse(
                itemId = item.id,
                title = item.title,
                status = draft.status ?: ItemStatus.GOOD,
                note = draft.note.trim(),
                hasPhoto = draft.photo != null
            )
        }
        val photos = HashMap<String, String>()
        drafts.forEach { (id, draft) -> draft.photo?.let { photos[id] = it } }

        repository.submit(current.id, responses, photos, generalNotes.trim(), gpsStart, gpsEnd)
        isSubmitting = false
        finished = true
    }

    fun finish() {
        drafts.clear()
        draftOwnerId = null
        generalNotes = ""
        gpsStart = null
        gpsPending = true
        step = 0
        finished = false
        inspection = null
    }
}