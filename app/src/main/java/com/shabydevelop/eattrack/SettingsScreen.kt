package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsScreen(model: CounterViewModel) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedButton(onClick = { confirm = true }, enabled = !model.busy && model.count > 0,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("מחיקת היסטוריית דיווחים") }
        if (model.error) Text("לא ניתן לעדכן את הנתונים", color = MaterialTheme.colorScheme.error)
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("מחיקת היסטוריית דיווחים") },
        text = { Text("כל הדיווחים יימחקו לצמיתות. להמשיך?") },
        confirmButton = { TextButton(onClick = { confirm = false; model.reset() }) { Text("מחיקה") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("ביטול") } })
}
