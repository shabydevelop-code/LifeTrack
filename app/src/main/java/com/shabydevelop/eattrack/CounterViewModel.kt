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
    val count: Long get() = report.total
    var error by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var feedback by mutableStateOf("")
        private set
    var successSequence by mutableLongStateOf(0L)
        private set
    private var lastTap: Long? = null

    init { reload() }
    fun reload() = refresh()
    fun increment() {
        val now = SystemClock.elapsedRealtime()
        if (busy || lastTap?.let { now - it < 700L } == true) return
        lastTap = now
        mutate { repository.addEvent(); "הדיווח נשמר" }
    }
    fun undo() = mutate {
        if (repository.undoLast()) "הדיווח האחרון בוטל" else "אין דיווח לביטול"
    }
    fun reset() = mutate { repository.reset(); "ההיסטוריה נמחקה" }

    private fun mutate(action: () -> String) {
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
