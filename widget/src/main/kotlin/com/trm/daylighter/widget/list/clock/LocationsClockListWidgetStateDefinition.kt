package com.trm.daylighter.widget.list.clock

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object LocationsClockListWidgetStateDefinition :
  GlanceStateDefinition<Loadable<List<LocationSunriseSunsetChange>>> {
  private const val DATA_STORE_FILENAME = "LocationClockListSunriseSunsetWidget"

  private val Context.datastore by dataStore(DATA_STORE_FILENAME, LocationLoadableSerializer)

  override suspend fun getDataStore(
    context: Context,
    fileKey: String
  ): DataStore<Loadable<List<LocationSunriseSunsetChange>>> = context.datastore

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME)

  private object LocationLoadableSerializer :
    Serializer<Loadable<List<LocationSunriseSunsetChange>>> {
    override val defaultValue = Empty

    override suspend fun readFrom(input: InputStream): Loadable<List<LocationSunriseSunsetChange>> =
      try {
        Json.decodeFromString(
          Loadable.serializer(ListSerializer(LocationSunriseSunsetChange.serializer())),
          input.readBytes().decodeToString()
        )
      } catch (exception: SerializationException) {
        throw CorruptionException("Could not read locations list widget data: ${exception.message}")
      }

    override suspend fun writeTo(
      t: Loadable<List<LocationSunriseSunsetChange>>,
      output: OutputStream
    ) {
      output.use {
        it.write(
          Json.encodeToString(
              Loadable.serializer(ListSerializer(LocationSunriseSunsetChange.serializer())),
              t
            )
            .encodeToByteArray()
        )
      }
    }
  }
}
