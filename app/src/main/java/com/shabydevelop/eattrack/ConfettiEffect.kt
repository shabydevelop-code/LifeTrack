package com.shabydevelop.eattrack

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

internal data class ConfettiStyle(val durationMillis: Int, val particleCount: Int, val colors: List<Color>) {
    companion object {
        val Healthy = ConfettiStyle(800, 28, listOf(UiColors.healthy, Color(0xFF22C55E), Color(0xFF86EFAC)))
        val Avoided = ConfettiStyle(1500, 65, listOf(UiColors.healthy, UiColors.avoided, Color(0xFFF59E0B), Color(0xFFDB2777), Color(0xFF9333EA)))
    }
}

@Composable
internal fun ConfettiEffect(sequence: Long, style: ConfettiStyle, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val progress = remember { Animatable(1f) }
    var handled by rememberSaveable { mutableLongStateOf(sequence) }
    var visible by remember { mutableStateOf(false) }
    val particles = remember(sequence, style) {
        val random = Random(sequence)
        List(style.particleCount) { FloatArray(5) { random.nextFloat() } }
    }
    val colors = style.colors
    LaunchedEffect(sequence) {
        if (sequence == handled) return@LaunchedEffect
        handled = sequence
        if (android.os.Build.VERSION.SDK_INT >= 26 && !android.animation.ValueAnimator.areAnimatorsEnabled()) {
            visible = false
            return@LaunchedEffect
        }
        if (android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f) {
            visible = false
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        visible = true
        try { progress.animateTo(1f, tween(style.durationMillis, easing = LinearEasing)) }
        finally { visible = false }
    }
    if (visible) Canvas(modifier) {
        val t = progress.value
        particles.forEachIndexed { i, p ->
            val x = size.width * (0.5f + (p[0] - 0.5f) * (0.2f + t * 1.8f))
            val y = size.height * (0.2f - p[1] * t * 0.65f + t * t * (0.7f + p[2] * 0.6f))
            val w = (4f + p[3] * 5f) * density
            rotate(p[4] * 360f + t * 420f, Offset(x, y)) {
                drawRect(colors[i % colors.size].copy(alpha = (1f - t).coerceIn(0f, 1f)), Offset(x, y), Size(w, w * 1.6f))
            }
        }
    }
}
