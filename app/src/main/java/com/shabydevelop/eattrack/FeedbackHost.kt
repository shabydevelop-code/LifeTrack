package com.shabydevelop.eattrack

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

internal fun metricFeedbackColor(type: EventType?): Color = when (type) {
    EventType.HEALTHY -> UiColors.healthy
    EventType.UNHEALTHY -> UiColors.unhealthy
    EventType.AVOIDED -> UiColors.avoided
    EventType.WATER -> UiColors.water
    EventType.AEROBIC -> UiColors.aerobic
    null -> UiColors.primary
}

@Composable
internal fun AppFeedbackHost(state: SnackbarHostState, color: Color, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = state, modifier = modifier) { data ->
        Snackbar(snackbarData = data, containerColor = color, contentColor = Color.White, shape = UiShapes.control)
    }
}
