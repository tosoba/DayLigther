package com.trm.daylighter.feature.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.util.ext.goToUrlInBrowser
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer
import com.trm.daylighter.core.common.R as commonR

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
      },
    )

    Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
      val context = LocalContext.current

      Spacer(modifier = Modifier.height(12.dp))

      SectionTitle(text = stringResource(R.string.repository))

      Spacer(modifier = Modifier.height(12.dp))

      LinkCard(
        imagePainter = painterResource(R.drawable.github),
        text = stringResource(R.string.github),
        contentDescription = stringResource(R.string.github),
        onClick = { context.goToUrlInBrowser("https://github.com/tosoba/DayLigther") },
      )

      Spacer(modifier = Modifier.height(12.dp))

      SectionTitle(text = stringResource(R.string.credits))

      Spacer(modifier = Modifier.height(12.dp))

      LinkCard(
        imagePainter = painterResource(R.drawable.open_street_map_creditor),
        text = stringResource(R.string.open_street_map),
        contentDescription = stringResource(R.string.open_street_map),
        onClick = { context.goToUrlInBrowser("https://www.openstreetmap.org/") },
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
}

@Composable
private fun LinkCard(
  text: String,
  contentDescription: String? = null,
  imagePainter: Painter,
  onClick: () -> Unit,
) {
  Card(modifier = Modifier.padding(horizontal = 16.dp), onClick = onClick) {
    Spacer(modifier = Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Spacer(modifier = Modifier.width(12.dp))

      Image(
        painter = imagePainter,
        contentDescription = contentDescription,
        modifier = Modifier.size(32.dp),
      )

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE),
      )

      Spacer(modifier = Modifier.width(12.dp))
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}
