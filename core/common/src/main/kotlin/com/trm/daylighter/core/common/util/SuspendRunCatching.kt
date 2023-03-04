package com.trm.daylighter.core.common.util

import kotlinx.coroutines.CancellationException
import timber.log.Timber

suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> =
  try {
    Result.success(block())
  } catch (cancellationException: CancellationException) {
    throw cancellationException
  } catch (exception: Exception) {
    Timber.tag("suspendRunCatching").e(exception)
    Result.failure(exception)
  }
