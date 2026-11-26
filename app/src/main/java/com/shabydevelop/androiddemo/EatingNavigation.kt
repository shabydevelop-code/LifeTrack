package com.shabydevelop.androiddemo

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

@Composable
internal fun EatingNavigation(model: CounterViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "home"
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }
    LaunchedEffect(route) { if (route == "reports") model.reload() }
    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet {
                Text("תפריט ראשי", Modifier.padding(20.dp), style = MaterialTheme.typography.titleLarge)
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
            }
        }
    ) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
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
                    composable("home") { CounterScreen(model.count, model.error, model::increment, model::reset) }
                    composable("reports") { ReportsScreen(model.report, model.error) }
                }
            }
        }
    }
}
