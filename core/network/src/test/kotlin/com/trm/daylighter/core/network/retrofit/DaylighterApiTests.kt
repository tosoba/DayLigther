package com.trm.daylighter.core.network.retrofit

import com.trm.daylighter.core.network.di.DaggerNetworkTestComponent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DaylighterApiTests {
  private val api: DaylighterApi = DaggerNetworkTestComponent.builder().build().daylighterApi()

  @Test
  fun getSunriseSunset() = runTest {
    println(
      api.getSunriseSunset(
        lat = WARSAW_LAT,
        lng = WARSAW_LNG,
        date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
      )
    )
  }

  companion object {
    private const val WARSAW_LAT = 52.237049
    private const val WARSAW_LNG = 21.017532
  }
}
