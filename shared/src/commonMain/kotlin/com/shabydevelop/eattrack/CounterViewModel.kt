package com.shabydevelop.eattrack

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CounterViewModel(private val repository: EatingDataSource, private val clock: AppClock, private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher) : ViewModel() {
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
                mutex.withLock { withContext(ioDispatcher) { repository.saveReasons(id, reasons, pendingReasonType) } }
                feedbackType = pendingReasonType
                closeReasons()
                feedback = "הסיבות נשמרו"
                successSequence++
            } catch (e: RepositoryFailure) { reasonError = true }
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
        withContext(Dispatchers.Main) { celebrate(EventType.WATER) }
        "דיווח שתיית מים נשמר"
    }
    fun addFood(kind: FoodKind, percent: Int) = mutate(if (percent == 100) EventType.HEALTHY else EventType.UNHEALTHY) {
        val id = repository.addFood(kind, percent)
        withContext(Dispatchers.Main) {
            if (percent == 100) celebrate(EventType.HEALTHY)
            else { pendingReasonType = EventType.UNHEALTHY; pendingReasonId = id; reasonError = false }
        }
        "דיווח ${kind.label} נשמר"
    }
    fun increment(type: EventType) {
        if (type == EventType.AEROBIC || type == EventType.WATER) return
        val now = clock.monotonicMillis()
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
                    withContext(ioDispatcher) { repository.updateEvent(event) }
                    feedbackType = event.type
                    feedback = "הדיווח עודכן"
                    successSequence++
                    try {
                        report = withContext(ioDispatcher) { repository.report() }
                        error = false
                    } catch (e: RepositoryFailure) { error = true }
                }
                completed(true)
            } catch (e: RepositoryFailure) { completed(false) }
            finally { busy = false }
        }
    }
    fun deleteEvent(id: Long) = mutate {
        if (!repository.deleteEvent(id)) throw RepositoryFailure("Report not found")
        "הדיווח נמחק"
    }
    fun undo() = mutate {
        if (repository.undoLast()) "הדיווח האחרון בוטל" else "אין דיווח לביטול"
    }
    fun reset() = mutate { repository.reset(); "ההיסטוריה נמחקה" }

    suspend fun historyPage(days: Int?, type: EventType?, before: EatingEvent?): HistoryPage =
        withContext(ioDispatcher) { repository.historyPage(days, type, before) }

    suspend fun foodHistoryPage(days: Int?, kind: FoodKind?, before: EatingEvent?): HistoryPage =
        withContext(ioDispatcher) { repository.foodHistoryPage(days, kind, before) }

    private fun mutate(type: EventType? = null, action: suspend () -> String) {
        if (busy) return
        busy = true
        feedback = ""
        viewModelScope.launch {
            try {
                mutex.withLock {
                    val message = withContext(ioDispatcher) { action() }
                    feedbackType = type
                    feedback = message
                    successSequence++
                    report = withContext(ioDispatcher) { repository.report() }
                    error = false
                }
            } catch (e: RepositoryFailure) {
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
                    report = withContext(ioDispatcher) { repository.report() }
                    error = false
                } catch (e: RepositoryFailure) {
                    error = true
                }
            }
        }
    }
}
