package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoButtonCard(
  infoText: String,
  actionText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(shape = CardDefaults.shape, shadowElevation = 6.dp, modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(16.dp),
    ) {
      Text(text = infoText, fontSize = 20.sp, textAlign = TextAlign.Center)
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedButton(onClick = onButtonClick) { Text(text = actionText) }
    }
  }
}
