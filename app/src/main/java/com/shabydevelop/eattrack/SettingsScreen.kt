package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.*

@Composable
internal fun SettingsScreen(model: CounterViewModel) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().visibleVerticalScroll(rememberScrollState()).padding(UiSpacing.screen), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        OutlinedButton(onClick = { confirm = true }, enabled = !model.busy && model.count > 0,
            modifier = Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch)) { Text("מחיקת היסטוריית דיווחים") }
        if (model.error) Text("לא ניתן לעדכן את הנתונים", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("מחיקת היסטוריית דיווחים", style = UiTypography.sectionTitle) },
        text = { Text("כל הדיווחים יימחקו לצמיתות. להמשיך?") },
        confirmButton = { TextButton(onClick = { confirm = false; model.reset() }) { Text("מחיקה", style = UiTypography.secondaryAction) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("ביטול", style = UiTypography.secondaryAction) } })
}
