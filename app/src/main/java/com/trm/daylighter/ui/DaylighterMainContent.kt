package com.trm.daylighter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trm.daylighter.R
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.feature.about.AboutScreen
import com.trm.daylighter.feature.about.aboutRoute
import com.trm.daylighter.feature.day.DayRoute
import com.trm.daylighter.feature.day.dayRoute
import com.trm.daylighter.feature.location.*
import com.trm.daylighter.feature.locations.locationsGraph
import com.trm.daylighter.feature.locations.locationsGraphRoute
import com.trm.daylighter.feature.widgets.WidgetsScreen
import com.trm.daylighter.feature.widgets.widgetsRoute
import kotlinx.coroutines.launch

@Composable
private fun NavController.currentRoute(): String =
  currentBackStackEntryAsState().value?.destination?.route ?: dayRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaylighterMainContent() {
  val scope = rememberCoroutineScope()
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val currentRoute = navController.currentRoute()

  ModalNavigationDrawer(
    gesturesEnabled = currentRoute == dayRoute,
    drawerState = drawerState,
    drawerContent = {
      DaylighterDrawerContent(
        onItemClick = { destination ->
          scope.launch { drawerState.close() }
          navController.navigate(
            route = destination.route,
            navOptions = navOptions { launchSingleTop = true }
          )
        }
      )
    }
  ) {
    DayLighterScaffold(
      navController = navController,
      onDrawerMenuClick = {
        scope.launch { if (drawerState.isOpen) drawerState.close() else drawerState.open() }
      },
      topBar = {
        AnimatedVisibility(
          visible = currentRoute != dayRoute && !currentRoute.startsWith(locationRoute)
        ) {
          CenterAlignedTopAppBar(
            title = { Text(stringResource(id = R.string.app_name)) },
            navigationIcon = {
              IconButton(onClick = navController::popBackStack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "back_arrow")
              }
            }
          )
        }
      }
    )
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DaylighterDrawerContent(onItemClick: (DrawerDestination) -> Unit) {
  val drawerDestinations = remember {
    sequenceOf(
      DrawerDestination(route = widgetsRoute, icon = Icons.Filled.Widgets, "Widgets"),
      DrawerDestination(route = locationsGraphRoute, icon = Icons.Filled.LocationOn, "Locations"),
      DrawerDestination(route = aboutRoute, icon = Icons.Filled.Info, "About")
    )
  }

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
        onClick = { onItemClick(destination) }
      )
    }
  }
}

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalComposeUiApi::class,
  ExperimentalLayoutApi::class
)
@Composable
private fun DayLighterScaffold(
  navController: NavHostController,
  onDrawerMenuClick: () -> Unit,
  topBar: @Composable () -> Unit
) {
  Scaffold(
    modifier = Modifier.semantics { testTagsAsResourceId = true },
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = topBar,
  ) {
    DaylighterNavHost(
      navController = navController,
      onDrawerMenuClick = onDrawerMenuClick,
      modifier =
        Modifier.padding(it).consumedWindowInsets(it).windowInsetsPadding(WindowInsets.safeDrawing)
    )
  }
}

@Composable
private fun DaylighterNavHost(
  navController: NavHostController,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  fun navigateToAddLocation() {
    navController.navigate(
      route = locationRoute,
      navOptions = navOptions { launchSingleTop = true }
    )
  }

  val addLocationDeepLinkUri = stringResource(id = commonR.string.add_location_deep_link_uri)

  NavHost(navController = navController, startDestination = dayRoute, modifier = modifier) {
    composable(dayRoute) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        onDrawerMenuClick = onDrawerMenuClick,
        onAddLocation = ::navigateToAddLocation
      )
    }

    composable(aboutRoute) { AboutScreen(modifier = Modifier.fillMaxSize()) }

    composable(
      locationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = addLocationDeepLinkUri })
    ) {
      LocationRoute(onBackClick = navController::popBackStack, modifier = Modifier.fillMaxSize())
    }

    locationsGraph(
      onAddLocationClick = ::navigateToAddLocation,
      onEditLocationClick = {
        navController.navigate(
          route = "$locationRoute/$it",
          navOptions = navOptions { launchSingleTop = true }
        )
      }
    ) {
      composable(
        route = editLocationRoute,
        arguments = listOf(navArgument(locationIdParam) { type = NavType.LongType })
      ) {
        LocationRoute(onBackClick = navController::popBackStack, modifier = Modifier.fillMaxSize())
      }
    }

    composable(widgetsRoute) { WidgetsScreen(modifier = Modifier.fillMaxSize()) }
  }
}

private data class DrawerDestination(val route: String, val icon: ImageVector, val label: String)
