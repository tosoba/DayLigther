package com.trm.daylighter.core.network.retrofit

import com.trm.daylighter.core.network.di.DaggerNetworkTestComponent
import com.trm.daylighter.core.network.di.NetworkTestComponent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NominatimEndpointTests {
  private val component: NetworkTestComponent = DaggerNetworkTestComponent.builder().build()
  private val endpoint: NominatimEndpoint = component.nominatimEndpoint()

  @Test
  fun getAddress() = runTest {
    println(
      endpoint.getAddress(lat = WARSAW_LAT, lon = WARSAW_LNG, email = "therealmerengue@gmail.com")
    )
  }

  companion object {
    private const val WARSAW_LAT = 52.237049
    private const val WARSAW_LNG = 21.017532
  }
}
