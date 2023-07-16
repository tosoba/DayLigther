package com.trm.daylighter.feature.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.local.usingPermanentNavigationDrawer

const val aboutRoute = "about_route"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier, onDrawerMenuClick: () -> Unit) {
  Column(modifier = modifier) {
    DayLighterTopAppBar(
      title = stringResource(commonR.string.about),
      navigationIcon = {
        if (!usingPermanentNavigationDrawer) {
          DrawerMenuIconButton(onClick = onDrawerMenuClick)
        }
      }
    )
    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
      Text(text = stringResource(commonR.string.about), modifier = Modifier.align(Alignment.Center))
    }
  }
}
