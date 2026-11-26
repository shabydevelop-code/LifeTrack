package com.shabydevelop.eattrack

import android.content.Context
import android.widget.Toast

/** Main-thread feedback: replace the previous message rather than queueing it. */
internal class ToastManager(context: Context) {
    private val appContext = context.applicationContext
    private var current: Toast? = null

    fun show(message: String) {
        cancel()
        current = Toast.makeText(appContext, message, Toast.LENGTH_SHORT).also { it.show() }
    }

    fun cancel() {
        current?.cancel()
        current = null
    }
}
