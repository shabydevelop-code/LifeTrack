package com.shabydevelop.eattrack

interface AppClock {
    fun nowMillis(): Long
    fun monotonicMillis(): Long
}
