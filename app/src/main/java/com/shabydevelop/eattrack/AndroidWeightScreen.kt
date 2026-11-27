package com.shabydevelop.eattrack

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
internal fun AndroidWeightScreen() {
    val context = LocalContext.current.applicationContext
    WeightScreen(PlatformClock, { period, cursor, action ->
        withContext(Dispatchers.IO) {
            databaseOperation { WeightRepository(context).use { repo -> action(repo); repo.page(period, cursor) to repo.summary() } }
        }
    }, PlatformDateTime::dateTime,
        { grams -> String.format(Locale.getDefault(), "%.1f", grams / 1000.0) },
        ::editableWeightKg,
        ::parseWeightGrams,
        { timestamp, change -> DateTimeFields(timestamp, change = change) })
}
