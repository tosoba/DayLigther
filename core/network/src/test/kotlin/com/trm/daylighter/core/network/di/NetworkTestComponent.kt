package com.trm.daylighter.core.network.di

import com.trm.daylighter.core.network.retrofit.NominatimEndpoint
import com.trm.daylighter.core.network.retrofit.SunriseSunsetEndpoint
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface NetworkTestComponent {
  fun sunriseSunsetEndpoint(): SunriseSunsetEndpoint

  fun nominatimEndpoint(): NominatimEndpoint
}
