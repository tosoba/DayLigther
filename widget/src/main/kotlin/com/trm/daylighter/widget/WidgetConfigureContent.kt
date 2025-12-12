package com.trm.daylighter.widget

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trm.daylighter.core.common.navigation.WidgetLocationRouteParams
import com.trm.daylighter.core.common.navigation.WidgetType
import com.trm.daylighter.core.common.navigation.nextLevelNavOptions
import com.trm.daylighter.core.ui.theme.DayLighterTheme
import com.trm.daylighter.feature.location.LocationRoute
import com.trm.daylighter.feature.location.locationRoute
import com.trm.daylighter.feature.widget.location.WidgetLocationRoute
import com.trm.daylighter.feature.widget.location.WidgetLocationViewModel
import com.trm.daylighter.feature.widget.location.widgetLocationRoute

@Composable
internal fun WidgetConfigureContent(
  widgetId: Int,
  widgetType: WidgetType,
  onConfirmEditWidgetLocationClick: () -> Unit,
) {
  DayLighterTheme {
    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      contentColor = MaterialTheme.colorScheme.onBackground,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) {
      val navController = rememberNavController()

      fun navigateToNewLocation() {
        navController.navigate(route = locationRoute, navOptions = nextLevelNavOptions())
      }

      NavHost(
        navController = navController,
        startDestination = "$widgetLocationRoute/${widgetType.name.lowercase()}/$widgetId",
        modifier =
          Modifier.padding(it).consumeWindowInsets(it).windowInsetsPadding(WindowInsets.safeDrawing),
      ) {
        composable(
          route =
            "$widgetLocationRoute/{${WidgetLocationRouteParams.WIDGET_TYPE}}/{${AppWidgetManager.EXTRA_APPWIDGET_ID}}",
          arguments =
            listOf(
              navArgument(WidgetLocationRouteParams.WIDGET_TYPE) { type = NavType.StringType },
              navArgument(AppWidgetManager.EXTRA_APPWIDGET_ID) { type = NavType.StringType },
            ),
        ) {
          val viewModel = hiltViewModel<WidgetLocationViewModel>()
          WidgetLocationRoute(
            modifier = Modifier.fillMaxSize(),
            onNewLocationClick = ::navigateToNewLocation,
            onDrawerMenuClick = null,
            onConfirmEditWidgetLocationClick = {
              viewModel.onConfirmEditWidgetLocationClick()
              onConfirmEditWidgetLocationClick()
            },
          )
        }

        composable(route = locationRoute) {
          LocationRoute(
            modifier = Modifier.fillMaxSize(),
            onBackClick = navController::popBackStack,
          )
        }
      }
    }
  }
}
