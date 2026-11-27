package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun ReasonDialog(model: CounterViewModel) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val avoided = model.pendingReasonType == EventType.AVOIDED
    val options = reasonOptions(model.pendingReasonType)
    AlertDialog(
        onDismissRequest = model::dismissReasons,
        title = { Text(if (avoided) "מה עזר לי לוותר?" else "מה הוביל לאכילה?", style = UiTypography.sectionTitle) },
        text = {
            Column(Modifier.visibleVerticalScroll(rememberScrollState()).selectableGroup()) {
                Text("הדיווח נשמר. אפשר לבחור סיבה מרכזית אחת או לדלג.")
                options.forEach { reason ->
                    val checked = reason.name == selected
                    Row(Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch).selectable(
                        selected = checked, enabled = !model.busy, role = Role.RadioButton,
                        onClick = { selected = reason.name }), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = checked, onClick = null, enabled = !model.busy)
                        Text(reason.label, Modifier.padding(start = UiSpacing.medium), style = UiTypography.body)
                    }
                }
                if (model.reasonError) Text("לא ניתן לשמור את הסיבות. אפשר לנסות שוב או לדלג.",
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        },
        confirmButton = { AppTextButton(enabled = selected != null && !model.busy,
            onClick = { model.saveReasons(options.filter { it.name == selected }.toSet()) }) { Text("שמירה", style = UiTypography.secondaryAction) } },
        dismissButton = { AppTextButton(enabled = !model.busy, onClick = model::dismissReasons) { Text("דילוג", style = UiTypography.secondaryAction) } }
    )
}

