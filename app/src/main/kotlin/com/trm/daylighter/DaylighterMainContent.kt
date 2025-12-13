package com.trm.daylighter

import android.appwidget.AppWidgetManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
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
import com.trm.daylighter.core.common.navigation.nextLevelNavOptions
import com.trm.daylighter.core.common.navigation.topLevelNavOptions
import com.trm.daylighter.core.common.navigation.widgetLocationDeepLinkPattern
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.ui.composable.DayPeriodChart
import com.trm.daylighter.core.ui.model.DayPeriodChartMode
import com.trm.daylighter.core.ui.model.asStable
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
fun DayLighterMainContent() {
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val currentRoute = navController.currentRoute()

  DayLighterNavigationDrawer(
    drawerState = drawerState,
    visible = !currentRoute.startsWith(locationRoute),
    drawerContent = {
      val scope = rememberCoroutineScope()
      DayLighterDrawerContent(
        currentRoute = currentRoute,
        onRouteSelected = { destinationRoute ->
          scope.launch { drawerState.close() }
          if (destinationRoute != currentRoute) {
            navController.navigate(
              route = destinationRoute,
              navOptions =
                navController.topLevelNavOptions(
                  saveCurrentRouteState = !currentRoute.startsWith(widgetLocationRoute),
                  restoreDestinationState = !destinationRoute.startsWith(widgetLocationRoute),
                ),
            )
          }
        },
      )
    },
  ) {
    DayLighterScaffold(navController = navController, drawerState = drawerState)
  }
}

@Composable
private fun NavController.currentRoute(): String =
  currentBackStackEntryAsState().value?.destination?.route ?: dayNightCycleRoute

@Composable
private fun DayLighterNavigationDrawer(
  modifier: Modifier = Modifier,
  drawerState: DrawerState,
  visible: Boolean,
  drawerContent: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  if (usingPermanentNavigationDrawer && visible) {
    PermanentNavigationDrawer(modifier = modifier, drawerContent = drawerContent, content = content)
  } else {
    ModalNavigationDrawer(
      modifier = modifier,
      gesturesEnabled = drawerState.isOpen,
      drawerState = drawerState,
      drawerContent = drawerContent,
      content = content,
    )
  }
}

@Composable
private fun DayLighterDrawerContent(
  currentRoute: String,
  onRouteSelected: (route: String) -> Unit,
) {
  val context = LocalContext.current
  val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

  @Composable
  fun DrawerRouteItem(label: String, route: String, icon: @Composable () -> Unit) {
    NavigationDrawerItem(
      icon = icon,
      label = { Text(label) },
      selected = currentRoute.startsWith(route),
      onClick = { onRouteSelected(route) },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }

  ModalDrawerSheet(
    windowInsets =
      WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
  ) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      Spacer(modifier = Modifier.height(16.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.surfaceVariant),
      ) {
        DayPeriodChart(change = Empty.asStable(), modifier = Modifier.size(96.dp))

        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(modifier = Modifier.width(16.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      DrawerRouteItem(
        label = stringResource(commonR.string.day_night_cycle),
        route = dayNightCycleRoute,
      ) {
        Icon(
          painter = painterResource(commonR.drawable.day_night_cycle),
          contentDescription = stringResource(commonR.string.day_night_cycle),
        )
      }

      DrawerRouteItem(
        label = stringResource(commonR.string.golden_blue_hour),
        route = goldenBlueHourRoute,
      ) {
        Icon(
          imageVector = Icons.Filled.PhotoCamera,
          contentDescription = stringResource(commonR.string.golden_blue_hour),
        )
      }

      if (appWidgetManager.isRequestPinAppWidgetSupported) {
        DrawerRouteItem(
          label = stringResource(commonR.string.widgets),
          route = widgetLocationRoute,
        ) {
          Icon(
            imageVector = Icons.Filled.Widgets,
            contentDescription = stringResource(commonR.string.widgets),
          )
        }
      }

      DrawerRouteItem(label = stringResource(commonR.string.locations), route = locationsRoute) {
        Icon(
          imageVector = Icons.Filled.LocationOn,
          contentDescription = stringResource(commonR.string.locations),
        )
      }

      DrawerRouteItem(label = stringResource(commonR.string.settings), route = settingsRoute) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = stringResource(commonR.string.settings),
        )
      }

      DrawerRouteItem(label = stringResource(commonR.string.about), route = aboutRoute) {
        Icon(
          imageVector = Icons.Filled.Info,
          contentDescription = stringResource(commonR.string.about),
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun DayLighterScaffold(navController: NavHostController, drawerState: DrawerState) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) {
    DayLighterNavHost(
      navController = navController,
      drawerState = drawerState,
      modifier =
        Modifier.padding(it).consumeWindowInsets(it).windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}

@Composable
private fun DayLighterNavHost(
  navController: NavHostController,
  drawerState: DrawerState,
  modifier: Modifier = Modifier,
) {
  fun navigateToNewLocation() {
    navController.navigate(route = locationRoute, navOptions = nextLevelNavOptions())
  }

  fun navigateToEditLocation(locationId: Long) {
    navController.navigate(route = "$locationRoute/$locationId", navOptions = nextLevelNavOptions())
  }

  val scope = rememberCoroutineScope()

  fun onDrawerMenuClick() {
    scope.launch { with(drawerState) { if (isOpen) close() else open() } }
  }

  val currentRoute = navController.currentRoute()
  BackHandler(enabled = !currentRoute.startsWith(locationRoute) && drawerState.isOpen) {
    scope.launch { drawerState.close() }
  }

  val context = LocalContext.current
  val dayNightCycleDeepLinkUriPattern = context.dayNightCycleDeepLinkPattern()
  val goldenBlueHourDeepLinkUriPattern = context.goldenBlueHourDeepLinkPattern()
  val newLocationDeepLinkUriPattern = context.newLocationDeepLinkPattern()
  val widgetLocationDeepLinkUriPattern = context.widgetLocationDeepLinkPattern()

  NavHost(
    navController = navController,
    startDestination =
      when (LocalActivity.current?.intent?.data?.pathSegments?.firstOrNull()) {
        GOLDEN_BLUE_HOUR_PATH_SEGMENT -> goldenBlueHourRoute
        WIDGET_LOCATION_PATH_SEGMENT -> widgetLocationRoute
        else -> dayNightCycleRoute
      },
    modifier = modifier,
  ) {
    composable(
      route = dayNightCycleRoute,
      deepLinks = listOf(navDeepLink { uriPattern = dayNightCycleDeepLinkUriPattern }),
    ) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        chartMode = DayPeriodChartMode.DAY_NIGHT_CYCLE,
        onDrawerMenuClick = ::onDrawerMenuClick,
        onNewLocationClick = ::navigateToNewLocation,
        onEditLocationClick = ::navigateToEditLocation,
      )
    }

    composable(
      route = goldenBlueHourRoute,
      deepLinks = listOf(navDeepLink { uriPattern = goldenBlueHourDeepLinkUriPattern }),
    ) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        chartMode = DayPeriodChartMode.GOLDEN_BLUE_HOUR,
        onDrawerMenuClick = ::onDrawerMenuClick,
        onNewLocationClick = ::navigateToNewLocation,
        onEditLocationClick = ::navigateToEditLocation,
      )
    }

    composable(
      route = widgetLocationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = widgetLocationDeepLinkUriPattern }),
    ) {
      WidgetLocationRoute(
        modifier = Modifier.fillMaxSize(),
        onNewLocationClick = ::navigateToNewLocation,
        onDrawerMenuClick = ::onDrawerMenuClick,
      )
    }

    composable(route = locationsRoute) {
      LocationsRoute(
        modifier = Modifier.fillMaxSize(),
        onNewLocationClick = ::navigateToNewLocation,
        onEditLocationClick = ::navigateToEditLocation,
        onDrawerMenuClick = ::onDrawerMenuClick,
      )
    }

    composable(route = settingsRoute) {
      SettingsRoute(modifier = Modifier.fillMaxSize(), onDrawerMenuClick = ::onDrawerMenuClick)
    }

    composable(route = aboutRoute) {
      AboutScreen(modifier = Modifier.fillMaxSize(), onDrawerMenuClick = ::onDrawerMenuClick)
    }

    composable(
      route = locationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = newLocationDeepLinkUriPattern }),
    ) {
      LocationRoute(modifier = Modifier.fillMaxSize(), onBackClick = navController::popBackStack)
    }

    composable(
      route = editLocationRoute,
      arguments = listOf(navArgument(locationIdParam) { type = NavType.LongType }),
    ) {
      LocationRoute(modifier = Modifier.fillMaxSize(), onBackClick = navController::popBackStack)
    }
  }
}
