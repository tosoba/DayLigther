package com.trm.daylighter.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trm.daylighter.core.network.BuildConfig
import com.trm.daylighter.core.network.SunriseSunsetNetworkDataSource
import com.trm.daylighter.core.network.retrofit.NominatimEndpoint
import com.trm.daylighter.core.network.retrofit.SunriseSunsetEndpoint
import com.trm.daylighter.core.network.retrofit.SunriseSunsetRetrofitDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
  @Binds
  abstract fun bindsSunriseSunsetRetrofitDataSource(
    source: SunriseSunsetRetrofitDataSource
  ): SunriseSunsetNetworkDataSource

  companion object {
    @Provides
    @Singleton
    fun sunriseSunsetEndpoint(
      okHttpClient: OkHttpClient,
      networkJson: Json
    ): SunriseSunsetEndpoint =
      Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(
          @OptIn(ExperimentalSerializationApi::class)
          networkJson.asConverterFactory("application/json".toMediaType())
        )
        .baseUrl(SunriseSunsetEndpoint.BASE_URL)
        .build()
        .create(SunriseSunsetEndpoint::class.java)

    @Provides
    @Singleton
    fun nominatimEndpoint(okHttpClient: OkHttpClient, networkJson: Json): NominatimEndpoint =
      Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(
          @OptIn(ExperimentalSerializationApi::class)
          networkJson.asConverterFactory("application/json".toMediaType())
        )
        .baseUrl(NominatimEndpoint.BASE_URL)
        .build()
        .create(NominatimEndpoint::class.java)

    @Provides
    @Singleton
    fun networkJson(): Json = Json {
      ignoreUnknownKeys = true
      @OptIn(ExperimentalSerializationApi::class)
      explicitNulls = false
    }

    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient =
      OkHttpClient.Builder()
        .addInterceptor(
          HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
          }
        )
        .build()
  }
}
