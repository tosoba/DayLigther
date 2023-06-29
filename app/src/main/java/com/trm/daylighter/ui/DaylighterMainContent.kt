package com.trm.daylighter.ui

import android.appwidget.AppWidgetManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trm.daylighter.R
import com.trm.daylighter.core.common.navigation.addLocationDeepPattern
import com.trm.daylighter.core.common.navigation.dayDeepLinkPattern
import com.trm.daylighter.core.common.navigation.widgetLocationDeepLinkPattern
import com.trm.daylighter.core.ui.composable.DrawerMenuButton
import com.trm.daylighter.feature.about.AboutScreen
import com.trm.daylighter.feature.about.aboutRoute
import com.trm.daylighter.feature.day.DayRoute
import com.trm.daylighter.feature.day.dayRoute
import com.trm.daylighter.feature.location.*
import com.trm.daylighter.feature.locations.LocationsRoute
import com.trm.daylighter.feature.locations.locationsRoute
import com.trm.daylighter.feature.settings.settingsAutoShowEmailDialogRoute
import com.trm.daylighter.feature.settings.settingsComposable
import com.trm.daylighter.feature.settings.settingsNavigationRoute
import com.trm.daylighter.feature.settings.settingsRoute
import com.trm.daylighter.feature.widget.location.WidgetLocationRoute
import com.trm.daylighter.feature.widget.location.newWidgetRoute
import kotlinx.coroutines.launch

@Composable
private fun NavController.currentRoute(): String =
  currentBackStackEntryAsState().value?.destination?.route ?: dayRoute

@OptIn(ExperimentalMaterial3Api::class)
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
    gesturesEnabled = currentRoute == dayRoute,
    drawerState = drawerState,
    drawerContent = {
      DayLighterDrawerContent(
        currentRoute = navController.currentRoute(),
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
      onDrawerMenuClick = ::onDrawerMenuClick,
      topBar = {
        AnimatedVisibility(
          visible = currentRoute == aboutRoute || currentRoute.startsWith(settingsRoute),
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          CenterAlignedTopAppBar(
            title = {
              Text(
                stringResource(
                  id =
                    when (currentRoute) {
                      aboutRoute -> R.string.about_item
                      settingsRoute,
                      settingsAutoShowEmailDialogRoute -> R.string.settings_item
                      else -> R.string.empty
                    }
                )
              )
            },
            navigationIcon = {
              DrawerMenuButton(onClick = ::onDrawerMenuClick, modifier = Modifier.padding(10.dp))
            }
          )
        }
      }
    )
  }
}

@Composable
private fun DayLighterDrawerContent(
  currentRoute: String,
  onItemClick: (DrawerDestination) -> Unit
) {
  val context = LocalContext.current
  val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

  ModalDrawerSheet {
    Spacer(Modifier.height(12.dp))

    DayLighterDrawerItem(
      destination =
        DrawerDestination(
          route = dayRoute,
          icon = ImageVector.vectorResource(R.drawable.day_night_cycle_drawer_item),
          label = stringResource(R.string.day_night_cycle_item)
        ),
      selected = currentRoute.startsWith(dayRoute),
      onItemClick = onItemClick
    )

    if (appWidgetManager.isRequestPinAppWidgetSupported) {
      DayLighterDrawerItem(
        destination =
          DrawerDestination(
            route = newWidgetRoute,
            icon = Icons.Filled.Widgets,
            label = stringResource(R.string.new_widget_item)
          ),
        selected = currentRoute.startsWith(newWidgetRoute),
        onItemClick = onItemClick
      )
    }

    DayLighterDrawerItem(
      destination =
        DrawerDestination(
          route = locationsRoute,
          icon = Icons.Filled.LocationOn,
          label = stringResource(R.string.locations_item)
        ),
      selected = currentRoute.startsWith(locationsRoute),
      onItemClick = onItemClick
    )

    DayLighterDrawerItem(
      destination =
        DrawerDestination(
          route = settingsRoute,
          icon = Icons.Filled.Settings,
          label = stringResource(R.string.settings_item)
        ),
      selected = currentRoute.startsWith(settingsRoute),
      onItemClick = onItemClick
    )

    DayLighterDrawerItem(
      destination =
        DrawerDestination(
          route = aboutRoute,
          icon = Icons.Filled.Info,
          label = stringResource(R.string.about_item)
        ),
      selected = currentRoute.startsWith(aboutRoute),
      onItemClick = onItemClick
    )
  }
}

@Composable
private fun DayLighterDrawerItem(
  destination: DrawerDestination,
  selected: Boolean = false,
  onItemClick: (DrawerDestination) -> Unit
) {
  NavigationDrawerItem(
    icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
    label = { Text(text = destination.label) },
    selected = selected,
    onClick = { onItemClick(destination) }
  )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
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
  fun NavOptionsBuilder.fadeInAndOut() {
    anim {
      enter = android.R.anim.fade_in
      exit = android.R.anim.fade_out
      popEnter = android.R.anim.fade_in
      popExit = android.R.anim.fade_out
    }
  }

  fun topLevelNavOptions(): NavOptions = navOptions {
    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
    fadeInAndOut()
  }

  fun nextLevelNavOptions(): NavOptions = navOptions {
    launchSingleTop = true
    fadeInAndOut()
  }

  fun navigateToAddLocation() {
    navController.navigate(route = locationRoute, navOptions = nextLevelNavOptions())
  }

  fun navigateToEditLocation(locationId: Long) {
    navController.navigate(route = "$locationRoute/$locationId", navOptions = nextLevelNavOptions())
  }

  fun navigateToSettingsOnEnableGeocodingClick() {
    navController.navigate(
      route = settingsNavigationRoute(autoShowEmailDialog = true),
      navOptions = topLevelNavOptions()
    )
  }

  val context = LocalContext.current
  val dayDeepLinkUri = context.dayDeepLinkPattern()
  val addLocationDeepLinkUri = context.addLocationDeepPattern()
  val widgetLocationDeepLinkUri = context.widgetLocationDeepLinkPattern()

  NavHost(navController = navController, startDestination = dayRoute, modifier = modifier) {
    composable(route = dayRoute, deepLinks = listOf(navDeepLink { uriPattern = dayDeepLinkUri })) {
      DayRoute(
        modifier = Modifier.fillMaxSize(),
        onDrawerMenuClick = onDrawerMenuClick,
        onAddLocationClick = ::navigateToAddLocation,
        onEditLocationClick = ::navigateToEditLocation,
      )
    }

    composable(route = aboutRoute) { AboutScreen(modifier = Modifier.fillMaxSize()) }

    settingsComposable(modifier = Modifier.fillMaxSize())

    composable(
      route = locationRoute,
      deepLinks = listOf(navDeepLink { uriPattern = addLocationDeepLinkUri })
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

    composable(route = locationsRoute) {
      LocationsRoute(
        modifier = Modifier.fillMaxSize(),
        onAddLocationClick = ::navigateToAddLocation,
        onEditLocationClick = ::navigateToEditLocation,
        onDrawerMenuClick = onDrawerMenuClick,
      )
    }

    composable(
      route = newWidgetRoute,
      deepLinks = listOf(navDeepLink { uriPattern = widgetLocationDeepLinkUri })
    ) {
      WidgetLocationRoute(
        modifier = Modifier.fillMaxSize(),
        onAddLocationClick = ::navigateToAddLocation,
        onDrawerMenuClick = onDrawerMenuClick,
      )
    }
  }
}

private data class DrawerDestination(val route: String, val icon: ImageVector, val label: String)
