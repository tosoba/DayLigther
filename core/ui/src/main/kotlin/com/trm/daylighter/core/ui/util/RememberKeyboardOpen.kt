package com.trm.daylighter.core.ui.util

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun rememberKeyboardOpen(): State<Boolean> {
  val keyboardOpen = remember { mutableStateOf(false) }
  val view = LocalView.current

  DisposableEffect(view) {
    val listener =
      ViewTreeObserver.OnGlobalLayoutListener {
        keyboardOpen.value =
          ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
      }
    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
    onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
  }

  return keyboardOpen
}
