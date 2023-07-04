package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerMenuTopAppBar(
  modifier: Modifier = Modifier,
  title: String,
  onDrawerMenuClick: () -> Unit,
  trailing: @Composable () -> Unit = {},
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    DrawerMenuIconButton(onClick = onDrawerMenuClick)

    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().basicMarquee().padding(horizontal = 10.dp)
    )

    trailing()
  }
}
