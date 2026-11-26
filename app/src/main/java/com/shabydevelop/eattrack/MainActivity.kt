package com.shabydevelop.eattrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
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
internal fun CounterScreen(report: EatingReport, error: Boolean, increment: () -> Unit, undo: () -> Unit, busy: Boolean, feedback: String) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(report.todayLabel, style = MaterialTheme.typography.titleMedium)
        Text(report.today.toString(), style = MaterialTheme.typography.displayLarge, color = Color(0xFFB91C1C))
        Text("דיווחים היום")
        Button(onClick = increment, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C), contentColor = Color.White)) {
            Text("אכילה לא בריאה", style = MaterialTheme.typography.titleLarge)
        }
        OutlinedButton(onClick = undo, enabled = !busy && report.total > 0,
            modifier = Modifier.heightIn(min = 48.dp)) { Text("ביטול דיווח אחרון") }
        Text(if (busy) "מעדכן נתונים…" else feedback,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        HorizontalDivider()
        ReportSummary("השבוע", report.weekLabel, report.week)
        ReportSummary("החודש", report.monthLabel, report.month)
        ReportSummary("סה״כ מתחילת המעקב", report.totalLabel, report.total)
        if (error) Text("לא ניתן לרענן את הנתונים. בדוק את מצב הדיווח לפני ניסיון נוסף.", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
}
