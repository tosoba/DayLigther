package com.trm.daylighter.widget.location

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object LocationWidgetStateDefinition : GlanceStateDefinition<LocationWidgetState> {
  private const val DATA_STORE_FILENAME = "LocationChartWidget"

  private val Context.datastore: DataStore<LocationWidgetState> by
    dataStore(DATA_STORE_FILENAME, LocationWidgetStateSerializer)

  override suspend fun getDataStore(
    context: Context,
    fileKey: String
  ): DataStore<LocationWidgetState> = context.datastore

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME)
}
