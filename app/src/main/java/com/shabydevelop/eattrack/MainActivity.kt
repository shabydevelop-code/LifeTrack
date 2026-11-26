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
                EatTrackTheme {
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
    val gap = (maxHeight * 0.014f).coerceIn(UiSpacing.compact, UiSpacing.medium)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = UiSpacing.screen, vertical = gap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Text("סיכום היום · " + report.todayLabel, style = UiTypography.sectionTitle, modifier = Modifier.semantics { heading() })
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
            CompactSummary("בריאה", report.healthyToday, Modifier.weight(1f).fillMaxHeight())
            CompactSummary("לא בריאה", report.today, Modifier.weight(1f).fillMaxHeight())
            CompactSummary("ויתור", report.avoidedToday, Modifier.weight(1f).fillMaxHeight())
        }
        ReportAction("אכילה בריאה", UiColors.healthy, !busy) { increment(EventType.HEALTHY) }
        ReportAction("אכילה לא בריאה", UiColors.unhealthy, !busy) { increment(EventType.UNHEALTHY) }
        ReportAction("ויתרתי על הפיתוי", UiColors.avoided, !busy) { increment(EventType.AVOIDED) }
        OutlinedButton(onClick = { if (!busy) undo() }, enabled = report.total + report.healthyTotal + report.avoidedTotal > 0,
            modifier = Modifier.heightIn(min = UiSpacing.touch)) { Text("ביטול דיווח אחרון", style = UiTypography.secondaryAction) }
        Spacer(Modifier.height(UiSpacing.small))
        HorizontalDivider()
        Text("סיכום מצטבר", style = UiTypography.sectionTitle, modifier = Modifier.semantics { heading() })
        PeriodTable(report.periodRows(false), false)
        if (error) Text("לא ניתן לרענן את הנתונים. בדוק את מצב הדיווח לפני ניסיון נוסף.", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    }
}

@Composable
private fun CompactSummary(label: String, count: Long, modifier: Modifier = Modifier) {
    Card(modifier = modifier.semantics(mergeDescendants = true) {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = UiSpacing.compact, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
            Text(label, style = UiTypography.tableHeading,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(count.toString(), style = UiTypography.counter,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
