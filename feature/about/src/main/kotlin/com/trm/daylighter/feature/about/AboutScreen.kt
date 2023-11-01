package com.trm.daylighter.feature.about

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.goToUrlInBrowser
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer

const val aboutRoute = "about_route"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
          .padding(vertical = 10.dp)
    ) {
      Text(
        text = stringResource(R.string.credits),
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
      )

      val context = LocalContext.current
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
          Modifier.clickable { context.goToUrlInBrowser("https://www.openstreetmap.org/") }
            .padding(horizontal = 20.dp, vertical = 10.dp)
      ) {
        Image(
          painter = painterResource(R.drawable.open_street_map_creditor),
          contentDescription = stringResource(R.string.open_street_map),
          modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
          text = stringResource(R.string.open_street_map),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.weight(1f).basicMarquee()
        )
      }

      Text(
        text = stringResource(R.string.support_me),
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
      )
    }
  }
}
