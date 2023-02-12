package com.trm.daylighter.feature.location.model

import android.os.Parcelable
import com.trm.daylighter.core.common.util.ext.MapDefaults
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MapPosition(
  val latitude: Double = MapDefaults.LATITUDE,
  val longitude: Double = MapDefaults.LONGITUDE,
  val zoom: Double = MapDefaults.MIN_ZOOM,
  val orientation: Float = MapDefaults.ORIENTATION
) : Parcelable
