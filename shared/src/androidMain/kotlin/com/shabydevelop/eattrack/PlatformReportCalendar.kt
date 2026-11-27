package com.shabydevelop.eattrack

import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

object PlatformReportCalendar {
    fun snapshot(): ReportPeriods {
        val now = Calendar.getInstance()
        val today = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY,0); set(Calendar.MINUTE,0); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0)
        }
        fun shifted(base: Calendar, days: Int) = (base.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, days) }
        val week = shifted(today, -(today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY))
        val month = (today.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH,1) }
        val monthEnd = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH,getActualMaximum(Calendar.DAY_OF_MONTH)) }
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val short = SimpleDateFormat("dd/MM", Locale.getDefault())
        return ReportPeriods(now.timeInMillis,today.timeInMillis,week.timeInMillis,month.timeInMillis,
            format.format(today.time),format.format(week.time)+" – "+format.format(shifted(week,6).time),
            format.format(month.time)+" – "+format.format(monthEnd.time),
            (0..6).map { offset -> val start = shifted(today,-offset); ReportDay(start.timeInMillis,shifted(start,1).timeInMillis,short.format(start.time)) })
    }
}
