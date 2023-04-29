package com.trm.daylighter.widget.list

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object LoadableLocationSunriseSunsetChangeListSerializer :
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
