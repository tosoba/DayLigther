package com.trm.daylighter.data.util

import android.util.Log
import kotlinx.coroutines.CancellationException

internal suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> =
  try {
    Result.success(block())
  } catch (cancellationException: CancellationException) {
    throw cancellationException
  } catch (exception: Exception) {
    Log.e(
      "suspendRunCatching",
      "Failed to evaluate a suspendRunCatchingBlock. Returning failure Result",
      exception
    )
    Result.failure(exception)
  }
