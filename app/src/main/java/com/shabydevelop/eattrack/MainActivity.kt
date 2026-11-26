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
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.WHITE)
        )
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
internal fun CounterScreen(report: EatingReport, error: Boolean, increment: (EventType) -> Unit, undo: () -> Unit, busy: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val gap = (maxHeight * 0.018f).coerceIn(8.dp, 16.dp)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = gap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Text(report.todayLabel, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactSummary("בריאה היום", report.healthyToday, Modifier.weight(1f))
            CompactSummary("לא בריאה היום", report.today, Modifier.weight(1f))
            CompactSummary("ויתור היום", report.avoidedToday, Modifier.weight(1f))
        }
        Button(onClick = { increment(EventType.HEALTHY) }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534), contentColor = Color.White)) {
            Text("אכילה בריאה", style = MaterialTheme.typography.titleLarge)
        }
        Button(onClick = { increment(EventType.UNHEALTHY) }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C), contentColor = Color.White)) {
            Text("אכילה לא בריאה", style = MaterialTheme.typography.titleLarge)
        }
        Button(onClick = { increment(EventType.AVOIDED) }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8), contentColor = Color.White)) { Text("ויתרתי על הפיתוי", style = MaterialTheme.typography.titleLarge) }
        OutlinedButton(onClick = undo, enabled = !busy && report.total + report.healthyTotal + report.avoidedTotal > 0,
            modifier = Modifier.heightIn(min = 48.dp)) { Text("ביטול דיווח אחרון") }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
        listOf("השבוע" to listOf(report.healthyWeek, report.week, report.avoidedWeek), "החודש" to listOf(report.healthyMonth, report.month, report.avoidedMonth), "מצטבר" to listOf(report.healthyTotal, report.total, report.avoidedTotal)).forEach { (label, counts) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f))
                Text("בריאה: ${counts[0]}", Modifier.weight(1f), color = Color(0xFF166534))
                Text("לא בריאה: ${counts[1]}", Modifier.weight(1f), color = Color(0xFFB91C1C))
                Text("ויתור: ${counts[2]}", Modifier.weight(1f), color = Color(0xFF1D4ED8))
            }
        }
        if (error) Text("לא ניתן לרענן את הנתונים. בדוק את מצב הדיווח לפני ניסיון נוסף.", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    }
}

@Composable
private fun CompactSummary(label: String, count: Long, modifier: Modifier = Modifier) {
    Card(modifier = modifier.semantics(mergeDescendants = true) {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(count.toString(), style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
