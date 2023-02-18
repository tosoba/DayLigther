package com.trm.daylighter.core.network.di

import com.trm.daylighter.core.network.retrofit.DaylighterApi
import dagger.Component
import fr.dudie.nominatim.client.JsonNominatimClient
import javax.inject.Singleton
import org.apache.http.impl.client.CloseableHttpClient

@Singleton
@Component(modules = [NetworkModule::class])
interface NetworkTestComponent {
  fun daylighterApi(): DaylighterApi

  fun nominatimHttpClient(): CloseableHttpClient

  fun jsonNominatimClient(): JsonNominatimClient
}
