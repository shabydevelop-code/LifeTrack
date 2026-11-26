package com.shabydevelop.eattrack

import androidx.activity.compose.BackHandler
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
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun EatingNavigation(model: CounterViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }
    val context = LocalContext.current
    val version = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }
    val toastManager = remember(context.applicationContext) { ToastManager(context.applicationContext) }
    DisposableEffect(toastManager) { onDispose { toastManager.cancel() } }
    val view = androidx.compose.ui.platform.LocalView.current
    var handledSequence by androidx.compose.runtime.saveable.rememberSaveable { mutableLongStateOf(model.successSequence) }
    LaunchedEffect(model.successSequence) {
        if (model.successSequence > handledSequence) {
            handledSequence = model.successSequence
            toastManager.show(model.feedback)
            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
    LaunchedEffect(route) {
        while (true) {
            model.reload()
            val now = System.currentTimeMillis()
            delay(60000L - now % 60000L)
        }
    }
    DisposableEffect(context) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) model.reload()
        }
        val owner = context as androidx.activity.ComponentActivity
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
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
                Text("מעקב אכילה אישי", Modifier.padding(20.dp).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
                listOf("home" to "מסך ראשי", "reports" to "דוחות", "history" to "היסטוריית דיווחים", "settings" to "הגדרות").forEach { (target, label) ->
                    NavigationDrawerItem(label = { Text(label) }, selected = route == target,
                        onClick = {
                            nav.navigate(target) {
                                popUpTo(nav.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                            scope.launch { drawer.close() }
                        }, modifier = Modifier.padding(horizontal = UiSpacing.medium))
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                Text("גרסה $version", Modifier.align(Alignment.CenterHorizontally).padding(UiSpacing.screen), style = MaterialTheme.typography.bodySmall)
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(UiColors.primary).padding(horizontal = UiSpacing.normal),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawer.open() } }) {
                        androidx.compose.foundation.Canvas(Modifier.size(UiSpacing.large).semantics {
                            this.contentDescription = "פתיחת תפריט"
                        }) {
                            for (i in 1..3) drawLine(Color.White,
                                androidx.compose.ui.geometry.Offset(UiSpacing.tiny.toPx(), size.height * i / 4),
                                androidx.compose.ui.geometry.Offset(size.width - UiSpacing.tiny.toPx(), size.height * i / 4),
                                strokeWidth = UiSpacing.tiny.toPx())
                        }
                    }
                    Text(when (route) { "reports" -> "דוחות אכילה"; "history" -> "היסטוריית דיווחים"; "settings" -> "הגדרות"; else -> "מעקב אכילה אישי" },
                        color = Color.White, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = UiSpacing.screen).semantics { heading() })
                }
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.weight(1f)) {
                    composable("home") { CounterScreen(model.report, model.error, model::increment, model::undo, model.busy) }
                    composable("reports") { ReportsScreen(model.report, model.error) }
                    composable("history") { HistoryScreen(model) }
                    composable("settings") { SettingsScreen(model) }
                }
            }
        }
        ConfettiEffect(model.celebrationSequence, if (model.celebrationType == EventType.HEALTHY) ConfettiStyle.Healthy else ConfettiStyle.Avoided, Modifier.matchParentSize())
        }
    }
}
