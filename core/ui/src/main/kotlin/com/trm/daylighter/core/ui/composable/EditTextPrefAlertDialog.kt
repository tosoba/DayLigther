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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.DialogProperties

@Composable
fun EditTextPrefAlertDialog(
  isShowing: Boolean,
  hide: () -> Unit,
  prefValue: String,
  editPref: (String) -> Unit,
  dialogTitle: String?,
  dialogMessage: String?,
  editTextPlaceholder: String?,
  validateValue: (String) -> String?,
  onValueChange: (String) -> Unit = {},
  dialogBackgroundColor: Color = MaterialTheme.colorScheme.background,
) {
  var textValue by rememberSaveable(prefValue) { mutableStateOf(prefValue) }
  var textValueChanged by rememberSaveable { mutableStateOf(false) }
  var validationMsg: String? by rememberSaveable { mutableStateOf(null) }

  if (isShowing) {
    LaunchedEffect(Unit) { if (!textValueChanged) textValue = prefValue }

    AlertDialog(
      modifier = Modifier.fillMaxWidth(0.9f),
      onDismissRequest = {
        textValueChanged = false
        textValue = prefValue
        hide()
      },
      title = { DialogHeader(dialogTitle = dialogTitle, dialogMessage = dialogMessage) },
      text = {
        OutlinedTextField(
          value = textValue,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          placeholder = editTextPlaceholder?.let { { Text(text = it) } },
          singleLine = true,
          isError = validationMsg != null,
          supportingText = {
            AnimatedVisibility(
              visible = validationMsg != null,
              enter = fadeIn(),
              exit = fadeOut()
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
          }
        )
      },
      confirmButton = {
        TextButton(
          modifier = Modifier.padding(end = 16.dp),
          onClick = {
            validationMsg = validateValue(textValue)
            if (validationMsg == null) {
              editPref(textValue)
              textValueChanged = false
              hide()
            }
          }
        ) {
          Text(
            text = stringResource(android.R.string.ok),
            style = MaterialTheme.typography.bodyLarge
          )
        }
      },
      dismissButton = {
        TextButton(
          modifier = Modifier.padding(end = 16.dp),
          onClick = {
            textValueChanged = false
            validationMsg = null
            hide()
          }
        ) {
          Text(
            text = stringResource(android.R.string.cancel),
            style = MaterialTheme.typography.bodyLarge
          )
        }
      },
      properties = DialogProperties(usePlatformDefaultWidth = false),
      containerColor = dialogBackgroundColor,
    )
  }
}

@Composable
private fun DialogHeader(dialogTitle: String?, dialogMessage: String?) {
  Column(modifier = Modifier.padding(16.dp)) {
    if (dialogTitle != null) {
      Text(
        text = dialogTitle,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
      )
    }
    if (dialogMessage != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = dialogMessage, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
