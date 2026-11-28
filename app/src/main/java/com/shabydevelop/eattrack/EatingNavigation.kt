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
    OnPlatformResume(context as androidx.activity.ComponentActivity) { model.reload() }
    NavigationShell(model, route, drawer, version, PlatformClock, { PlatformFeedback.success(view) },
        { PlatformFeedback.animationsEnabled(context) }, { target ->
            nav.navigate(target) {
                popUpTo(nav.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }) {
                NavHost(navController = nav, startDestination = "home", modifier = Modifier.fillMaxSize()) {
                    composable("home") { CounterScreen(model.report, model.error, model::increment, model::addActivity, model::addWater, model.busy, model::addFood, banner = { HomeBanner(it) }) { nav.navigate("weight") } }
                    composable("reports") { ReportsScreen(model.report, model.error) }
                    composable("history") { HistoryScreen(model, PlatformDateTime::date, PlatformDateTime::time) { event, dismiss -> EditEventDialog(event, model, PlatformClock, { timestamp, enabled, change -> DateTimeFields(timestamp, enabled, change) }, dismiss) } }
                    composable("about") { AboutScreen(version) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(UiSpacing.aboutLogo).background(UiColors.primary, UiShapes.control)
                        )
                    } }
                    composable("settings") { ProtectedArea { SettingsScreen(model) } }
                    composable("weight") { ProtectedArea { AndroidWeightScreen() } }
                }
    }
}
