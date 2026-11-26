package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

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
    val aerobic = Color(0xFF7C3AED)
    val avoided = Color(0xFF1D4ED8)
}

internal object UiTypography {
    private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) =
        TextStyle(fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight)

    val screenTitle = style(24, 32, FontWeight.SemiBold)
    val sectionTitle = style(20, 28, FontWeight.Medium)
    val primaryAction = style(18, 24, FontWeight.Medium)
    val body = style(16, 24)
    val secondaryAction = style(16, 24, FontWeight.Medium)
    val tableCell = style(14, 20)
    val tableHeading = style(14, 20, FontWeight.Medium)
    val supporting = style(12, 16)
    val counter = style(24, 32, FontWeight.SemiBold)
    val material = Typography(
        headlineSmall = counter, titleLarge = screenTitle, titleMedium = sectionTitle,
        titleSmall = tableHeading, bodyLarge = body, bodyMedium = body,
        bodySmall = supporting, labelLarge = secondaryAction, labelMedium = tableHeading,
        labelSmall = supporting
    )
}

@Composable
internal fun EatTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = UiColors.primary), typography = UiTypography.material, content = content)
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
        Text(label, style = UiTypography.primaryAction)
    }
}

@Composable
internal fun AppHeader(title: String, onMenu: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(UiColors.primary).padding(horizontal = UiSpacing.normal),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenu, modifier = Modifier.size(UiSpacing.touch)) {
            androidx.compose.foundation.Canvas(Modifier.size(UiSpacing.large).semantics {
                contentDescription = "פתיחת תפריט"
            }) {
                for (i in 1..3) drawLine(Color.White,
                    androidx.compose.ui.geometry.Offset(UiSpacing.tiny.toPx(), size.height * i / 4),
                    androidx.compose.ui.geometry.Offset(size.width - UiSpacing.tiny.toPx(), size.height * i / 4),
                    strokeWidth = UiSpacing.tiny.toPx())
            }
        }
        Text(title, modifier = Modifier.weight(1f).padding(vertical = UiSpacing.screen).semantics { heading() },
            color = Color.White, style = UiTypography.screenTitle,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.width(UiSpacing.touch))
    }
}

@Composable
internal fun SectionHeader(label: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(label, modifier = Modifier.fillMaxWidth().semantics { heading() }, style = UiTypography.sectionTitle,
        color = color, textAlign = androidx.compose.ui.text.style.TextAlign.Start)
}

private fun Modifier.scrollIndicator(metrics: () -> Pair<Float, Float>?): Modifier = drawWithContent {
    drawContent()
    val (fraction, position) = metrics() ?: return@drawWithContent
    val width = 4.dp.toPx()
    val thumb = (size.height * fraction).coerceIn(minOf(24.dp.toPx(), size.height), size.height)
    val x = if (layoutDirection == LayoutDirection.Rtl) 0f else size.width - width
    drawRoundRect(Color(0xFFD1D5DB), Offset(x, 0f), Size(width, size.height), CornerRadius(width / 2))
    drawRoundRect(Color(0xFF64748B), Offset(x, (size.height - thumb) * position.coerceIn(0f, 1f)), Size(width, thumb), CornerRadius(width / 2))
}

internal fun Modifier.visibleVerticalScroll(state: ScrollState): Modifier =
    scrollIndicator {
        val max = state.maxValue
        if (max <= 0 || max == Int.MAX_VALUE || state.viewportSize <= 0) null
        else state.viewportSize.toFloat() / (state.viewportSize.toFloat() + max) to state.value.toFloat() / max
    }.padding(end = UiSpacing.normal).verticalScroll(state)

internal fun Modifier.lazyScrollIndicator(state: LazyListState): Modifier =
    scrollIndicator {
        val info = state.layoutInfo
        val total = info.totalItemsCount
        val visible = info.visibleItemsInfo
        if (total == 0 || visible.isEmpty() || (!state.canScrollForward && !state.canScrollBackward)) null
        else {
            val first = visible.first()
            val last = visible.last()
            val start = first.index + ((info.viewportStartOffset - first.offset).toFloat() / first.size.coerceAtLeast(1)).coerceIn(0f, 1f)
            val end = last.index + ((info.viewportEndOffset - last.offset).toFloat() / last.size.coerceAtLeast(1)).coerceIn(0f, 1f)
            val span = (end - start).coerceIn(0.01f, total.toFloat())
            span / total to (start / (total - span).coerceAtLeast(0.01f))
        }
    }.padding(end = UiSpacing.normal)
