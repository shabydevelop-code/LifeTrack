package com.shabydevelop.eattrack
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
internal fun DateTimeFields(timestamp: Long, enabled: Boolean = true, change: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    var picker by remember { mutableStateOf<android.app.Dialog?>(null) }
    DisposableEffect(Unit) { onDispose { picker?.dismiss() } }
    AppOutlinedButton(enabled = enabled, modifier = Modifier.fillMaxWidth(), onClick = {
        picker = android.app.DatePickerDialog(context, { _, y, m, d ->
            change(Calendar.getInstance().apply { timeInMillis = timestamp; set(y,m,d) }.timeInMillis)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = PlatformClock.nowMillis(); show() }
    }) { Text("תאריך: " + PlatformDateTime.date(timestamp)) }
    AppOutlinedButton(enabled = enabled, modifier = Modifier.fillMaxWidth(), onClick = {
        picker = android.app.TimePickerDialog(context, { _, h, m ->
            change(Calendar.getInstance().apply { timeInMillis = timestamp; set(Calendar.HOUR_OF_DAY,h); set(Calendar.MINUTE,m); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0) }.timeInMillis)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).apply { show() }
    }) { Text("שעה: " + PlatformDateTime.time(timestamp)) }
}
