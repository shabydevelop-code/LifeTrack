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

internal enum class HabitSymbol { FOOD, UNHEALTHY, AVOIDED, WATER, ACTIVITY, WEIGHT, PRIVACY }

@Composable
internal fun HabitIcon(symbol: HabitSymbol, color: Color) {
    Canvas(Modifier.size(UiSpacing.large)) {
        val u = size.width / 24f
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(color, Offset(x1*u,y1*u), Offset(x2*u,y2*u), 1.8f*u, StrokeCap.Round)
        fun outline(points: List<Pair<Float, Float>>, close: Boolean = false) {
            val p = Path()
            points.forEachIndexed { i, (x,y) -> if (i == 0) p.moveTo(x*u,y*u) else p.lineTo(x*u,y*u) }
            if (close) p.close()
            drawPath(p,color,style=Stroke(1.8f*u,cap=StrokeCap.Round))
        }
        when(symbol) {
            HabitSymbol.FOOD -> {
                for (x in listOf(4f,7f,10f)) line(x,3f,x,9f)
                outline(listOf(4f to 9f,7f to 12f,10f to 9f))
                line(7f,12f,7f,21f)
                outline(listOf(19f to 3f,15f to 7f,15f to 13f,19f to 13f,19f to 21f))
                line(19f,3f,19f,13f)
            }
            HabitSymbol.UNHEALTHY -> {
                outline(listOf(3f to 5f,21f to 5f,12f to 22f),true)
                for ((x,y) in listOf(9f to 9f,15f to 9f,12f to 14f)) drawCircle(color,1.4f*u,Offset(x*u,y*u))
            }
            HabitSymbol.AVOIDED -> {
                drawCircle(color,9*u,Offset(12*u,12*u),style=Stroke(1.8f*u))
                outline(listOf(7f to 12f,10f to 15f,17f to 8f))
            }
            HabitSymbol.WATER -> {
                val p = Path().apply {
                    moveTo(12*u,2*u)
                    cubicTo(9*u,7*u,4*u,11*u,4*u,15*u)
                    cubicTo(4*u,24*u,20*u,24*u,20*u,15*u)
                    cubicTo(20*u,11*u,15*u,7*u,12*u,2*u)
                    close()
                }
                drawPath(p,color,style=Stroke(1.8f*u))
            }
            HabitSymbol.ACTIVITY -> outline(listOf(2f to 12f,6f to 12f,9f to 4f,14f to 20f,17f to 12f,22f to 12f))
            HabitSymbol.WEIGHT -> {
                outline(listOf(3f to 3f,21f to 3f,21f to 21f,3f to 21f),true)
                drawCircle(color,4*u,Offset(12*u,10*u),style=Stroke(1.8f*u))
                line(12f,10f,14f,7f)
            }
            HabitSymbol.PRIVACY -> {
                outline(listOf(12f to 2f,21f to 6f,20f to 15f,16f to 20f,12f to 22f,8f to 20f,4f to 15f,3f to 6f),true)
                outline(listOf(8f to 12f,11f to 15f,16f to 9f))
            }
        }
    }
}

