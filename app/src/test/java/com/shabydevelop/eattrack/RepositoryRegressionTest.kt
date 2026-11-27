package com.shabydevelop.eattrack

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RepositoryRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun clean() { context.deleteDatabase("eattrack.db") }
    @After fun cleanup() { context.deleteDatabase("eattrack.db") }

    @Test fun reportsEditsAndPagination() {
        val repo = EatingRepository(context)
        val ids = (1..35).map { repo.addEvent(EventType.HEALTHY) }
        val water = repo.addEvent(EventType.WATER, milliliters = 250)
        repo.addEvent(EventType.AEROBIC, ActivityKind.WALK, 30)
        val first = repo.historyPage(null, EventType.HEALTHY, null)
        assertEquals(30, first.events.size)
        assertTrue(first.hasMore)
        val next = repo.historyPage(null, EventType.HEALTHY, first.events.last())
        assertEquals(5, next.events.size)
        assertFalse(next.hasMore)
        assertEquals(ids, (first.events + next.events).map { it.id })
        val report = repo.report()
        assertEquals(35L, report.healthyToday)
        assertEquals(250L, report.water.today.milliliters)
        assertEquals(30L, report.activity.today.minutes)
        val event = repo.historyPage(null, EventType.WATER, null).events.single()
        repo.updateEvent(event.copy(milliliters = 500))
        assertEquals(500L, repo.report().water.today.milliliters)
        assertTrue(repo.deleteEvent(water))
        assertEquals(0L, repo.count(EventType.WATER))
        repo.reset()
        assertEquals(0L, repo.count(EventType.HEALTHY))
    }

    @Test fun weightPaginationAndEatingResetIsolation() {
        WeightRepository(context).use { repo ->
            repeat(35) { repo.add(70000 + it) }
            val first = repo.page(0)
            val next = repo.page(0, first.entries.last())
            assertEquals(30, first.entries.size)
            assertTrue(first.hasMore)
            assertEquals(5, next.entries.size)
            assertFalse(next.hasMore)
            assertEquals(35, (first.entries + next.entries).map { it.id }.distinct().size)
            assertEquals(34, repo.summary()!!.change)
            val latest = first.entries.first()
            repo.update(latest.copy(grams = 71000))
            assertEquals(71000, repo.summary()!!.latest)
            EatingRepository(context).reset()
            assertEquals(71000, repo.summary()!!.latest)
            repo.delete(latest.id)
            assertEquals(70033, repo.summary()!!.latest)
        }
    }

    @Test fun upgradeVersionEightPreservesWeightAndAddsIndex() {
        val path = context.getDatabasePath("eattrack.db")
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.execSQL("CREATE TABLE weight_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, grams INTEGER NOT NULL CHECK (grams > 0))")
            db.execSQL("INSERT INTO weight_entries VALUES (42, 1000, 72501)")
            db.version = 8
        }
        EatingDatabase(context).use { helper ->
            val db = helper.writableDatabase
            assertEquals(9, db.version)
            db.rawQuery("SELECT grams FROM weight_entries WHERE id = 42", null).use {
                assertTrue(it.moveToFirst()); assertEquals(72501, it.getInt(0))
            }
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_weight_time'", null).use {
                assertTrue(it.moveToFirst())
            }
        }
    }
}
