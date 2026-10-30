package com.lyc.sstcontrol.ui.inspector

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.InspectionRepository

class InboxViewModel : ViewModel() {

    private val repository = InspectionRepository()
    private var listener: ListenerRegistration? = null

    var tasks by mutableStateOf<List<Inspection>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    fun start(inspectorId: String) {
        listener?.remove()
        isLoading = true
        listener = repository.listenPendingFor(inspectorId) {
            tasks = it
            isLoading = false
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
    }

    override fun onCleared() {
        stop()
    }
}