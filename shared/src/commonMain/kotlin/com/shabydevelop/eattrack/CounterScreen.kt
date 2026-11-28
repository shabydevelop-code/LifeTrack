package com.shabydevelop.eattrack

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

@Composable
fun CounterScreen(report: EatingReport, error: Boolean, increment: (EventType) -> Unit, addActivity: (ActivityKind, Int) -> Unit, addWater: (Int) -> Unit, busy: Boolean, addFood: (FoodKind, Int) -> Unit, banner: @Composable (androidx.compose.ui.unit.Dp) -> Unit, openWeight: () -> Unit) {
    var foodKind by remember { mutableStateOf<FoodKind?>(null) }
    foodKind?.let { kind ->
        AlertDialog(onDismissRequest = { foodKind = null }, title = { Text(kind.label, style = UiTypography.sectionTitle) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                listOf(true, false).forEach { healthy ->
                    AppOutlinedButton(onClick = { foodKind = null; addFood(kind, if (healthy) 100 else 0) }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch)) {
                        Text(if (kind == FoodKind.MEAL) (if (healthy) "בריאה" else "לא בריאה") else (if (healthy) "בריא" else "לא בריא"), color = if (healthy) UiColors.healthy else UiColors.unhealthy, style = UiTypography.secondaryAction)
                    }
                }
            } }, confirmButton = {}, dismissButton = { AppTextButton(onClick = { foodKind = null }) { Text("ביטול", style = UiTypography.secondaryAction) } })
    }
    var activityDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    if (activityDialog) ActivityDialog(busy, { activityDialog = false }, { kind, minutes -> activityDialog = false; addActivity(kind, minutes) })
    var waterDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    if (waterDialog) WaterDialog(busy, { waterDialog = false }, { amount -> waterDialog = false; addWater(amount) })
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val availableHeight = maxHeight
    Column(
        modifier = Modifier.fillMaxSize().visibleVerticalScroll(rememberScrollState()).padding(horizontal = UiSpacing.screen, vertical = UiSpacing.homeTopGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        banner(availableHeight)
        Spacer(Modifier.height(UiSpacing.homeBannerGap))
        HabitActions(listOf(
            HabitActionSpec("ארוחה", UiColors.primary, HabitSymbol.FOOD, !busy) { foodKind = FoodKind.MEAL },
            HabitActionSpec("נשנוש", UiColors.primary, HabitSymbol.SNACK, !busy) { foodKind = FoodKind.SNACK },
            HabitActionSpec("ויתרתי על הפיתוי", UiColors.avoided, HabitSymbol.AVOIDED, !busy) { increment(EventType.AVOIDED) },
            HabitActionSpec("שתיית מים", UiColors.water, HabitSymbol.WATER, !busy) { waterDialog = true },
            HabitActionSpec("פעילות אירובית", UiColors.aerobic, HabitSymbol.ACTIVITY, !busy) { activityDialog = true },
            HabitActionSpec("מעקב משקל", UiColors.primary, HabitSymbol.WEIGHT, !busy) { openWeight() }
        ))
        Spacer(Modifier.height(UiSpacing.homeSummaryGap))
        DailySummary(report)
        if (error) Spacer(Modifier.height(UiSpacing.normal))
        if (error) Text("לא ניתן לרענן את הנתונים. בדוק את מצב הדיווח לפני ניסיון נוסף.", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
    }
    }
}

