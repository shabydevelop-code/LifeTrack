package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun EditEventDialog(event: EatingEvent, model: CounterViewModel, clock: AppClock, dateTimeFields: @Composable (Long, Boolean, (Long) -> Unit) -> Unit, dismiss: () -> Unit) {
    var timestamp by remember(event.id) { mutableLongStateOf(event.timestamp) }
    var type by remember(event.id) { mutableStateOf(event.type) }
    var foodKind by remember(event.id) { mutableStateOf(event.foodKind) }
    var percent by remember(event.id) { mutableIntStateOf(event.healthyPercent ?: 100) }
    var reasons by remember(event.id) { mutableStateOf(event.reasons.toSet()) }
    var activity by remember(event.id) { mutableStateOf(event.activity ?: ActivityKind.WALK) }
    var minutes by remember(event.id) { mutableStateOf(if (event.minutes > 0) event.minutes.toString() else "30") }
    var water by remember(event.id) { mutableStateOf(if (event.milliliters > 0) event.milliliters.toString() else "250") }
    var failed by remember { mutableStateOf(false) }
    val valid = timestamp <= clock.nowMillis() && (type != EventType.AEROBIC || (minutes.toIntOrNull() ?: 0) > 0) && (type != EventType.WATER || (water.toIntOrNull() ?: 0) > 0)
    AlertDialog(onDismissRequest = { if (!model.busy) dismiss() },
        title = { Text("עריכת דיווח", style = UiTypography.sectionTitle) },
        text = {
            Column(Modifier.visibleVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                dateTimeFields(timestamp, !model.busy) { timestamp = it }
                Text("סוג הדיווח", style = UiTypography.tableHeading)
                Column(Modifier.selectableGroup()) {
                    EventType.entries.forEach { option ->
                        EditChoice(option.label, type == option, !model.busy) {
                            if (type != option) { type = option; reasons = emptySet(); activity = ActivityKind.WALK; minutes = "30"; water = "250" }
                        }
                    }
                }
                if (type.isFood) {
                    Text("סוג אכילה")
                    Column(Modifier.selectableGroup()) {
                        EditChoice("טרם סווג", foodKind == null, !model.busy) { foodKind = null }
                        FoodKind.entries.forEach { kind -> EditChoice(kind.label, foodKind == kind, !model.busy) { foodKind = kind } }
                    }
                    Text("אחוז בריא")
                    Column(Modifier.selectableGroup()) {
                        listOf(100,75,50,25,0).forEach { p -> EditChoice("$p%", percent == p, !model.busy) { percent = p } }
                    }
                }
                if (type.isFood || type == EventType.AVOIDED) {
                    Text("סיבת הדיווח", style = UiTypography.tableHeading)
                    Column(Modifier.selectableGroup()) {
                        EditChoice("ללא סיבה", reasons.isEmpty(), !model.busy) { reasons = emptySet() }
                        (reasonOptions(if (type.isFood) EventType.UNHEALTHY else type) + reasons).distinct().forEach { reason ->
                            EditChoice(reason.label, reason in reasons, !model.busy) { reasons = setOf(reason) }
                        }
                    }
                }
                if (type == EventType.AEROBIC) {
                    Text("סוג הפעילות", color = UiColors.aerobic, style = UiTypography.tableHeading)
                    Column(Modifier.selectableGroup()) {
                        ActivityKind.entries.forEach { option -> EditChoice(option.label, activity == option, !model.busy) { activity = option } }
                    }
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter(Char::isDigit) }, enabled = !model.busy, singleLine = true,
                        label = { Text("משך הפעילות בדקות") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = (minutes.toIntOrNull() ?: 0) <= 0, modifier = Modifier.fillMaxWidth())
                }
                if (type == EventType.WATER) WaterAmountField(water, { water = it }, !model.busy)
                if (timestamp > clock.nowMillis()) Text("לא ניתן לבחור מועד עתידי", color = MaterialTheme.colorScheme.error)
                if (failed) Text("לא ניתן לשמור את הדיווח. ניתן לנסות שוב.", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        },
        confirmButton = { AppTextButton(enabled = valid && !model.busy, onClick = {
            failed = false
            model.updateEvent(event.copy(timestamp = timestamp, type = if (type.isFood) (if (percent == 100) EventType.HEALTHY else EventType.UNHEALTHY) else type, foodKind = if (type.isFood) foodKind else null, healthyPercent = if (type.isFood) percent else null, reasons = reasons.toList(), activity = if (type == EventType.AEROBIC) activity else null, minutes = if (type == EventType.AEROBIC) minutes.toInt() else 0, milliliters = if (type == EventType.WATER) water.toInt() else 0)) { ok -> if (ok) dismiss() else failed = true }
        }) { Text("שמירה", style = UiTypography.secondaryAction) } },
        dismissButton = { AppTextButton(enabled = !model.busy, onClick = dismiss) { Text("ביטול", style = UiTypography.secondaryAction) } })
}

@Composable
private fun EditChoice(label: String, selected: Boolean, enabled: Boolean, choose: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch).selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = choose), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Text(label, style = UiTypography.body)
    }
}
