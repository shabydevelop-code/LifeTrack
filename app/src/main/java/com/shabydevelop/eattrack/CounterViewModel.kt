package com.shabydevelop.eattrack

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CounterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EatingRepository(application)
    private val mutex = Mutex()
    var report by mutableStateOf(EatingReport())
        private set
    val count: Long get() = report.total + report.healthyTotal
    var error by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var feedback by mutableStateOf("")
        private set
    var successSequence by mutableLongStateOf(0L)
        private set
    var pendingReasonId by mutableStateOf<Long?>(null)
        private set
    var reasonError by mutableStateOf(false)
        private set
    fun dismissReasons() { if (!busy) { pendingReasonId = null; reasonError = false } }
    fun saveReasons(reasons: Set<EatingReason>) {
        val id = pendingReasonId ?: return
        if (busy) return
        busy = true
        reasonError = false
        viewModelScope.launch {
            try {
                mutex.withLock { withContext(Dispatchers.IO) { repository.saveReasons(id, reasons) } }
                pendingReasonId = null
                feedback = "הסיבות נשמרו"
                successSequence++
            } catch (e: android.database.SQLException) { reasonError = true }
            finally { busy = false }
        }
    }
    private var lastTap: Long? = null

    init { reload() }
    fun reload() = refresh()
    fun increment(healthy: Boolean) {
        val now = SystemClock.elapsedRealtime()
        if (busy || lastTap?.let { now - it < 700L } == true) return
        lastTap = now
        mutate {
            val id = repository.addEvent(healthy)
            if (!healthy) withContext(Dispatchers.Main) { pendingReasonId = id; reasonError = false }
            if (healthy) "דיווח אכילה בריאה נשמר" else "דיווח אכילה לא בריאה נשמר"
        }
    }
    fun undo() = mutate {
        if (repository.undoLast()) "הדיווח האחרון בוטל" else "אין דיווח לביטול"
    }
    fun reset() = mutate { repository.reset(); "ההיסטוריה נמחקה" }

    suspend fun historyPage(days: Int?, healthy: Boolean?, before: EatingEvent?): HistoryPage =
        withContext(Dispatchers.IO) { repository.historyPage(days, healthy, before) }

    private fun mutate(action: suspend () -> String) {
        if (busy) return
        busy = true
        feedback = ""
        viewModelScope.launch {
            try {
                mutex.withLock {
                    val message = withContext(Dispatchers.IO) { action() }
                    feedback = message
                    successSequence++
                    report = withContext(Dispatchers.IO) { repository.report() }
                    error = false
                }
            } catch (e: android.database.SQLException) {
                error = true
            } finally {
                busy = false
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    report = withContext(Dispatchers.IO) { repository.report() }
                    error = false
                } catch (e: android.database.SQLException) {
                    error = true
                }
            }
        }
    }
}
