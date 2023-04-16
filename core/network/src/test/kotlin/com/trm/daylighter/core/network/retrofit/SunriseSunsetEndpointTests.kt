package com.trm.daylighter.core.network.retrofit

import com.trm.daylighter.core.network.di.DaggerNetworkTestComponent
import com.trm.daylighter.core.network.di.NetworkTestComponent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SunriseSunsetEndpointTests {
  private val component: NetworkTestComponent = DaggerNetworkTestComponent.builder().build()
  private val endpoint: SunriseSunsetEndpoint = component.sunriseSunsetEndpoint()

  @Test
  fun getSunriseSunset() = runTest {
    println(
      endpoint.getSunriseSunset(
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
