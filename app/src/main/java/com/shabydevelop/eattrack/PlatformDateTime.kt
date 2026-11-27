package com.shabydevelop.eattrack

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal object PlatformDateTime {
    fun date(timestamp: Long) = format(timestamp, "dd/MM/yyyy")
    fun time(timestamp: Long) = format(timestamp, "HH:mm")
    fun dateTime(timestamp: Long) = format(timestamp, "dd/MM/yyyy HH:mm")
    fun format(timestamp: Long, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    fun startOfDays(days: Int): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1 - days)
    }.timeInMillis
}
