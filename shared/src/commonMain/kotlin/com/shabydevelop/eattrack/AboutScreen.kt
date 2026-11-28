package com.shabydevelop.eattrack

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@Composable
private fun AboutRow(label: String, symbol: HabitSymbol, color: Color, isHeading: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        HabitIcon(symbol, color)
        Text(label, modifier = Modifier.weight(1f).then(if (isHeading) Modifier.semantics { heading() } else Modifier),
            style = if (isHeading) UiTypography.sectionTitle else UiTypography.body)
    }
}

@Composable
fun AboutScreen(version: String, logo: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().visibleVerticalScroll(rememberScrollState()).padding(horizontal = UiSpacing.screen, vertical = UiSpacing.normal),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.tiny)) {
            logo()
            Text("LifeTrack", style = UiTypography.screenTitle, color = UiColors.primary)
            Text("מעקב אישי לאורח חיים בריא", style = UiTypography.body, textAlign = TextAlign.Center)
        }
        HorizontalDivider()
        AboutRow("הרגלי אכילה", HabitSymbol.FOOD, UiColors.healthy)
        AboutRow("שתיית מים", HabitSymbol.WATER, UiColors.water)
        AboutRow("פעילות גופנית", HabitSymbol.ACTIVITY, UiColors.aerobic)
        AboutRow("מעקב משקל", HabitSymbol.WEIGHT, UiColors.primary)
        HorizontalDivider()
        AboutRow("פרטיות המידע", HabitSymbol.PRIVACY, UiColors.primary, isHeading = true)
        Text("הנתונים נשמרים במכשיר בלבד.", style = UiTypography.body)
        HorizontalDivider()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.tiny)) {
            Text("גרסה $version", style = UiTypography.supporting)
            Text("LifeTrack © 2026", style = UiTypography.supporting)
        }
    }
}
