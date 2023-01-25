package com.trm.daylighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.trm.daylighter.feature.about.AboutScreen
import com.trm.daylighter.feature.about.aboutRoute
import com.trm.daylighter.feature.day.DayScreen
import com.trm.daylighter.feature.day.dayRoute
import com.trm.daylighter.feature.location.LocationScreen
import com.trm.daylighter.feature.location.locationRoute
import com.trm.daylighter.ui.theme.DayLighterTheme
import kotlinx.coroutines.launch

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalComposeUiApi::class,
  ExperimentalLayoutApi::class
)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      DayLighterTheme {
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ModalNavigationDrawer(
          drawerState = drawerState,
          drawerContent = {
            ModalDrawerSheet {
              Spacer(Modifier.height(12.dp))
              drawerDestinations.forEach { destination ->
                NavigationDrawerItem(
                  icon = {
                    Icon(
                      imageVector = destination.icon,
                      contentDescription = "drawer_item_${destination.label.lowercase()}"
                    )
                  },
                  label = { Text(text = destination.label) },
                  selected = false,
                  onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(
                      route = destination.route,
                      navOptions = navOptions { launchSingleTop = true }
                    )
                  }
                )
              }
            }
          }
        ) {
          Scaffold(
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
              FloatingActionButton(
                onClick = {
                  navController.navigate(
                    route = locationRoute,
                    navOptions = navOptions { launchSingleTop = true }
                  )
                }
              ) {
                Icon(imageVector = Icons.Filled.Add, "")
              }
            }
          ) {
            NavHost(
              navController = navController,
              startDestination = dayRoute,
              modifier =
                Modifier.padding(it)
                  .consumedWindowInsets(it)
                  .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
              composable(dayRoute) { DayScreen() }
              composable(locationRoute) { LocationScreen() }
              composable(aboutRoute) { AboutScreen() }
            }
          }
        }
      }
    }
  }

  companion object {
    private val drawerDestinations =
      listOf(DrawerDestination(route = aboutRoute, icon = Icons.Filled.Info, "About"))
  }
}

internal data class DrawerDestination(val route: String, val icon: ImageVector, val label: String)
