package com.shabydevelop.androiddemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CounterViewModel(private val state: SavedStateHandle) : ViewModel() {
    var count by mutableIntStateOf(state.get<Int>("count") ?: 0)
        private set

    fun increment() = update(count + 1)
    fun reset() = update(0)

    private fun update(value: Int) {
        count = value
        state["count"] = value
    }
}
