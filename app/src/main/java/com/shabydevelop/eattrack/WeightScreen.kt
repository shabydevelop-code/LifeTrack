package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun WeightScreen() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<WeightEntry>>(emptyList()) }
    var busy by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var add by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<WeightEntry?>(null) }
    fun refresh(action: (WeightRepository) -> Unit = {}) {
        busy = true
        scope.launch {
            try {
                entries = withContext(Dispatchers.IO) { WeightRepository(context).use { repo -> action(repo); repo.list() } }
                error = false
            } catch (e: android.database.SQLException) { error = true }
            finally { busy = false }
        }
    }
    LaunchedEffect(Unit) { refresh() }
    fun kg(grams: Int) = String.format(Locale.getDefault(), "%.1f", grams / 1000.0)
    val state = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize().lazyScrollIndicator(state), state = state, contentPadding = PaddingValues(UiSpacing.screen), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        item {
            AppOutlinedButton(onClick = { input = ""; add = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("דיווח משקל חדש") }
        }
        if (entries.isNotEmpty()) item {
            TableFrame {
                TableLine(listOf(2f,1f), Modifier.padding(vertical = UiSpacing.normal)) { i -> Text(if (i == 0) "משקל אחרון" else kg(entries.first().grams) + " ק״ג", style = UiTypography.tableCell) }
                HorizontalDivider()
                TableLine(listOf(2f,1f), Modifier.padding(vertical = UiSpacing.normal)) { i -> Text(if (i == 0) "שינוי מתחילת המעקב" else kg(entries.first().grams - entries.last().grams) + " ק״ג", style = UiTypography.tableCell) }
            }
        }
        item { SectionHeader("היסטוריית שקילות") }
        if (error) item { Text("לא ניתן להשלים את הפעולה", color = MaterialTheme.colorScheme.error) }
        if (busy) item { CircularProgressIndicator() }
        if (!busy && entries.isEmpty()) item { Text("אין שקילות") }
        items(entries, key = { it.id }) { entry ->
            TableFrame {
                TableLine(listOf(2f,1f,1f)) { i ->
                    when(i) {
                        0 -> Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp)), style = UiTypography.tableCell)
                        1 -> Text(kg(entry.grams), style = UiTypography.tableCell)
                        else -> AppTextButton(onClick = { delete = entry }, enabled = !busy, color = UiColors.unhealthy) { Text("מחיקה") }
                    }
                }
            }
        }
    }
    if (add) {
        val grams = input.replace(',','.').toBigDecimalOrNull()?.movePointRight(3)?.let { runCatching { it.intValueExact() }.getOrNull() }
        AlertDialog(onDismissRequest = { add = false }, title = { Text("דיווח משקל חדש") },
            text = { OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("משקל בק״ג") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true) },
            confirmButton = { AppTextButton(onClick = { grams?.let { refresh { repo -> repo.add(it) } }; add = false }, enabled = grams != null && grams > 0 && !busy) { Text("שמירה") } },
            dismissButton = { AppTextButton(onClick = { add = false }) { Text("ביטול") } })
    }
    delete?.let { entry ->
        AlertDialog(onDismissRequest = { delete = null }, title = { Text("מחיקת שקילה") }, text = { Text("למחוק את השקילה?") },
            confirmButton = { AppTextButton(onClick = { delete = null; refresh { it.delete(entry.id) } }, color = UiColors.unhealthy) { Text("מחיקה") } },
            dismissButton = { AppTextButton(onClick = { delete = null }) { Text("ביטול") } })
    }
}
