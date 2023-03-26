package com.trm.daylighter.feature.location.model

import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.trm.daylighter.core.common.util.MapDefaults
import kotlinx.parcelize.Parcelize

@Parcelize
@Stable
data class MapPosition(
  val latitude: Double = MapDefaults.LATITUDE,
  val longitude: Double = MapDefaults.LONGITUDE,
  val zoom: Double = MapDefaults.MIN_ZOOM,
  val orientation: Float = MapDefaults.ORIENTATION
) : Parcelable
