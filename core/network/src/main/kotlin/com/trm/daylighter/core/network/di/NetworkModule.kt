package com.trm.daylighter.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trm.daylighter.core.common.BuildConfig
import com.trm.daylighter.core.network.retrofit.DaylighterApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides @Singleton fun networkJson(): Json = Json { ignoreUnknownKeys = true }

  @Provides
  @Singleton
  fun daylighterEndpoints(networkJson: Json): DaylighterApi =
    Retrofit.Builder()
      .client(
        OkHttpClient.Builder()
          .addInterceptor(
            HttpLoggingInterceptor().apply {
              if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
            }
          )
          .build()
      )
      .addConverterFactory(
        @OptIn(ExperimentalSerializationApi::class)
        networkJson.asConverterFactory("application/json".toMediaType())
      )
      .baseUrl(DaylighterApi.BASE_URL)
      .build()
      .create(DaylighterApi::class.java)
}
