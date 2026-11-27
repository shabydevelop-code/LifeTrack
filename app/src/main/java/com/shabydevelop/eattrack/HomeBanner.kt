package com.shabydevelop.eattrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeBanner(availableHeight: Dp) {
    val fontScale = LocalDensity.current.fontScale
    // Decorative content yields to controls and daily summary on constrained screens.
    val height = (availableHeight * 0.14f).coerceAtMost(UiSpacing.homeBannerMaxHeight)
    if (availableHeight < 540.dp || fontScale >= 1.3f || height < UiSpacing.homeBannerMinHeight) return
    Image(
        painter = painterResource(R.drawable.home_banner),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(height).clip(UiShapes.control)
    )
}
