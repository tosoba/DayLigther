package com.trm.daylighter.core.network.di

import com.trm.daylighter.core.network.retrofit.DaylighterApi
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface NetworkTestComponent {
  fun daylighterApi(): DaylighterApi
}
