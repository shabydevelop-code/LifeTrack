package com.shabydevelop.eattrack

object PlatformClock : AppClock {
    override fun nowMillis() = System.currentTimeMillis()
    override fun monotonicMillis() = android.os.SystemClock.elapsedRealtime()
}
