package com.trm.daylighter.core.domain.usecase

import com.trm.daylighter.core.domain.model.DawnOrTwilight
import com.trm.daylighter.core.domain.model.HalfDay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CalculateDawnOrTwilightUseCaseTests {
  @Test
  fun london() {
    val lat = 52.13113642931021
    val lng = 0.13225649369147163
    val zoneId = ZoneId.of("Europe/London")
    val date = LocalDateTime.now(zoneId)
    calculateAllTimestamps(date = date, lat = lat, lng = lng, zoneId = zoneId)
  }

  @Test
  fun antarctica() {
    val lat = -81.83806720596576
    val lng = 15.970982142857139
    val zoneId = ZoneId.of("Antarctica/Troll")
    val date = LocalDateTime.now(zoneId)
    calculateAllTimestamps(date = date, lat = lat, lng = lng, zoneId = zoneId)
  }

  @Test
  fun kazakhstan() {
    val lat = 55.21509112276269
    val lng = 70.35006931849887
    val zoneId = ZoneId.of("Asia/Yekaterinburg")
    val date = LocalDateTime.now(zoneId)
    calculateAllTimestamps(date = date, lat = lat, lng = lng, zoneId = zoneId)
  }

  @Test
  fun greenland() {
    val lat = 83.57659495458118
    val lng = -26.71875
    val zoneId = ZoneId.of("America/Nuuk")
    val date = LocalDateTime.now(zoneId)
    calculateAllTimestamps(date = date, lat = lat, lng = lng, zoneId = zoneId)
  }

  @Test
  fun benchmarkSequential() {
    val start = System.currentTimeMillis()
    london()
    println("${System.currentTimeMillis() - start} ms")
  }

  @Test
  fun benchmarkParallel() = runBlocking {
    val lat = 52.13113642931021
    val lng = 0.13225649369147163
    val zoneId = ZoneId.of("Europe/London")
    val useCase = CalculateDawnOrTwilightUseCase()
    val date = LocalDateTime.now(zoneId)

    val start = System.currentTimeMillis()

    val results =
      HalfDay.values()
        .flatMap { hd ->
          DawnOrTwilight.values().map { dot ->
            async(Dispatchers.Default) {
              useCase(
                latitude = lat,
                longitude = lng,
                date = date,
                dawnOrTwilight = dot,
                halfDay = hd,
                timeZone = TimeZone.getTimeZone(zoneId)
              )
            }
          }
        }
        .awaitAll()

    println("${System.currentTimeMillis() - start} ms")

    results.forEach { result -> println(result?.format(DateTimeFormatter.ISO_DATE_TIME)) }
  }

  private fun calculateAllTimestamps(
    date: LocalDateTime,
    lat: Double,
    lng: Double,
    zoneId: ZoneId?
  ) {
    val useCase = CalculateDawnOrTwilightUseCase()
    HalfDay.values().forEach { hd ->
      DawnOrTwilight.values().forEach { dot ->
        val result =
          useCase(
            latitude = lat,
            longitude = lng,
            date = date,
            dawnOrTwilight = dot,
            halfDay = hd,
            timeZone = TimeZone.getTimeZone(zoneId)
          )
        println("${hd.name} - ${dot.name} ${result?.format(DateTimeFormatter.ISO_DATE_TIME)}")
      }
    }
  }
}
