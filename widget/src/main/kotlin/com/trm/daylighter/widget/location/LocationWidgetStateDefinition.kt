package com.trm.daylighter.widget.location

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import java.io.File

object LocationWidgetStateDefinition :
  GlanceStateDefinition<Loadable<LocationSunriseSunsetChange>> {
  private const val DATA_STORE_FILENAME = "LocationChartWidget"

  private val Context.datastore: DataStore<Loadable<LocationSunriseSunsetChange>> by
    dataStore(DATA_STORE_FILENAME, LoadableLocationSunriseSunsetChangeSerializer)

  override suspend fun getDataStore(
    context: Context,
    fileKey: String
  ): DataStore<Loadable<LocationSunriseSunsetChange>> = context.datastore

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME)
}
