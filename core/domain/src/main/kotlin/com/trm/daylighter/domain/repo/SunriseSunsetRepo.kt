package com.trm.daylighter.domain.repo

interface SunriseSunsetRepo {
  suspend fun sync(): Boolean
}
