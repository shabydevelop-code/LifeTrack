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
    val feedbackHost = remember { SnackbarHostState() }
    var feedbackColor by remember { mutableStateOf(UiColors.primary) }
    val view = androidx.compose.ui.platform.LocalView.current
    var handledSequence by androidx.compose.runtime.saveable.rememberSaveable { mutableLongStateOf(model.successSequence) }
    LaunchedEffect(model.successSequence) {
        if (model.successSequence > handledSequence) {
            handledSequence = model.successSequence
            feedbackHost.currentSnackbarData?.dismiss()
            feedbackColor = metricFeedbackColor(model.feedbackType)
            PlatformFeedback.success(view)
            feedbackHost.showSnackbar(model.feedback, duration = SnackbarDuration.Short)
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
            val now = PlatformClock.nowMillis()
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
                Text("מעקב אורח חיים", Modifier.padding(20.dp).semantics { heading() }, style = UiTypography.screenTitle)
                listOf("home" to "מסך ראשי", "reports" to "דוחות", "history" to "היסטוריית דיווחים", "weight" to "מעקב משקל", "settings" to "הגדרות").forEach { (target, label) ->
                    NavigationDrawerItem(label = { Text(label, style = UiTypography.body) },
                        icon = if (target == "weight" || target == "settings") ({ PrivacyIcon(color = LocalContentColor.current) }) else null, selected = route == target,
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
                Text("גרסה $version", Modifier.align(Alignment.CenterHorizontally).padding(UiSpacing.screen), style = UiTypography.supporting)
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                AppHeader(
                    title = when (route) { "reports" -> "דוחות"; "history" -> "היסטוריית דיווחים"; "settings" -> "הגדרות"; "weight" -> "מעקב משקל"; else -> "מעקב אורח חיים" },
                    onMenu = { scope.launch { drawer.open() } }
                )
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.weight(1f)) {
                    composable("home") { CounterScreen(model.report, model.error, model::increment, model::addActivity, model::addWater, model.busy) { nav.navigate("weight") } }
                    composable("reports") { ReportsScreen(model.report, model.error) }
                    composable("history") { HistoryScreen(model) }
                    composable("settings") { ProtectedArea { SettingsScreen(model) } }
                    composable("weight") { ProtectedArea { WeightScreen() } }
                }
            }
        }
        ConfettiEffect(model.celebrationSequence, when (model.celebrationType) { EventType.HEALTHY -> ConfettiStyle.Healthy; EventType.AEROBIC -> ConfettiStyle.Aerobic; else -> ConfettiStyle.Avoided }, Modifier.matchParentSize())
        AppFeedbackHost(feedbackHost, feedbackColor, Modifier.align(Alignment.BottomCenter).padding(UiSpacing.screen))
        }
    }
}
