package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DrawerMenuTopAppBar(
  title: String,
  onDrawerMenuClick: () -> Unit,
  trailing: @Composable RowScope.() -> Unit = {},
) {
  CenterAlignedTopAppBar(
    navigationIcon = { DrawerMenuIconButton(onClick = onDrawerMenuClick) },
    title = {
      Text(
        text = title,
        style = appBarTextStyle(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().basicMarquee().padding(horizontal = 10.dp)
      )
    },
    actions = trailing
  )
}

@Composable
fun appBarTextStyle(): TextStyle =
  MaterialTheme.typography.titleLarge.copy(
    fontWeight = FontWeight.SemiBold,
    shadow =
      Shadow(
        color = MaterialTheme.colorScheme.inverseOnSurface,
        offset = Offset(1f, 1f),
        blurRadius = 1f
      ),
  )
