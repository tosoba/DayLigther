package com.trm.daylighter.feature.about

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R as commonR
import com.trm.daylighter.core.common.util.ext.copyToClipboard
import com.trm.daylighter.core.common.util.ext.goToUrlInBrowser
import com.trm.daylighter.core.ui.composable.DayLighterTopAppBar
import com.trm.daylighter.core.ui.composable.DrawerMenuIconButton
import com.trm.daylighter.core.ui.util.usingPermanentNavigationDrawer

const val aboutRoute = "about_route"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
  modifier: Modifier = Modifier,
  onDrawerMenuClick: () -> Unit,
  backHandler: @Composable () -> Unit,
) {
  backHandler()

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
      val context = LocalContext.current

      Text(
        text = stringResource(R.string.repository),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
      )

      ImageTextRow(
        imagePainter = painterResource(R.drawable.github),
        text = stringResource(R.string.github),
        contentDescription = stringResource(R.string.github),
        modifier =
          Modifier.clickable { context.goToUrlInBrowser("https://github.com/tosoba/DayLigther") }
            .padding(horizontal = 20.dp, vertical = 10.dp)
      )

      Text(
        text = stringResource(R.string.credits),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
      )

      ImageTextRow(
        imagePainter = painterResource(R.drawable.open_street_map_creditor),
        text = stringResource(R.string.open_street_map),
        contentDescription = stringResource(R.string.open_street_map),
        modifier =
          Modifier.clickable { context.goToUrlInBrowser("https://www.openstreetmap.org/") }
            .padding(horizontal = 20.dp, vertical = 10.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = stringResource(R.string.support_me),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(horizontal = 20.dp)
      )

      Spacer(modifier = Modifier.height(5.dp))

      Text(
        text = stringResource(R.string.support_me_info),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = 20.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      var currentToast: Toast? = null
      val addressCopied = stringResource(R.string.address_copied)
      fun showMessageOnCopy() {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, addressCopied, Toast.LENGTH_SHORT).apply { show() }
      }

      val bitcoinAddress = stringResource(R.string.bitcoin_address)
      ImageTextRow(
        imagePainter = painterResource(R.drawable.bitcoin),
        text = bitcoinAddress,
        contentDescription = stringResource(R.string.bitcoin),
        modifier =
          Modifier.clickable {
              context.copyToClipboard(text = bitcoinAddress, showMessage = ::showMessageOnCopy)
            }
            .padding(horizontal = 20.dp, vertical = 10.dp)
      )

      val ethereumAddress = stringResource(R.string.ethereum_address)
      ImageTextRow(
        imagePainter = painterResource(R.drawable.ethereum),
        text = ethereumAddress,
        contentDescription = stringResource(R.string.ethereum),
        modifier =
          Modifier.clickable {
              context.copyToClipboard(text = ethereumAddress, showMessage = ::showMessageOnCopy)
            }
            .padding(horizontal = 20.dp, vertical = 10.dp)
      )

      val xrpAddress = stringResource(R.string.xrp_address)
      ImageTextRow(
        imagePainter = painterResource(R.drawable.xrp),
        text = xrpAddress,
        contentDescription = stringResource(R.string.xrp),
        modifier =
          Modifier.clickable {
              context.copyToClipboard(text = xrpAddress, showMessage = ::showMessageOnCopy)
            }
            .padding(horizontal = 20.dp, vertical = 10.dp)
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageTextRow(
  modifier: Modifier = Modifier,
  text: String,
  contentDescription: String? = null,
  imagePainter: Painter
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    Image(
      painter = imagePainter,
      contentDescription = contentDescription,
      modifier = Modifier.size(30.dp)
    )

    Spacer(modifier = Modifier.width(10.dp))

    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE)
    )
  }
}
