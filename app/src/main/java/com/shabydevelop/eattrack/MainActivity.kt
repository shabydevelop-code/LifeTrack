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
internal fun CounterScreen(report: EatingReport, error: Boolean, increment: (EventType) -> Unit, addActivity: (ActivityKind, Int) -> Unit, addWater: (Int) -> Unit, busy: Boolean) {
    var activityDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    if (activityDialog) ActivityDialog(busy, { activityDialog = false }, { kind, minutes -> activityDialog = false; addActivity(kind, minutes) })
    var waterDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    if (waterDialog) WaterDialog(busy, { waterDialog = false }, { amount -> waterDialog = false; addWater(amount) })
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val gap = (maxHeight * 0.014f).coerceIn(UiSpacing.compact, UiSpacing.medium)
    Column(
        modifier = Modifier.fillMaxSize().visibleVerticalScroll(rememberScrollState()).padding(horizontal = UiSpacing.screen, vertical = gap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        SectionHeader("סיכום אכילה יומי · " + report.todayLabel)
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
            CompactSummary("בריאה", report.healthyToday, Modifier.weight(1f).fillMaxHeight())
            CompactSummary("לא בריאה", report.today, Modifier.weight(1f).fillMaxHeight())
            CompactSummary("ויתור", report.avoidedToday, Modifier.weight(1f).fillMaxHeight())
        }
        AdaptiveActionPair(
            { ReportAction("אכילה בריאה", UiColors.healthy, !busy) { increment(EventType.HEALTHY) } },
            { ReportAction("אכילה לא בריאה", UiColors.unhealthy, !busy) { increment(EventType.UNHEALTHY) } })
        AdaptiveActionPair(
            { ReportAction("ויתרתי על הפיתוי", UiColors.avoided, !busy) { increment(EventType.AVOIDED) } },
            { ReportAction("שתיית מים", UiColors.water, !busy) { waterDialog = true } })
        ReportAction("פעילות אירובית", UiColors.aerobic, !busy) { activityDialog = true }
        Spacer(Modifier.height(UiSpacing.small))
        HorizontalDivider()
        SectionHeader("סיכום אכילה מצטבר")
        PeriodTable(report.periodRows(false), false)
        WaterSummary(report.water)
        ActivitySummary(report.activity)
        if (error) Text("לא ניתן לרענן את הנתונים. בדוק את מצב הדיווח לפני ניסיון נוסף.", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    }
}

