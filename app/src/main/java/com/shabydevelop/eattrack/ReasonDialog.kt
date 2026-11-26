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

enum class EatingReason(val label: String) {
    HUNGER("רעב"), SWEET_CRAVING("חשק למתוק"), BOREDOM("שעמום"), STRESS("לחץ"),
    FATIGUE("עייפות"), HABIT("הרגל"), TEMPTATION("פיתוי"), SOCIAL("אירוע חברתי"),
    NO_TIME("חוסר זמן"), EMOTION("מצב רגשי"), OTHER("אחר"), UNKNOWN("לא ברור לי"), GOAL("נזכרתי במטרה שלי"), ALTERNATIVE("בחרתי בחלופה בריאה"), DISTRACTION("הסחתי את דעתי"), DISTANCE("התרחקתי מהפיתוי"), WAITED("המתנתי והחשק חלף"), NOT_HUNGRY("הבנתי שאינני רעב")
}

@Composable
internal fun ReasonDialog(model: CounterViewModel) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val avoided = model.pendingReasonType == EventType.AVOIDED
    val options = if (avoided) listOf(EatingReason.GOAL, EatingReason.ALTERNATIVE, EatingReason.DISTRACTION, EatingReason.DISTANCE, EatingReason.WAITED, EatingReason.NOT_HUNGRY, EatingReason.OTHER) else EatingReason.entries.takeWhile { it != EatingReason.GOAL }
    AlertDialog(
        onDismissRequest = model::dismissReasons,
        title = { Text(if (avoided) "מה עזר לי לוותר?" else "מה הוביל לאכילה?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                Text("הדיווח נשמר. אפשר לבחור סיבה מרכזית אחת או לדלג.")
                options.forEach { reason ->
                    val checked = reason.name == selected
                    Row(Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch).selectable(
                        selected = checked, enabled = !model.busy, role = Role.RadioButton,
                        onClick = { selected = reason.name }), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = checked, onClick = null, enabled = !model.busy)
                        Text(reason.label, Modifier.padding(start = UiSpacing.medium))
                    }
                }
                if (model.reasonError) Text("לא ניתן לשמור את הסיבות. אפשר לנסות שוב או לדלג.",
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        },
        confirmButton = { TextButton(enabled = selected != null && !model.busy,
            onClick = { model.saveReasons(options.filter { it.name == selected }.toSet()) }) { Text("שמירה") } },
        dismissButton = { TextButton(enabled = !model.busy, onClick = model::dismissReasons) { Text("דילוג") } }
    )
}
