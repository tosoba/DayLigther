package com.trm.daylighter.core.common.di

import android.content.Context
import com.trm.daylighter.core.common.util.ext.getCurrentUserLocation
import com.trm.daylighter.core.domain.model.LatLng
import com.trm.daylighter.core.domain.usecase.GetCurrentUserLatLngUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
  @Provides
  fun getCurrentUserLocation(@ApplicationContext context: Context) = GetCurrentUserLatLngUseCase {
    try {
      context.getCurrentUserLocation()?.let { location ->
        LatLng(latitude = location.latitude, longitude = location.longitude)
      }
    } catch (ex: Exception) {
      Timber.tag("USER_LOCATION_GET").e(ex)
      null
    }
  }
}
