package com.trm.daylighter.core.network.geocoding

import com.trm.daylighter.core.network.di.DaggerNetworkTestComponent
import com.trm.daylighter.core.network.di.NetworkTestComponent
import fr.dudie.nominatim.client.JsonNominatimClient
import fr.dudie.nominatim.model.Element
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.After
import org.junit.Test

class NominatimTests {
  private val component: NetworkTestComponent = DaggerNetworkTestComponent.builder().build()
  private val httpClient: CloseableHttpClient = component.nominatimHttpClient()
  private val nominatimClient: JsonNominatimClient = component.jsonNominatimClient()

  @Test
  fun getAddress() {
    val address = nominatimClient.getAddress(1.64891269513038, 48.1166561643464, 5)
    println(address.displayName)
    println("Elements:")
    address.addressElements?.forEach(::printElement)
    println("Details:")
    address.nameDetails?.forEach(::printElement)
  }

  @After
  fun closeClient() {
    httpClient.close()
  }

  private fun printElement(element: Element) {
    println("${element.key} : ${element.value}")
  }
}
