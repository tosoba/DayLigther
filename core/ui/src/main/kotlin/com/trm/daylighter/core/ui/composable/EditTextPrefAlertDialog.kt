package com.trm.daylighter.core.ui.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.ui.local.LocalHeightSizeClass
import com.trm.daylighter.core.ui.util.rememberKeyboardOpen

@Composable
fun EditTextPrefAlertDialog(
  isShowing: Boolean,
  hide: () -> Unit,
  prefValue: String,
  editPref: (String) -> Unit,
  title: @Composable () -> Unit,
  editTextPlaceholder: String?,
  validateValue: (String) -> String?,
  modifier: Modifier = Modifier,
  onValueChange: (String) -> Unit = {},
  dialogBackgroundColor: Color = MaterialTheme.colorScheme.background,
) {
  var textValue by rememberSaveable(prefValue) { mutableStateOf(prefValue) }
  var textValueChanged by rememberSaveable { mutableStateOf(false) }
  var validationMsg: String? by rememberSaveable { mutableStateOf(null) }

  val keyboardOpen = rememberKeyboardOpen()

  if (isShowing) {
    LaunchedEffect(Unit) { if (!textValueChanged) textValue = prefValue }

    AlertDialog(
      modifier = modifier,
      onDismissRequest = {
        textValueChanged = false
        textValue = prefValue
        hide()
      },
      title = title,
      text = {
        TextField(
          value = textValue,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          placeholder = editTextPlaceholder?.let { { Text(text = it) } },
          singleLine = true,
          isError = validationMsg != null,
          supportingText = {
            AnimatedVisibility(
              visible = validationMsg != null,
              enter = fadeIn(),
              exit = fadeOut(),
            ) {
              Text(text = validationMsg ?: "")
            }
          },
          onValueChange = {
            val trimmed = it.trim()
            textValue = trimmed
            validationMsg = null
            textValueChanged = true
            onValueChange(trimmed)
          },
        )
      },
      confirmButton = {
        AnimatedVisibility(
          visible =
            !keyboardOpen.value || LocalHeightSizeClass.current != WindowHeightSizeClass.Compact
        ) {
          TextButton(
            modifier = Modifier.padding(end = 16.dp),
            onClick = {
              validationMsg = validateValue(textValue)
              if (validationMsg == null) {
                editPref(textValue)
                textValueChanged = false
                hide()
              }
            },
          ) {
            Text(
              text = stringResource(android.R.string.ok),
              style = MaterialTheme.typography.bodyLarge,
            )
          }
        }
      },
      dismissButton = {
        AnimatedVisibility(
          visible =
            !keyboardOpen.value || LocalHeightSizeClass.current != WindowHeightSizeClass.Compact
        ) {
          TextButton(
            modifier = Modifier.padding(end = 16.dp),
            onClick = {
              textValueChanged = false
              validationMsg = null
              hide()
            },
          ) {
            Text(
              text = stringResource(android.R.string.cancel),
              style = MaterialTheme.typography.bodyLarge,
            )
          }
        }
      },
      containerColor = dialogBackgroundColor,
    )
  }
}

@Composable
fun AlertDialogHeader(dialogTitle: String?, dialogMessage: String?, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    if (dialogTitle != null) {
      Text(
        text = dialogTitle,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
      )
    }
    if (dialogMessage != null) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = dialogMessage, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
