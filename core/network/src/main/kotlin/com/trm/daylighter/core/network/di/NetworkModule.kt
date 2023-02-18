package com.trm.daylighter.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trm.daylighter.core.common.BuildConfig
import com.trm.daylighter.core.network.DaylighterNetworkDataSource
import com.trm.daylighter.core.network.retrofit.DaylighterApi
import com.trm.daylighter.core.network.retrofit.DaylighterRetrofitDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.dudie.nominatim.client.JsonNominatimClient
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
  @Binds abstract fun DaylighterRetrofitDataSource.binds(): DaylighterNetworkDataSource

  companion object {
    @Provides @Singleton fun networkJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun daylighterApi(networkJson: Json): DaylighterApi =
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

    @Provides
    @Singleton
    fun nominatimHttpClient(): CloseableHttpClient = HttpClients.createDefault()

    @Provides
    @Singleton
    fun jsonNominatimClient(httpClient: CloseableHttpClient): JsonNominatimClient =
      JsonNominatimClient(
        "https://nominatim.openstreetmap.org/",
        httpClient,
        "therealmerengue@gmail.com"
      )
  }
}
