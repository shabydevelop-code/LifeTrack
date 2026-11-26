package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
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

enum class EatingReason(val label: String) {
    HUNGER("רעב"), SWEET_CRAVING("חשק למתוק"), BOREDOM("שעמום"), STRESS("לחץ"),
    FATIGUE("עייפות"), HABIT("הרגל"), TEMPTATION("פיתוי"), SOCIAL("אירוע חברתי"),
    NO_TIME("חוסר זמן"), EMOTION("מצב רגשי"), OTHER("אחר"), UNKNOWN("לא ברור לי")
}

@Composable
internal fun ReasonDialog(model: CounterViewModel) {
    var selected by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    AlertDialog(
        onDismissRequest = model::dismissReasons,
        title = { Text("מה הוביל לאכילה?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("הדיווח נשמר. אפשר לבחור כמה סיבות או לדלג.")
                EatingReason.entries.forEach { reason ->
                    val checked = reason.name in selected
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                        value = checked, enabled = !model.busy, role = Role.Checkbox,
                        onValueChange = { value ->
                            selected = ArrayList(if (value) selected + reason.name else selected.filterNot { it == reason.name })
                        }), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checked, onCheckedChange = null, enabled = !model.busy)
                        Text(reason.label, Modifier.padding(start = 12.dp))
                    }
                }
                if (model.reasonError) Text("לא ניתן לשמור את הסיבות. אפשר לנסות שוב או לדלג.",
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        },
        confirmButton = { TextButton(enabled = selected.isNotEmpty() && !model.busy,
            onClick = { model.saveReasons(EatingReason.entries.filter { it.name in selected }.toSet()) }) { Text("שמירה") } },
        dismissButton = { TextButton(enabled = !model.busy, onClick = model::dismissReasons) { Text("דילוג") } }
    )
}
