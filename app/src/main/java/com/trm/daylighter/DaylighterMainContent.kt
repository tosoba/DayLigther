package com.trm.daylighter

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.navigation.addLocationDeepLinkPattern
import com.trm.daylighter.core.common.navigation.dayNightCycleDeepLinkPattern
import com.trm.daylighter.core.common.navigation.goldenBlueHourDeepLinkPattern
import com.trm.daylighter.core.common.navigation.widgetLocationDeepLinkPattern
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.feature.about.AboutScreen
import com.trm.daylighter.feature.about.aboutRoute
import com.trm.daylighter.feature.day.DayRoute
import com.trm.daylighter.feature.day.dayNightCycleRoute
import com.trm.daylighter.feature.day.goldenBlueHourRoute
import com.trm.daylighter.feature.location.*
import com.trm.daylighter.feature.locations.LocationsRoute
import com.trm.daylighter.feature.locations.locationsRoute
import com.trm.daylighter.feature.settings.settingsComposable
import com.trm.daylighter.feature.settings.settingsNavigationRoute
import com.trm.daylighter.feature.settings.settingsRoute
import com.trm.daylighter.feature.widget.location.WidgetLocationRoute
import com.trm.daylighter.feature.widget.location.newWidgetRoute
import kotlinx.coroutines.launch

@Composable
private fun NavController.currentRoute(): String =
  currentBackStackEntryAsState().value?.destination?.route ?: dayNightCycleRoute

@Composable
fun DayLighterMainContent() {
  val scope = rememberCoroutineScope()
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val currentRoute = navController.currentRoute()

  fun onDrawerMenuClick() {
    scope.launch { with(drawerState) { if (isOpen) close() else open() } }
  }

  ModalNavigationDrawer(
    gesturesEnabled = !currentRoute.startsWith(locationRoute),
    drawerState = drawerState,
    drawerContent = {
      DayLighterDrawerContent(
        currentRoute = currentRoute,
        onRouteSelected = { destinationRoute ->
          scope.launch { drawerState.close() }
          if (destinationRoute != currentRoute) {
            navController.navigate(
              route = destinationRoute,
              navOptions =
                navController.topLevelNavOptions(
                  saveCurrentRouteState = !currentRoute.startsWith(newWidgetRoute),
                  restoreDestinationState = !destinationRoute.startsWith(newWidgetRoute)
                )
            )
          }
        }
      )
    }
  ) {
    DayLighterScaffold(navController = navController, onDrawerMenuClick = ::onDrawerMenuClick)
  }
}

@Composable
private fun DayLighterDrawerContent(
  currentRoute: String,
  onRouteSelected: (route: String) -> Unit
) {
  val context = LocalContext.current
  val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

  @Composable
  fun DrawerRouteItem(label: String, route: String, icon: @Composable () -> Unit) {
    NavigationDrawerItem(
      icon = icon,
      label = { Text(label) },
      selected = currentRoute.startsWith(route),
      onClick = { onRouteSelected(route) }
    )
  }

  ModalDrawerSheet {
    Spacer(Modifier.height(12.dp))

    DrawerRouteItem(
      label = stringResource(commonR.string.day_night_cycle),
      route = dayNightCycleRoute
    ) {
      Icon(
        painter = painterResource(commonR.drawable.day_night_cycle),
        contentDescription = stringResource(commonR.string.day_night_cycle)
      )
    }

    DrawerRouteItem(
      label = stringResource(commonR.string.golden_blue_hour),
      route = goldenBlueHourRoute
    ) {
      Icon(
        imageVector = Icons.Filled.PhotoCamera,
        contentDescription = stringResource(commonR.string.golden_blue_hour)
      )
    }

    if (appWidgetManager.isRequestPinAppWidgetSupported) {
      DrawerRouteItem(label = stringResource(commonR.string.new_widget), route = newWidgetRoute) {
        Icon(
          imageVector = Icons.Filled.Widgets,
          contentDescription = stringResource(commonR.string.new_widget)
        )
      }
    }

    DrawerRouteItem(label = stringResource(commonR.string.locations), route = locationsRoute) {
      Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = stringResource(commonR.string.locations)
      )
    }

    DrawerRouteItem(label = stringResource(commonR.string.settings), route = settingsRoute) {
      Icon(
        imageVector = Icons.Filled.Settings,
        contentDescription = stringResource(commonR.string.settings)
      )
    }

    DrawerRouteItem(label = stringResource(commonR.string.about), route = aboutRoute) {
      Icon(
        imageVector = Icons.Filled.Info,
        contentDescription = stringResource(commonR.string.about)
      )
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DayLighterScaffold(navController: NavHostController, onDrawerMenuClick: () -> Unit) {
  Scaffold(
    modifier = Modifier.semantics { testTagsAsResourceId = true },
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) {
    DayLighterNavHost(
      navController = navController,
      onDrawerMenuClick = onDrawerMenuClick,
      modifier =
        Modifier.padding(it).consumeWindowInsets(it).windowInsetsPadding(WindowInsets.safeDrawing)
    )
  }
}

@Composable
private fun DayLighterNavHost(
  navController: NavHostController,
  onDrawerMenuClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  fun navigateToAddLocation() {
    navController.navigate(route = locationRoute, navOptions = nextLevelNavOptions())
  }

  fun navigateToEditLocation(locationId: Long) {
    navController.navigate(route = "$locationRoute/$locationId", navOptions = nextLevelNavOptions())
  }

  fun navigateToSettingsOnEnableGeocodingClick() {
    navController.navigate(
      route = settingsNavigationRoute(autoShowEmailDialog = true),
      navOptions = nextLevelNavOptions()
    )
  }

  val context = LocalContext.current
  val dayNightCycleDeepLinkUriPattern = context.dayNightCycleDeepLinkPattern()
  val goldenBlueHourDeepLinkUriPattern = context.goldenBlueHourDeepLinkPattern()
  val addLocationDeepLinkUriPattern = context.addLocationDeepLinkPattern()
  val widgetLocationDeepLinkUriPattern = context.widgetLocationDeepLinkPattern()

  NavHost(
    navController = navController,
    startDestination = dayNightCycleRoute,
    modifier = modifier
  ) {
    composable(
      route = dayNightCycleRoute,
      deepLinks = listOf(navDeepLink { uriPattern = dayNightCycleDeepLinkUriPattern })
    ) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        chartMode = DayPeriodChartMode.DAY_NIGHT_CYCLE,
        onDrawerMenuClick = onDrawerMenuClick,
        onAddLocationClick = ::navigateToAddLocation,
        onEditLocationClick = ::navigateToEditLocation,
      )
    }

    composable(
      route = goldenBlueHourRoute,
      deepLinks = listOf(navDeepLink { uriPattern = goldenBlueHourDeepLinkUriPattern })
    ) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        chartMode = DayPeriodChartMode.GOLDEN_BLUE_HOUR,
        onDrawerMenuClick = onDrawerMenuClick,
        onAddLocationClick = ::navigateToAddLocation,
        onEditLocationClick = ::navigateToEditLocation,
      )
    }

    composable(
      route = newWidgetRoute,
      deepLinks = listOf(navDeepLink { uriPattern = widgetLocationDeepLinkUriPattern })
    ) {
      WidgetLocationRoute(
        modifier = Modifier.fillMaxSize(),
        onAddLocationClick = ::navigateToAddLocation,
        onDrawerMenuClick = onDrawerMenuClick,
      )
    }

    composable(route = locationsRoute) {
      LocationsRoute(
        modifier = Modifier.fillMaxSize(),
        onAddLocationClick = ::navigateToAddLocation,
        onEditLocationClick = ::navigateToEditLocation,
        onDrawerMenuClick = onDrawerMenuClick,
      )
    }

    settingsComposable(
      modifier = Modifier.fillMaxSize(),
      onBackClick = navController::popBackStack,
      onDrawerMenuClick = onDrawerMenuClick,
    )

    composable(route = aboutRoute) {
      AboutScreen(modifier = Modifier.fillMaxSize(), onDrawerMenuClick = onDrawerMenuClick)
    }

    composable(
      route = locationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = addLocationDeepLinkUriPattern })
    ) {
      LocationRoute(
        modifier = Modifier.fillMaxSize(),
        onBackClick = navController::popBackStack,
        onEnableGeocodingClick = ::navigateToSettingsOnEnableGeocodingClick
      )
    }

    composable(
      route = editLocationRoute,
      arguments = listOf(navArgument(locationIdParam) { type = NavType.LongType })
    ) {
      LocationRoute(
        modifier = Modifier.fillMaxSize(),
        onBackClick = navController::popBackStack,
        onEnableGeocodingClick = ::navigateToSettingsOnEnableGeocodingClick
      )
    }
  }
}

private fun NavOptionsBuilder.fadeInAndOut() {
  anim {
    enter = android.R.anim.fade_in
    exit = android.R.anim.fade_out
    popEnter = android.R.anim.fade_in
    popExit = android.R.anim.fade_out
  }
}

private fun NavController.topLevelNavOptions(
  saveCurrentRouteState: Boolean,
  restoreDestinationState: Boolean
): NavOptions = navOptions {
  popUpTo(graph.findStartDestination().id) { saveState = saveCurrentRouteState }
  launchSingleTop = true
  this.restoreState = restoreDestinationState
  fadeInAndOut()
}

private fun nextLevelNavOptions(): NavOptions = navOptions {
  launchSingleTop = true
  fadeInAndOut()
}
