package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.*
import java.util.Calendar
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
    var edit by remember { mutableStateOf<WeightEntry?>(null) }
    var timestamp by remember { mutableLongStateOf(0L) }
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
    LazyColumn(Modifier.fillMaxSize().lazyScrollIndicator(state), state = state, contentPadding = PaddingValues(UiSpacing.screen)) {
        item {
            AppOutlinedButton(onClick = { input = ""; edit = null; timestamp = System.currentTimeMillis(); add = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("דיווח משקל חדש") }
        }
        if (entries.isNotEmpty()) item {
            Spacer(Modifier.height(UiSpacing.normal))
            TableFrame {
                TableLine(listOf(2f,1f), Modifier.padding(vertical = UiSpacing.normal)) { i -> Text(if (i == 0) "משקל אחרון" else kg(entries.first().grams) + " ק״ג", style = UiTypography.tableCell) }
                HorizontalDivider()
                TableLine(listOf(2f,1f), Modifier.padding(vertical = UiSpacing.normal)) { i -> Text(if (i == 0) "שינוי מתחילת המעקב" else kg(entries.first().grams - entries.last().grams) + " ק״ג", style = UiTypography.tableCell) }
            }
        }
        item { Spacer(Modifier.height(UiSpacing.normal)); SectionHeader("היסטוריית שקילות"); Spacer(Modifier.height(UiSpacing.small)) }
        if (error) item { Text("לא ניתן להשלים את הפעולה", color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(UiSpacing.normal)) }
        if (busy) item { CircularProgressIndicator(); Spacer(Modifier.height(UiSpacing.normal)) }
        if (!busy && entries.isEmpty()) item { Text("אין שקילות") }
        if (entries.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth().tableSegment(top = true).tableHeaderBackground().padding(vertical = UiSpacing.normal).semantics { heading() }, verticalAlignment = Alignment.CenterVertically) {
                TableLine(listOf(2f,1f), Modifier.weight(1f)) { i -> Text(if (i == 0) "תאריך ושעה" else "ק״ג", style = UiTypography.tableHeading) }
                Text("פעולות", Modifier.width(UiSpacing.touch * 2), style = UiTypography.tableHeading, textAlign = TextAlign.Center)
            }
        }
        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
            Column(Modifier.fillMaxWidth().tableSegment(bottom = index == entries.lastIndex)) {
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TableLine(listOf(2f,1f), Modifier.weight(1f)) { i -> Text(if (i == 0) date else kg(entry.grams), style = UiTypography.tableCell) }
                    TableRowActions("שקילה $date", !busy, {
                        edit = entry
                        input = java.math.BigDecimal.valueOf(entry.grams.toLong(), 3).stripTrailingZeros().toPlainString()
                        timestamp = entry.timestamp
                        add = true
                    }, { delete = entry })
                }
                if (index != entries.lastIndex) HorizontalDivider()
            }
        }
    }
    if (add) {
        val grams = input.replace(',','.').toBigDecimalOrNull()?.movePointRight(3)?.let { runCatching { it.intValueExact() }.getOrNull() }
        AlertDialog(onDismissRequest = { add = false }, title = { Text(if (edit == null) "דיווח משקל חדש" else "עריכת שקילה", style = UiTypography.sectionTitle) },
            text = { Column(Modifier.visibleVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) { OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("משקל בק״ג") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                if (edit != null) WeightDateTimeFields(timestamp) { timestamp = it }
            } },
            confirmButton = { AppTextButton(onClick = { grams?.let { value -> val existing = edit; val time = timestamp; refresh { repo -> if (existing == null) repo.add(value) else repo.update(existing.copy(grams = value, timestamp = time)) } }; add = false }, enabled = grams != null && grams > 0 && timestamp <= System.currentTimeMillis() && !busy) { Text("שמירה") } },
            dismissButton = { AppTextButton(onClick = { add = false }) { Text("ביטול") } })
    }
    delete?.let { entry ->
        AlertDialog(onDismissRequest = { delete = null }, title = { Text("מחיקת שקילה") }, text = { Text("למחוק את השקילה?") },
            confirmButton = { AppTextButton(onClick = { delete = null; refresh { it.delete(entry.id) } }, color = UiColors.unhealthy) { Text("מחיקה") } },
            dismissButton = { AppTextButton(onClick = { delete = null }) { Text("ביטול") } })
    }
}

@Composable
private fun WeightDateTimeFields(timestamp: Long, change: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    var picker by remember { mutableStateOf<android.app.Dialog?>(null) }
    DisposableEffect(Unit) { onDispose { picker?.dismiss() } }
    AppOutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
        picker = android.app.DatePickerDialog(context, { _, y, m, d ->
            change(Calendar.getInstance().apply { timeInMillis = timestamp; set(y,m,d) }.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = System.currentTimeMillis(); show() }
    }) { Text("תאריך: " + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))) }
    AppOutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
        picker = android.app.TimePickerDialog(context, { _, h, m ->
            change(Calendar.getInstance().apply { timeInMillis = timestamp; set(Calendar.HOUR_OF_DAY,h); set(Calendar.MINUTE,m); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }.timeInMillis)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).apply { show() }
    }) { Text("שעה: " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))) }
    if (timestamp > System.currentTimeMillis()) Text("לא ניתן לבחור מועד עתידי", color = MaterialTheme.colorScheme.error)
}
