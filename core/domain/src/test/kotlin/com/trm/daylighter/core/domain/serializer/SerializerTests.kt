package com.trm.daylighter.core.domain.serializer

import com.trm.daylighter.core.domain.model.*
import java.io.IOException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

class SerializerTests {
  @Test
  fun failedLoadableSerialization() {
    val serializer = Loadable.serializer(Int.serializer())
    val loadable = FailedNext(5, IOException("Test"))
    val encoded = Json.encodeToString(serializer, loadable)
    val decoded = Json.decodeFromString<Loadable<Int>>(encoded)
    assert(decoded is FailedNext<Int>)
    assert(loadable.data == (decoded as FailedNext<Int>).data)
    assert(decoded.isFailedWith<IOException>())
    assert(loadable.throwable?.message == decoded.throwable?.message)
  }
}
