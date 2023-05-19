package com.trm.daylighter.core.domain.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: DaylighterDispatchers)

enum class DaylighterDispatchers {
  IO,
  DEFAULT
}
