package com.shabydevelop.androiddemo

import android.app.Application
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
    var count by mutableLongStateOf(0L)
        private set
    var error by mutableStateOf(false)
        private set

    init { refresh() }
    fun increment() = refresh { repository.addEvent() }
    fun reset() = refresh { repository.reset() }

    private fun refresh(action: () -> Unit = {}) {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    count = withContext(Dispatchers.IO) {
                        action()
                        repository.count()
                    }
                    error = false
                } catch (e: android.database.SQLException) {
                    error = true
                }
            }
        }
    }
}
