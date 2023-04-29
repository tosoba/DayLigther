package com.trm.daylighter.widget.defaultlocation.chart

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.widget.defaultlocation.LoadableLocationSunriseSunsetChangeSerializer
import java.io.File

object DefaultLocationChartWidgetStateDefinition :
  GlanceStateDefinition<Loadable<LocationSunriseSunsetChange>> {
  private const val DATA_STORE_FILENAME = "DefaultLocationChartWidget"

  private val Context.datastore by dataStore(DATA_STORE_FILENAME, LoadableLocationSunriseSunsetChangeSerializer)

  override suspend fun getDataStore(
    context: Context,
    fileKey: String
  ): DataStore<Loadable<LocationSunriseSunsetChange>> = context.datastore

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME)
}
