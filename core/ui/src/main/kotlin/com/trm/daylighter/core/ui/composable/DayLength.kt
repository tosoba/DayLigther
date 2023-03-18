package com.trm.daylighter.core.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.trm.daylighter.core.common.R
import com.trm.daylighter.core.common.util.ext.dayLengthDiffPrefix
import com.trm.daylighter.core.common.util.ext.dayLengthDiffTime
import com.trm.daylighter.core.common.util.ext.formatTimeDifference
import com.trm.daylighter.core.domain.model.SunriseSunset
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun BoxScope.DayLengthSymbolContent() {
  val sunPainter = rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.sun))
  Image(
    painter = sunPainter,
    contentDescription = null,
    modifier = Modifier.align(Alignment.Center).size(40.dp)
  )
  Icon(
    painter = painterResource(id = R.drawable.clock),
    contentDescription = null,
    modifier = Modifier.align(Alignment.BottomEnd)
  )
}

@Composable
fun DayLengthInfoContent(today: SunriseSunset, yesterday: SunriseSunset) {
  val todayLength = LocalTime.ofSecondOfDay(today.dayLengthSeconds.toLong())
  val dayLengthDiffTime = dayLengthDiffTime(today.dayLengthSeconds, yesterday.dayLengthSeconds)
  val diffPrefix =
    dayLengthDiffPrefix(
      todayLengthSeconds = today.dayLengthSeconds,
      yesterdayLengthSeconds = yesterday.dayLengthSeconds
    )
  Text(text = todayLength.format(DateTimeFormatter.ISO_LOCAL_TIME))
  Text(text = formatTimeDifference(diffPrefix, dayLengthDiffTime))
}
