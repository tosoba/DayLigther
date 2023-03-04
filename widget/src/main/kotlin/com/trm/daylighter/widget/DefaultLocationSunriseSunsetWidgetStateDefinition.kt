package com.trm.daylighter.widget

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
import kotlinx.serialization.json.Json

object DefaultLocationSunriseSunsetWidgetStateDefinition :
  GlanceStateDefinition<Loadable<LocationSunriseSunsetChange>> {
  private const val DATA_STORE_FILENAME = "sunriseSunsetWidget"

  private val Context.datastore by dataStore(DATA_STORE_FILENAME, LocationLoadableSerializer)

  override suspend fun getDataStore(
    context: Context,
    fileKey: String
  ): DataStore<Loadable<LocationSunriseSunsetChange>> = context.datastore

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME)

  private object LocationLoadableSerializer : Serializer<Loadable<LocationSunriseSunsetChange>> {
    override val defaultValue = Empty

    override suspend fun readFrom(input: InputStream): Loadable<LocationSunriseSunsetChange> =
      try {
        Json.decodeFromString(
          Loadable.serializer(LocationSunriseSunsetChange.serializer()),
          input.readBytes().decodeToString()
        )
      } catch (exception: SerializationException) {
        throw CorruptionException("Could not read weather data: ${exception.message}")
      }

    override suspend fun writeTo(t: Loadable<LocationSunriseSunsetChange>, output: OutputStream) {
      output.use {
        it.write(
          Json.encodeToString(Loadable.serializer(LocationSunriseSunsetChange.serializer()), t)
            .encodeToByteArray()
        )
      }
    }
  }
}
