package com.trm.daylighter.feature.location.model

import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.trm.daylighter.core.common.model.MapDefaults
import java.util.UUID
import kotlinx.parcelize.Parcelize

@Parcelize
@Stable
data class MapPosition(
  val latitude: Double = MapDefaults.LATITUDE,
  val longitude: Double = MapDefaults.LONGITUDE,
  val zoom: Double = MapDefaults.MIN_ZOOM,
  val orientation: Float = MapDefaults.ORIENTATION,
  val label: String = MapDefaults.LABEL,
  private val uuid: UUID = UUID.randomUUID(),
) : Parcelable
