package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

internal object UiSpacing {
    val tiny = 2.dp
    val cell = 3.dp
    val small = 4.dp
    val compact = 6.dp
    val normal = 8.dp
    val medium = 12.dp
    val screen = 16.dp
    val large = 24.dp
    val touch = 48.dp
    val action = 56.dp
}

internal object UiColors {
    val primary = Color(0xFF1E4064)
    val healthy = Color(0xFF166534)
    val unhealthy = Color(0xFFB91C1C)
    val avoided = Color(0xFF1D4ED8)
}

@Composable
internal fun EatTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = UiColors.primary), content = content)
}

@Composable
internal fun AdaptiveActionPair(first: @Composable () -> Unit, second: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 340.dp || LocalDensity.current.fontScale >= 1.3f) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                first()
                second()
            }
        } else {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                Box(Modifier.weight(1f).fillMaxHeight(), propagateMinConstraints = true) { first() }
                Box(Modifier.weight(1f).fillMaxHeight(), propagateMinConstraints = true) { second() }
            }
        }
    }
}

@Composable
internal fun ReportAction(label: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = UiSpacing.action),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White,
            disabledContainerColor = color, disabledContentColor = Color.White)) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}
