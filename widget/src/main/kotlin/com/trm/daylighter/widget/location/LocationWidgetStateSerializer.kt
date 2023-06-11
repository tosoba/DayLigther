package com.trm.daylighter.widget.location

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object LocationWidgetStateSerializer : Serializer<LocationWidgetState> {
  private const val DEFAULT_LOCATION_ID = -1L

  override val defaultValue = LocationWidgetState.DefaultLocation(UUID.randomUUID().toString())

  override suspend fun readFrom(input: InputStream): LocationWidgetState =
    try {
      val locationId =
        Json.decodeFromString(
          deserializer = Long.serializer(),
          string = input.readBytes().decodeToString()
        )
      val uuid =
        Json.decodeFromString(
          deserializer = String.serializer(),
          string = input.readBytes().decodeToString()
        )
      if (locationId != DEFAULT_LOCATION_ID) LocationWidgetState.ChosenLocation(locationId, uuid)
      else LocationWidgetState.DefaultLocation(uuid)
    } catch (exception: SerializationException) {
      throw CorruptionException("Could not read location widget state : ${exception.message}")
    }

  override suspend fun writeTo(t: LocationWidgetState, output: OutputStream) {
    output.use {
      it.write(
        Json.encodeToString(
            serializer = Long.serializer(),
            value =
              when (t) {
                is LocationWidgetState.DefaultLocation -> DEFAULT_LOCATION_ID
                is LocationWidgetState.ChosenLocation -> t.locationId
              }
          )
          .encodeToByteArray()
      )
      it.write(
        Json.encodeToString(serializer = String.serializer(), value = t.uuid).encodeToByteArray()
      )
    }
  }
}
