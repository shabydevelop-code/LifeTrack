package com.shabydevelop.eattrack

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AboutScreen(version: String, logo: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().visibleVerticalScroll(rememberScrollState()).padding(UiSpacing.screen),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.medium)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
            logo()
            Text("LifeTrack", style = UiTypography.screenTitle, color = UiColors.primary)
            Text("מעקב אישי לאורח חיים בריא", style = UiTypography.body, textAlign = TextAlign.Center)
        }
        HorizontalDivider()
        Text("LifeTrack נועדה לסייע במעקב אחר הרגלים יומיומיים, לחזק בחירות בריאות ולעודד התמדה ושיפור אישי לאורך זמן.", style = UiTypography.body)
        listOf(
            "מעקב אחר הרגלי אכילה" to UiColors.healthy,
            "מעקב אחר שתיית מים" to UiColors.water,
            "מעקב אחר פעילות גופנית" to UiColors.aerobic,
            "מעקב אחר משקל" to UiColors.primary
        ).forEach { (label, color) ->
            Text(label, color = color, style = UiTypography.body)
        }
        HorizontalDivider()
        SectionHeader("פרטיות המידע")
        Text("הנתונים נשמרים במכשיר האישי ואינם מועברים לשרת חיצוני על ידי האפליקציה.", style = UiTypography.body)
        HorizontalDivider()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
            Text("גרסה $version", style = UiTypography.supporting)
            Text("LifeTrack © 2026", style = UiTypography.body)
        }
    }
}
