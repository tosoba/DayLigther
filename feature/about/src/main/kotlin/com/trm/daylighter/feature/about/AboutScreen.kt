package com.trm.daylighter.feature.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer

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

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
      Text(text = stringResource(R.string.credits), style = MaterialTheme.typography.headlineSmall)

      Text(
        text = stringResource(R.string.support_me),
        style = MaterialTheme.typography.headlineSmall
      )
    }
  }
}
