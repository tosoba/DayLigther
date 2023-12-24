package com.trm.daylighter

import android.appwidget.AppWidgetManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.navigation.GOLDEN_BLUE_HOUR_PATH_SEGMENT
import com.trm.daylighter.core.common.navigation.WIDGET_LOCATION_PATH_SEGMENT
import com.trm.daylighter.core.common.navigation.dayNightCycleDeepLinkPattern
import com.trm.daylighter.core.common.navigation.goldenBlueHourDeepLinkPattern
import com.trm.daylighter.core.common.navigation.newLocationDeepLinkPattern
import com.trm.daylighter.core.common.navigation.widgetLocationDeepLinkPattern
import com.trm.daylighter.core.common.util.ext.getActivity
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer
import com.trm.daylighter.feature.about.AboutScreen
import com.trm.daylighter.feature.about.aboutRoute
import com.trm.daylighter.feature.day.DayRoute
import com.trm.daylighter.feature.day.dayNightCycleRoute
import com.trm.daylighter.feature.day.goldenBlueHourRoute
import com.trm.daylighter.feature.location.*
import com.trm.daylighter.feature.locations.LocationsRoute
import com.trm.daylighter.feature.locations.locationsRoute
import com.trm.daylighter.feature.settings.SettingsRoute
import com.trm.daylighter.feature.settings.settingsRoute
import com.trm.daylighter.feature.widget.location.WidgetLocationRoute
import com.trm.daylighter.feature.widget.location.widgetLocationRoute
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

  BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

  fun onDrawerMenuClick() {
    scope.launch { with(drawerState) { if (isOpen) close() else open() } }
  }

  DayLighterNavigationDrawer(
    modifier = Modifier.statusBarsPadding(),
    drawerState = drawerState,
    visible = !currentRoute.startsWith(locationRoute),
    drawerContent = {
      DayLighterDrawerContent(
        currentRoute = currentRoute,
        onRouteSelected = { destinationRoute ->
          if (destinationRoute != currentRoute) {
            navController.navigate(
              route = destinationRoute,
              navOptions =
                navController.topLevelNavOptions(
                  saveCurrentRouteState = !currentRoute.startsWith(widgetLocationRoute),
                  restoreDestinationState = !destinationRoute.startsWith(widgetLocationRoute)
                )
            )
          }
          scope.launch { drawerState.close() }
        }
      )
    }
  ) {
    DayLighterScaffold(navController = navController, onDrawerMenuClick = ::onDrawerMenuClick)
  }
}

@Composable
fun DayLighterNavigationDrawer(
  modifier: Modifier = Modifier,
  drawerState: DrawerState,
  visible: Boolean,
  drawerContent: @Composable () -> Unit,
  content: @Composable () -> Unit
) {
  if (usingPermanentNavigationDrawer && visible) {
    PermanentNavigationDrawer(modifier = modifier, drawerContent = drawerContent, content = content)
  } else {
    ModalNavigationDrawer(
      modifier = modifier,
      gesturesEnabled = false,
      drawerState = drawerState,
      drawerContent = drawerContent,
      content = content
    )
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
      DrawerRouteItem(label = stringResource(commonR.string.widgets), route = widgetLocationRoute) {
        Icon(
          imageVector = Icons.Filled.Widgets,
          contentDescription = stringResource(commonR.string.widgets)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayLighterScaffold(navController: NavHostController, onDrawerMenuClick: () -> Unit) {
  Scaffold(
    modifier = Modifier.statusBarsPadding(),
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
  fun navigateToNewLocation() {
    navController.navigate(route = locationRoute, navOptions = nextLevelNavOptions())
  }

  fun navigateToEditLocation(locationId: Long) {
    navController.navigate(route = "$locationRoute/$locationId", navOptions = nextLevelNavOptions())
  }

  val context = LocalContext.current
  val dayNightCycleDeepLinkUriPattern = context.dayNightCycleDeepLinkPattern()
  val goldenBlueHourDeepLinkUriPattern = context.goldenBlueHourDeepLinkPattern()
  val newLocationDeepLinkUriPattern = context.newLocationDeepLinkPattern()
  val widgetLocationDeepLinkUriPattern = context.widgetLocationDeepLinkPattern()

  NavHost(
    navController = navController,
    startDestination =
      when (context.getActivity()?.intent?.data?.pathSegments?.firstOrNull()) {
        GOLDEN_BLUE_HOUR_PATH_SEGMENT -> goldenBlueHourRoute
        WIDGET_LOCATION_PATH_SEGMENT -> widgetLocationRoute
        else -> dayNightCycleRoute
      },
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
        onNewLocationClick = ::navigateToNewLocation,
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
        onNewLocationClick = ::navigateToNewLocation,
        onEditLocationClick = ::navigateToEditLocation,
      )
    }

    composable(
      route = widgetLocationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = widgetLocationDeepLinkUriPattern })
    ) {
      WidgetLocationRoute(
        modifier = Modifier.fillMaxSize(),
        onNewLocationClick = ::navigateToNewLocation,
        onDrawerMenuClick = onDrawerMenuClick,
      )
    }

    composable(route = locationsRoute) {
      LocationsRoute(
        modifier = Modifier.fillMaxSize(),
        onNewLocationClick = ::navigateToNewLocation,
        onEditLocationClick = ::navigateToEditLocation,
        onDrawerMenuClick = onDrawerMenuClick,
      )
    }

    composable(route = settingsRoute) {
      SettingsRoute(modifier = Modifier.fillMaxSize(), onDrawerMenuClick = onDrawerMenuClick)
    }

    composable(route = aboutRoute) {
      AboutScreen(modifier = Modifier.fillMaxSize(), onDrawerMenuClick = onDrawerMenuClick)
    }

    composable(
      route = locationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = newLocationDeepLinkUriPattern })
    ) {
      LocationRoute(modifier = Modifier.fillMaxSize(), onBackClick = navController::popBackStack)
    }

    composable(
      route = editLocationRoute,
      arguments = listOf(navArgument(locationIdParam) { type = NavType.LongType })
    ) {
      LocationRoute(modifier = Modifier.fillMaxSize(), onBackClick = navController::popBackStack)
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
