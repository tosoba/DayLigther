package com.trm.daylighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.trm.daylighter.feature.about.AboutScreen
import com.trm.daylighter.feature.about.aboutRoute
import com.trm.daylighter.feature.day.DayRoute
import com.trm.daylighter.feature.day.dayRoute
import com.trm.daylighter.feature.location.LocationRoute
import com.trm.daylighter.feature.location.locationRoute
import com.trm.daylighter.ui.theme.DayLighterTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalComposeUiApi::class,
  ExperimentalLayoutApi::class
)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      DayLighterTheme {
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        ModalNavigationDrawer(
          gesturesEnabled = currentRoute == dayRoute,
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
            topBar = {
              AnimatedVisibility(visible = currentRoute != locationRoute) {
                CenterAlignedTopAppBar(
                  title = { Text(stringResource(id = R.string.app_name)) },
                  navigationIcon = {
                    if (currentRoute == dayRoute) {
                      IconButton(
                        onClick = {
                          scope.launch {
                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                          }
                        }
                      ) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = "toggle_drawer")
                      }
                    } else {
                      IconButton(onClick = navController::popBackStack) {
                        Icon(
                          imageVector = Icons.Filled.ArrowBack,
                          contentDescription = "back_arrow"
                        )
                      }
                    }
                  }
                )
              }
            }
          ) {
            DaylighterNavHost(
              navController = navController,
              modifier =
                Modifier.padding(it)
                  .consumedWindowInsets(it)
                  .windowInsetsPadding(WindowInsets.safeDrawing)
            )
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

@Composable
private fun DaylighterNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  NavHost(navController = navController, startDestination = dayRoute, modifier = modifier) {
    composable(dayRoute) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        onAddLocation = {
          navController.navigate(
            route = locationRoute,
            navOptions = navOptions { launchSingleTop = true }
          )
        }
      )
    }
    composable(locationRoute) {
      LocationRoute(navController = navController, modifier = Modifier.fillMaxSize())
    }
    composable(aboutRoute) { AboutScreen(modifier = Modifier.fillMaxSize()) }
  }
}

internal data class DrawerDestination(val route: String, val icon: ImageVector, val label: String)
