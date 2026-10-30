package com.lyc.sstcontrol.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.lyc.sstcontrol.data.AssignError
import com.lyc.sstcontrol.data.AssignException
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.InspectionRepository
import com.lyc.sstcontrol.data.InspectionType
import com.lyc.sstcontrol.data.UserProfile
import java.time.LocalDate
import java.time.ZoneId

class AssignViewModel : ViewModel() {

    private val repository = InspectionRepository()
    private var recentListener: ListenerRegistration? = null

    var inspectors by mutableStateOf<List<UserProfile>>(emptyList())
        private set
    var inspectorsLoading by mutableStateOf(true)
        private set
    var recent by mutableStateOf<List<Inspection>>(emptyList())
        private set

    var selectedInspector by mutableStateOf<UserProfile?>(null)
        private set
    var selectedType by mutableStateOf<InspectionType?>(null)
        private set
    var location by mutableStateOf("")
        private set
    var dueDate by mutableStateOf<LocalDate?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<AssignError?>(null)
        private set
    var justAssigned by mutableStateOf(false)
        private set

    fun startListening() {
        inspectorsLoading = true
        repository.loadActiveInspectors { result ->
            inspectorsLoading = false
            result
                .onSuccess { inspectors = it }
                .onFailure { error = (it as? AssignException)?.error ?: AssignError.UNKNOWN }
        }
        recentListener?.remove()
        recentListener = repository.listenRecent(5) { recent = it }
    }

    fun stopListening() {
        recentListener?.remove()
        recentListener = null
    }

    override fun onCleared() {
        stopListening()
    }

    fun onInspectorSelected(value: UserProfile) {
        selectedInspector = value
        clearMessages()
    }

    fun onTypeSelected(value: InspectionType) {
        selectedType = value
        clearMessages()
    }

    fun onLocationChange(value: String) {
        location = value
        clearMessages()
    }

    fun onDueDateSelected(value: LocalDate) {
        dueDate = value
        clearMessages()
    }

    private fun clearMessages() {
        error = null
        justAssigned = false
    }

    fun onAssignClick(adminUid: String) {
        if (isLoading) return
        val inspector = selectedInspector
        val type = selectedType
        val date = dueDate
        val place = location.trim()
        if (inspector == null || type == null || date == null || place.isEmpty()) {
            error = AssignError.INCOMPLETE
            return
        }

        isLoading = true
        clearMessages()
        val endOfDay = date.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant()
        val dueTimestamp = Timestamp(endOfDay.epochSecond, 0)

        repository.assign(inspector, type, place, dueTimestamp, adminUid) { result ->
            isLoading = false
            result
                .onSuccess {
                    justAssigned = true
                    selectedInspector = null
                    selectedType = null
                    location = ""
                    dueDate = null
                }
                .onFailure { error = (it as? AssignException)?.error ?: AssignError.UNKNOWN }
        }
    }
}