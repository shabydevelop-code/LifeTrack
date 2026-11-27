package com.shabydevelop.eattrack

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun PrivacyIcon(color: Color, fingerprint: Boolean = false) {
    Canvas(Modifier.size(UiSpacing.compactIcon)) {
        val w = size.width
        val h = size.height
        val stroke = w * .085f
        if (fingerprint) {
            for (i in 0..2) {
                val inset = .12f + i * .13f
                drawArc(color, 165f, 235f, false, Offset(w * inset, h * inset), Size(w * (1-2*inset), h * (1-2*inset)), style = Stroke(stroke, cap = StrokeCap.Round))
            }
            drawLine(color, Offset(w*.5f,h*.48f), Offset(w*.44f,h*.88f), stroke, StrokeCap.Round)
        } else {
            drawArc(color, 180f, 180f, false, Offset(w*.28f,h*.08f), Size(w*.44f,h*.48f), style = Stroke(stroke))
            drawRoundRect(color, Offset(w*.17f,h*.43f), Size(w*.66f,h*.48f), CornerRadius(w*.08f), style = Stroke(stroke))
            drawLine(color, Offset(w*.5f,h*.60f), Offset(w*.5f,h*.75f), stroke, StrokeCap.Round)
        }
    }
}
