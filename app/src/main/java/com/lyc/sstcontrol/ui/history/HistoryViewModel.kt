package com.lyc.sstcontrol.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.lyc.sstcontrol.data.Inspection
import com.lyc.sstcontrol.data.InspectionRepository
import com.lyc.sstcontrol.data.ReportRepository

class HistoryViewModel : ViewModel() {

    private val inspections = InspectionRepository()
    private val reports = ReportRepository()
    private var listener: ListenerRegistration? = null

    var items by mutableStateOf<List<Inspection>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var query by mutableStateOf("")
        private set
    var statusFilter by mutableStateOf<String?>(null)
        private set

    fun start(isAdmin: Boolean, uid: String) {
        listener?.remove()
        isLoading = true
        val onChange: (List<Inspection>) -> Unit = {
            items = it
            isLoading = false
        }
        listener = if (isAdmin) {
            inspections.listenRecent(100, onChange)
        } else {
            reports.listenByInspector(uid, onChange)
        }
    }

    fun stop() {
        listener?.remove()
        listener = null
    }

    override fun onCleared() {
        stop()
    }

    fun onQueryChange(value: String) {
        query = value
    }

    fun onFilterSelected(value: String?) {
        statusFilter = value
    }
}