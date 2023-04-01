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
import com.trm.daylighter.feature.locations.locationsRoute
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
            title = {
              Text(
                stringResource(
                  id =
                    when (currentRoute) {
                      aboutRoute -> R.string.about_item
                      locationsRoute -> R.string.locations_item
                      widgetsRoute -> R.string.widgets_item
                      else -> R.string.empty
                    }
                )
              )
            },
            navigationIcon = {
              IconButton(onClick = navController::popBackStack) {
                Icon(
                  imageVector = Icons.Filled.ArrowBack,
                  contentDescription = stringResource(id = commonR.string.back)
                )
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
  val widgetsLabel = stringResource(R.string.widgets_item)
  val locationsLabel = stringResource(R.string.locations_item)
  val aboutLabel = stringResource(R.string.about_item)
  val drawerDestinations = remember {
    sequenceOf(
      DrawerDestination(route = widgetsRoute, icon = Icons.Filled.Widgets, label = widgetsLabel),
      DrawerDestination(
        route = locationsGraphRoute,
        icon = Icons.Filled.LocationOn,
        label = locationsLabel
      ),
      DrawerDestination(route = aboutRoute, icon = Icons.Filled.Info, label = aboutLabel)
    )
  }

  ModalDrawerSheet {
    Spacer(Modifier.height(12.dp))
    drawerDestinations.forEach { destination ->
      NavigationDrawerItem(
        icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
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

  fun navigateToEditLocation(locationId: Long) {
    navController.navigate(
      route = "$locationRoute/$locationId",
      navOptions = navOptions { launchSingleTop = true }
    )
  }

  fun NavGraphBuilder.editLocationRoute() {
    composable(
      route = editLocationRoute,
      arguments = listOf(navArgument(locationIdParam) { type = NavType.LongType })
    ) {
      LocationRoute(onBackClick = navController::popBackStack, modifier = Modifier.fillMaxSize())
    }
  }

  val addLocationDeepLinkUri = stringResource(id = commonR.string.add_location_deep_link_uri)

  NavHost(navController = navController, startDestination = dayRoute, modifier = modifier) {
    composable(dayRoute) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        onDrawerMenuClick = onDrawerMenuClick,
        onAddLocation = ::navigateToAddLocation,
        onEditLocation = ::navigateToEditLocation,
      )
    }

    composable(aboutRoute) { AboutScreen(modifier = Modifier.fillMaxSize()) }

    composable(
      route = locationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = addLocationDeepLinkUri })
    ) {
      LocationRoute(onBackClick = navController::popBackStack, modifier = Modifier.fillMaxSize())
    }

    editLocationRoute()

    locationsGraph(
      onAddLocationClick = ::navigateToAddLocation,
      onEditLocationClick = ::navigateToEditLocation
    ) {
      editLocationRoute()
    }

    composable(widgetsRoute) { WidgetsScreen(modifier = Modifier.fillMaxSize()) }
  }
}

private data class DrawerDestination(val route: String, val icon: ImageVector, val label: String)
