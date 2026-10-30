package com.lyc.sstcontrol.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lyc.sstcontrol.data.InspectionDetail
import com.lyc.sstcontrol.data.ReportRepository

class DetailViewModel : ViewModel() {

    private val repository = ReportRepository()
    private var loadedId: String? = null

    var detail by mutableStateOf<InspectionDetail?>(null)
        private set
    var failed by mutableStateOf(false)
        private set

    fun reset() {
        loadedId = null
        detail = null
        failed = false
    }

    fun load(id: String) {
        loadedId = id
        detail = null
        failed = false
        repository.loadDetail(id) { result ->
            if (loadedId != id) return@loadDetail
            result
                .onSuccess { detail = it }
                .onFailure { failed = true }
        }
    }
}