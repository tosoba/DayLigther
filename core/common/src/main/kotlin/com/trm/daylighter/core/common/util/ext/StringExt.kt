package com.trm.daylighter.core.common.util.ext

import android.util.Patterns
import androidx.annotation.StringRes
import com.trm.daylighter.core.common.R

@StringRes
fun String.isValidEmail(): Int? =
  when {
    isBlank() -> R.string.email_empty_error
    !Patterns.EMAIL_ADDRESS.matcher(this).matches() -> R.string.invalid_email_error
    else -> null
  }
