package com.trm.daylighter.core.domain.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: DayLighterDispatchers)

enum class DayLighterDispatchers {
  IO,
  DEFAULT,
}
