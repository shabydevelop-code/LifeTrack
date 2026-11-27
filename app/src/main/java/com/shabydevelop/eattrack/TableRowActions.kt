package com.shabydevelop.eattrack
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
@Composable
internal fun TableRowActions(description: String, enabled: Boolean, edit: () -> Unit, delete: () -> Unit) {
    Row {
                IconButton(onClick = edit, enabled = enabled, modifier = Modifier.size(UiSpacing.touch).semantics { contentDescription = "עריכת $description" }) {
                    androidx.compose.foundation.Canvas(Modifier.size(UiSpacing.tableActionIcon)) {
                        val w = UiSpacing.tiny.toPx()
                        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(UiColors.primary, androidx.compose.ui.geometry.Offset(size.width*x1,size.height*y1), androidx.compose.ui.geometry.Offset(size.width*x2,size.height*y2), w)
                        line(.15f,.85f,.22f,.55f); line(.22f,.55f,.72f,.05f); line(.72f,.05f,.95f,.28f); line(.95f,.28f,.45f,.78f); line(.45f,.78f,.15f,.85f); line(.62f,.15f,.85f,.38f)
                    }
                }
                IconButton(onClick = delete, enabled = enabled, modifier = Modifier.size(UiSpacing.touch).semantics { contentDescription = "מחיקת $description" }) {
                    androidx.compose.foundation.Canvas(Modifier.size(UiSpacing.tableActionIcon)) {
                        val c = UiColors.unhealthy
                        val w = UiSpacing.tiny.toPx()
                        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(c, androidx.compose.ui.geometry.Offset(size.width*x1,size.height*y1), androidx.compose.ui.geometry.Offset(size.width*x2,size.height*y2), w)
                        line(.15f,.25f,.85f,.25f); line(.4f,.12f,.6f,.12f)
                        line(.25f,.3f,.3f,.88f); line(.75f,.3f,.7f,.88f); line(.3f,.88f,.7f,.88f)
                        line(.43f,.4f,.43f,.72f); line(.57f,.4f,.57f,.72f)
                    }
                }
    }
}
