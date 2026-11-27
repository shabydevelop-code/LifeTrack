package com.shabydevelop.eattrack

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.ContextCompat

internal class AuthenticationManager(private val activity: FragmentActivity) {
    fun authenticate(result: (Boolean) -> Unit): BiometricPrompt {
        val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(value: BiometricPrompt.AuthenticationResult) { result(true) }
                override fun onAuthenticationError(code: Int, message: CharSequence) { result(false) }
            })
        val info = BiometricPrompt.PromptInfo.Builder().setTitle("אימות זהות")
            .setSubtitle("כניסה לאזור אישי")
            .setDeviceCredentialAllowed(true).build()
        prompt.authenticate(info)
        return prompt
    }
}

@Composable
internal fun ProtectedArea(content: @Composable () -> Unit) {
    val activity = LocalContext.current as FragmentActivity
    val manager = remember(activity) { AuthenticationManager(activity) }
    var unlocked by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf(false) }
    var prompt by remember { mutableStateOf<BiometricPrompt?>(null) }
    var active by remember { mutableStateOf(true) }
    DisposableEffect(activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) unlocked = false
        }
        activity.lifecycle.addObserver(observer)
        onDispose {
            active = false
            activity.lifecycle.removeObserver(observer)
            prompt?.cancelAuthentication()
        }
    }
    if (unlocked) content() else Column(Modifier.fillMaxSize().padding(UiSpacing.screen),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        Text("אזור אישי מוגן", style = UiTypography.sectionTitle)
        Text("יש לאמת את הזהות באמצעות נעילת המכשיר.")
        AppOutlinedButton(onClick = {
            pending = true
            prompt = manager.authenticate { success ->
                if (active) { pending = false; unlocked = success }
            }
        }, enabled = !pending, modifier = Modifier.fillMaxWidth()) { Text("אימות זהות") }
    }
}
