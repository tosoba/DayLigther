package com.trm.daylighter.widget.list.clock

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import com.trm.daylighter.widget.list.LoadableLocationSunriseSunsetChangeListSerializer
import java.io.File

object LocationsClockListWidgetStateDefinition :
  GlanceStateDefinition<Loadable<List<LocationSunriseSunsetChange>>> {
  private const val DATA_STORE_FILENAME = "LocationClockListSunriseSunsetWidget"

  private val Context.datastore by
    dataStore(DATA_STORE_FILENAME, LoadableLocationSunriseSunsetChangeListSerializer)

  override suspend fun getDataStore(
    context: Context,
    fileKey: String
  ): DataStore<Loadable<List<LocationSunriseSunsetChange>>> = context.datastore

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME)
}
