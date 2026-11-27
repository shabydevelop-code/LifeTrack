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
    val count: Long get() = report.total + report.healthyTotal + report.avoidedTotal + report.activity.total.count + report.water.total.count
    var error by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var feedback by mutableStateOf("")
        private set
    var feedbackType by mutableStateOf<EventType?>(null)
        private set
    var failureSequence by mutableLongStateOf(0L)
        private set
    var successSequence by mutableLongStateOf(0L)
        private set
    var pendingReasonId by mutableStateOf<Long?>(null)
        private set
    var pendingReasonType by mutableStateOf(EventType.UNHEALTHY)
        private set
    var reasonError by mutableStateOf(false)
        private set
    var celebrationSequence by mutableLongStateOf(0L)
        private set
    var celebrationType by mutableStateOf(EventType.AVOIDED)
        private set
    private fun celebrate(type: EventType) {
        celebrationType = type
        celebrationSequence++
    }
    private fun closeReasons() {
        if (pendingReasonId != null && pendingReasonType == EventType.AVOIDED) celebrate(EventType.AVOIDED)
        pendingReasonId = null
        reasonError = false
    }
    fun dismissReasons() { if (!busy) closeReasons() }
    fun saveReasons(reasons: Set<EatingReason>) {
        val id = pendingReasonId ?: return
        if (busy) return
        busy = true
        reasonError = false
        viewModelScope.launch {
            try {
                mutex.withLock { withContext(Dispatchers.IO) { repository.saveReasons(id, reasons, pendingReasonType) } }
                feedbackType = pendingReasonType
                closeReasons()
                feedback = "הסיבות נשמרו"
                successSequence++
            } catch (e: android.database.SQLException) { reasonError = true }
            finally { busy = false }
        }
    }
    private var lastTap: Long? = null

    init { reload() }
    fun reload() = refresh()
    fun addActivity(kind: ActivityKind, minutes: Int) = mutate(EventType.AEROBIC) {
        repository.addEvent(EventType.AEROBIC, kind, minutes)
        withContext(Dispatchers.Main) { celebrate(EventType.AEROBIC) }
        "הפעילות נשמרה"
    }
    fun addWater(milliliters: Int) = mutate(EventType.WATER) {
        repository.addEvent(EventType.WATER, milliliters = milliliters)
        "דיווח שתיית מים נשמר"
    }
    fun increment(type: EventType) {
        if (type == EventType.AEROBIC || type == EventType.WATER) return
        val now = SystemClock.elapsedRealtime()
        if (busy || lastTap?.let { now - it < 700L } == true) return
        lastTap = now
        mutate(type) {
            val id = repository.addEvent(type)
            withContext(Dispatchers.Main) {
                if (type == EventType.HEALTHY) celebrate(type)
                else { pendingReasonType = type; pendingReasonId = id; reasonError = false }
            }
            "דיווח ${type.label} נשמר"
        }
    }
    fun updateEvent(event: EatingEvent, completed: (Boolean) -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                mutex.withLock {
                    withContext(Dispatchers.IO) { repository.updateEvent(event) }
                    feedbackType = event.type
                    feedback = "הדיווח עודכן"
                    successSequence++
                    try {
                        report = withContext(Dispatchers.IO) { repository.report() }
                        error = false
                    } catch (e: android.database.SQLException) { error = true }
                }
                completed(true)
            } catch (e: android.database.SQLException) { completed(false) }
            finally { busy = false }
        }
    }
    fun deleteEvent(id: Long) = mutate {
        if (!repository.deleteEvent(id)) throw android.database.SQLException("Report not found")
        "הדיווח נמחק"
    }
    fun undo() = mutate {
        if (repository.undoLast()) "הדיווח האחרון בוטל" else "אין דיווח לביטול"
    }
    fun reset() = mutate { repository.reset(); "ההיסטוריה נמחקה" }

    suspend fun historyPage(days: Int?, type: EventType?, before: EatingEvent?): HistoryPage =
        withContext(Dispatchers.IO) { repository.historyPage(days, type, before) }

    private fun mutate(type: EventType? = null, action: suspend () -> String) {
        if (busy) return
        busy = true
        feedback = ""
        viewModelScope.launch {
            try {
                mutex.withLock {
                    val message = withContext(Dispatchers.IO) { action() }
                    feedbackType = type
                    feedback = message
                    successSequence++
                    report = withContext(Dispatchers.IO) { repository.report() }
                    error = false
                }
            } catch (e: android.database.SQLException) {
                error = true
                failureSequence++
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
