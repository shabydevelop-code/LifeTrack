package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.ui.platform.LocalAccessibilityManager

@Composable
fun NavigationShell(model: CounterViewModel, route: String, drawer: DrawerState, version: String, clock: AppClock, successFeedback: () -> Unit, animationsEnabled: () -> Boolean, navigate: (String) -> Unit, destination: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val accessibilityManager = LocalAccessibilityManager.current
    val successDuration = accessibilityManager?.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = 1200L, containsIcons = false, containsText = true, containsControls = false
    ) ?: 1200L
    val feedbackHost = remember { SnackbarHostState() }
    var feedbackColor by remember { mutableStateOf(UiColors.primary) }
    var handledSequence by androidx.compose.runtime.saveable.rememberSaveable { mutableLongStateOf(model.successSequence) }
    LaunchedEffect(model.successSequence) {
        if (model.successSequence > handledSequence) {
            handledSequence = model.successSequence
            feedbackHost.currentSnackbarData?.dismiss()
            feedbackColor = metricFeedbackColor(model.feedbackType)
            successFeedback()
            withTimeoutOrNull(successDuration) {
                feedbackHost.showSnackbar(model.feedback, duration = SnackbarDuration.Indefinite)
            }
        }
    }
    var handledFailure by androidx.compose.runtime.saveable.rememberSaveable { mutableLongStateOf(model.failureSequence) }
    val errorColor = MaterialTheme.colorScheme.error
    LaunchedEffect(model.failureSequence) {
        if (model.failureSequence > handledFailure) {
            handledFailure = model.failureSequence
            feedbackHost.currentSnackbarData?.dismiss()
            feedbackColor = errorColor
            feedbackHost.showSnackbar("לא ניתן להשלים את הפעולה. בדוק את מצב הדיווח.", duration = SnackbarDuration.Long)
        }
    }
    LaunchedEffect(route) {
        while (true) {
            model.reload()
            val now = clock.nowMillis()
            delay(60000L - now % 60000L)
        }
    }
    model.pendingReasonId?.let { id -> key(id) { ReasonDialog(model) } }
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize().background(UiColors.primary)
            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.statusBars).union(WindowInsets.displayCutout).only(WindowInsetsSides.Top))
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
                Text("מעקב אורח חיים", Modifier.padding(20.dp).semantics { heading() }, style = UiTypography.screenTitle)
                listOf("home" to "מסך ראשי", "reports" to "דוחות", "history" to "היסטוריית דיווחים", "weight" to "מעקב משקל", "settings" to "הגדרות", "about" to "אודות").forEach { (target, label) ->
                    NavigationDrawerItem(label = { Text(label, style = UiTypography.body) },
                        icon = if (target == "weight" || target == "settings") ({ PrivacyIcon(color = LocalContentColor.current) }) else null, selected = route == target,
                        onClick = {
                            navigate(target)
                            scope.launch { drawer.close() }
                        }, modifier = Modifier.padding(horizontal = UiSpacing.medium))
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                Text("גרסה $version", Modifier.align(Alignment.CenterHorizontally).padding(UiSpacing.screen), style = UiTypography.supporting)
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                AppHeader(
                    title = "מעקב אורח חיים",
                    onMenu = { scope.launch { drawer.open() } }
                )
                val screenTitle = when (route) {
                    "reports" -> "דוחות"
                    "history" -> "היסטוריית דיווחים"
                    "settings" -> "הגדרות"
                    "about" -> "אודות"
                    "weight" -> "מעקב משקל"
                    else -> null
                }
                screenTitle?.let {
                    Text(it, Modifier.fillMaxWidth().background(UiColors.screenTitleBackground).padding(horizontal = UiSpacing.screen, vertical = UiSpacing.normal).semantics { heading() }, color = UiColors.primary, style = UiTypography.screenTitle)
                }
                Box(Modifier.weight(1f)) { destination() }
            }
        }
        ConfettiEffect(model.celebrationSequence, when (model.celebrationType) { EventType.HEALTHY -> ConfettiStyle.Healthy; EventType.AEROBIC -> ConfettiStyle.Aerobic; EventType.WATER -> ConfettiStyle.Water; else -> ConfettiStyle.Avoided }, Modifier.matchParentSize()) { animationsEnabled() }
        AppFeedbackHost(feedbackHost, feedbackColor, Modifier.align(Alignment.BottomCenter).padding(UiSpacing.screen))
        }
    }
}
