package com.shabydevelop.androiddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF1E4064))) {
                    val model: CounterViewModel = viewModel()
                    EatingNavigation(model)
                }
            }
        }
    }
}

@Composable
internal fun CounterScreen(count: Long, error: Boolean, increment: () -> Unit, reset: () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("איפוס דיווחים") },
            text = { Text("כל הדיווחים יימחקו לצמיתות. להמשיך?") },
            confirmButton = { TextButton(onClick = { confirmReset = false; reset() }) { Text("איפוס") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("ביטול") } }
        )
    }
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                Button(
                    onClick = increment,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C), contentColor = Color.White)
                ) { Text("אכילה לא בריאה", style = MaterialTheme.typography.titleLarge) }
                Text("מספר דיווחים: $count", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { confirmReset = true }, enabled = count > 0) { Text("איפוס") }
                if (error) Text("לא ניתן לעדכן את הנתונים. נסה שוב.", color = MaterialTheme.colorScheme.error)
            }
}
