package com.trm.daylighter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.trm.daylighter.ui.DaylighterMainContent
import com.trm.daylighter.ui.theme.DayLighterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { DayLighterTheme { DaylighterMainContent() } }
  }
}
