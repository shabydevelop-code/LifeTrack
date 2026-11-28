package com.shabydevelop.eattrack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

internal data class HabitActionSpec(val label: String, val color: Color, val symbol: HabitSymbol,
    val enabled: Boolean, val protected: Boolean = false, val onClick: () -> Unit)

@Composable
internal fun HabitActions(actions: List<HabitActionSpec>) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val available = maxWidth
        Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
            actions.chunked(2).forEach { pair ->
                val needed = pair.maxOf { action ->
                    with(density) { measurer.measure(action.label, style = UiTypography.primaryAction,
                        softWrap = false).size.width.toDp() } + UiSpacing.normal * 2
                }
                if (pair.size == 2 && available >= needed * 2 + UiSpacing.normal) {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                        pair.forEach { HabitAction(it, Modifier.weight(1f).fillMaxHeight()) }
                    }
                } else {
                    pair.forEach { HabitAction(it, Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
private fun HabitAction(action: HabitActionSpec, modifier: Modifier) {
    val color = action.color
    OutlinedButton(
        onClick = action.onClick,
        enabled = action.enabled,
        modifier = modifier.heightIn(min = UiSpacing.habitActionMinHeight)
            .then(if (action.protected) Modifier.semantics { stateDescription = "מוגן" } else Modifier),
        shape = UiShapes.habitAction,
        border = BorderStroke(1.dp, if (action.enabled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = lerp(Color.White, color, 0.06f),
            contentColor = color,
            disabledContainerColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = UiSpacing.normal, vertical = UiSpacing.normal)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                HabitIcon(action.symbol, LocalContentColor.current)
                if (action.protected) PrivacyIcon(color = LocalContentColor.current)
            }
            Text(action.label, style = UiTypography.primaryAction, textAlign = TextAlign.Center)
        }
    }
}
