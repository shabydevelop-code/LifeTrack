package com.shabydevelop.eattrack

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.*

@Composable
internal fun OnPlatformResume(activity: ComponentActivity, resume: () -> Unit) {
    val current by rememberUpdatedState(resume)
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) current()
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }
}

internal class PlatformLifecycle(private val activity: ComponentActivity) {
    val isChangingConfiguration: Boolean get() = activity.isChangingConfigurations
    val isStarted: Boolean get() = activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    fun observeStop(stop: () -> Unit): () -> Unit {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && !isChangingConfiguration) stop()
        }
        activity.lifecycle.addObserver(observer)
        return { activity.lifecycle.removeObserver(observer) }
    }
}
