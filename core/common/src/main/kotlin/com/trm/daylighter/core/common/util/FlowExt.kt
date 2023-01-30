package com.trm.daylighter.core.common.util

import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

fun <A, B : Any, R> Flow<A>.withLatestFrom(
  other: Flow<B>,
  transform: suspend (A, B) -> R
): Flow<R> = flow {
  coroutineScope {
    val latestB = AtomicReference<B?>()
    val outerScope = this
    launch {
      try {
        other.collect { latestB.set(it) }
      } catch (e: CancellationException) {
        outerScope.cancel(e) // cancel outer scope on cancellation exception, too
      }
    }
    collect { a: A -> latestB.get()?.let { b -> emit(transform(a, b)) } }
  }
}
