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
    val view = androidx.compose.ui.platform.LocalView.current
    var handledSequence by androidx.compose.runtime.saveable.rememberSaveable { mutableLongStateOf(model.successSequence) }
    LaunchedEffect(model.successSequence) {
        if (model.successSequence > handledSequence) {
            handledSequence = model.successSequence
            android.widget.Toast.makeText(context.applicationContext, model.feedback, android.widget.Toast.LENGTH_SHORT).show()
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
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1E4064))
            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.statusBars).union(WindowInsets.displayCutout).only(WindowInsetsSides.Top))
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
                Text("EatTrack", Modifier.padding(20.dp), style = MaterialTheme.typography.titleLarge)
                listOf("home" to "מסך ראשי", "reports" to "דוחות").forEach { (target, label) ->
                    NavigationDrawerItem(label = { Text(label) }, selected = route == target,
                        onClick = {
                            nav.navigate(target) {
                                popUpTo(nav.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                            scope.launch { drawer.close() }
                        }, modifier = Modifier.padding(horizontal = 12.dp))
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                Text("גרסה $version", Modifier.align(Alignment.CenterHorizontally).padding(16.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    ) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(Color(0xFF1E4064)).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawer.open() } }) {
                        androidx.compose.foundation.Canvas(Modifier.size(24.dp).semantics {
                            this.contentDescription = "פתיחת תפריט"
                        }) {
                            for (i in 1..3) drawLine(Color.White,
                                androidx.compose.ui.geometry.Offset(2.dp.toPx(), size.height * i / 4),
                                androidx.compose.ui.geometry.Offset(size.width - 2.dp.toPx(), size.height * i / 4),
                                strokeWidth = 2.dp.toPx())
                        }
                    }
                    Text(if (route == "reports") "דוחות אכילה" else "מעקב אכילה אישי",
                        color = Color.White, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp).semantics { heading() })
                }
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.weight(1f)) {
                    composable("home") { CounterScreen(model.report, model.error, model::increment, model::undo, model.busy) }
                    composable("reports") { ReportsScreen(model.report, model.error, model::reset, model.busy) }
                }
            }
        }
    }
}
