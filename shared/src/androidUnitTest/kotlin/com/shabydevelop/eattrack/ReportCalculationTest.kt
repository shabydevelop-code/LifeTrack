package com.shabydevelop.eattrack

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class ReportCalculationTest {
    @Test fun reportUsesPeriodBoundariesAndMetricTypes() {
        val p = ReportPeriods(99,80,50,10,"today","week","month",listOf(ReportDay(80,100,"day")))
        val report = buildEatingReport(p, { start,end,type -> (end-start) * (type.ordinal+1) },
            { 100L }, null, { "" }, { start,end -> ActivityStats(1,end-start) }, { start,end -> WaterStats(1,end-start) })
        assertEquals(20L,report.today)
        assertEquals(40L,report.healthyToday)
        assertEquals(60L,report.avoidedToday)
        assertEquals(20L,report.activity.today.minutes)
        assertEquals(100L,report.water.total.milliliters)
        assertEquals(20L,report.days.single().count)
        assertEquals("אין דיווחים",report.totalLabel)
        assertEquals(3,report.periodRows(false).size)
    }
    @Test fun reasonOptionsSeparateAvoidance() {
        assertTrue(EatingReason.GOAL in reasonOptions(EventType.AVOIDED))
        assertTrue(EatingReason.GOAL !in reasonOptions(EventType.UNHEALTHY))
    }
}
