package com.trm.daylighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.trm.daylighter.feature.location.LocationScreen
import com.trm.daylighter.ui.theme.DayLighterTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { DayLighterTheme { LocationScreen(modifier = Modifier.fillMaxSize()) } }
  }
}
