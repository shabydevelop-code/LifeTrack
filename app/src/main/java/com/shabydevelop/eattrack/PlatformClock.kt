package com.shabydevelop.eattrack

internal interface AppClock {
    fun nowMillis(): Long
    fun monotonicMillis(): Long
}

internal object PlatformClock : AppClock {
    override fun nowMillis() = System.currentTimeMillis()
    override fun monotonicMillis() = android.os.SystemClock.elapsedRealtime()
}
