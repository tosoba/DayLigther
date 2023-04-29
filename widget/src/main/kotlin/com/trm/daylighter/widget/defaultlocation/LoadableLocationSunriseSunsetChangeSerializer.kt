package com.trm.daylighter.widget.defaultlocation

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.trm.daylighter.core.domain.model.Empty
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.LocationSunriseSunsetChange
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object LoadableLocationSunriseSunsetChangeSerializer :
  Serializer<Loadable<LocationSunriseSunsetChange>> {
  override val defaultValue = Empty

  override suspend fun readFrom(input: InputStream): Loadable<LocationSunriseSunsetChange> =
    try {
      Json.decodeFromString(
        Loadable.serializer(LocationSunriseSunsetChange.serializer()),
        input.readBytes().decodeToString()
      )
    } catch (exception: SerializationException) {
      throw CorruptionException(
        "Could not read Loadable<LocationSunriseSunsetChange> data: ${exception.message}"
      )
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
