package com.trm.daylighter.core.common.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions

fun NavController.topLevelNavOptions(
  saveCurrentRouteState: Boolean,
  restoreDestinationState: Boolean,
): NavOptions = navOptions {
  popUpTo(graph.findStartDestination().id) { saveState = saveCurrentRouteState }
  launchSingleTop = true
  this.restoreState = restoreDestinationState
  fadeInAndOut()
}

fun nextLevelNavOptions(): NavOptions = navOptions {
  launchSingleTop = true
  fadeInAndOut()
}

fun NavOptionsBuilder.fadeInAndOut() {
  anim {
    enter = android.R.anim.fade_in
    exit = android.R.anim.fade_out
    popEnter = android.R.anim.fade_in
    popExit = android.R.anim.fade_out
  }
}
