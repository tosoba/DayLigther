package com.trm.daylighter.feature.location.model

import android.os.Parcelable
import com.trm.daylighter.feature.location.util.MapDefaults
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MapPosition(
  val lat: Double = MapDefaults.LAT,
  val lng: Double = MapDefaults.LNG,
  val zoom: Double = MapDefaults.MIN_ZOOM,
  val orientation: Float = MapDefaults.ORIENTATION
) : Parcelable