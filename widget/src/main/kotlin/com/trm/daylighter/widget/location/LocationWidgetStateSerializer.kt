package com.trm.daylighter.widget.location

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object LocationWidgetStateSerializer : Serializer<LocationWidgetState> {
  private const val DEFAULT_LOCATION_ID = -1L

  override val defaultValue = LocationWidgetState.DefaultLocation(UUID.randomUUID().toString())

  @Serializable
  private data class LocationWidgetStateSurrogate(val locationId: Long, val uuid: String)

  override suspend fun readFrom(input: InputStream): LocationWidgetState =
    try {
      val surrogate =
        Json.decodeFromString(
          deserializer = LocationWidgetStateSurrogate.serializer(),
          string = input.readBytes().decodeToString()
        )
      if (surrogate.locationId != DEFAULT_LOCATION_ID) {
        LocationWidgetState.ChosenLocation(locationId = surrogate.locationId, uuid = surrogate.uuid)
      } else {
        LocationWidgetState.DefaultLocation(uuid = surrogate.uuid)
      }
    } catch (exception: SerializationException) {
      throw CorruptionException("Could not read location widget state : ${exception.message}")
    }

  override suspend fun writeTo(t: LocationWidgetState, output: OutputStream) {
    output.use {
      it.write(
        Json.encodeToString(
            LocationWidgetStateSurrogate(
              locationId =
                when (t) {
                  is LocationWidgetState.DefaultLocation -> DEFAULT_LOCATION_ID
                  is LocationWidgetState.ChosenLocation -> t.locationId
                },
              uuid = t.uuid
            )
          )
          .encodeToByteArray()
      )
    }
  }
}
