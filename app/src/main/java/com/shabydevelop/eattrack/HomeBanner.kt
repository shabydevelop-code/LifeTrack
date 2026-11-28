package com.shabydevelop.eattrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
internal fun HomeBanner(availableHeight: Dp) {
    // Keep the banner visible; shrink it on constrained screens.
    val height = (availableHeight * 0.12f).coerceIn(UiSpacing.touch, UiSpacing.homeBannerMaxHeight)
    Image(
        painter = painterResource(R.drawable.home_banner),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth().height(height).clip(UiShapes.control)
    )
}
