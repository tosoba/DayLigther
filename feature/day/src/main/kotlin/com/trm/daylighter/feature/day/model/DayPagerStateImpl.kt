package com.trm.daylighter.feature.day.model

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

@OptIn(ExperimentalFoundationApi::class)
internal class DaylighterPagerStateImpl(
  initialPage: Int = 0,
  initialPageOffsetFraction: Float = 0f,
  val updatedPageCount: () -> Int
) : PagerState(initialPage, initialPageOffsetFraction) {
  var pageCountState = mutableStateOf(updatedPageCount)

  override val pageCount: Int
    get() = pageCountState.value()

  companion object {
    val Saver: Saver<DaylighterPagerStateImpl, *> =
      listSaver(
        save = { listOf(it.currentPage, it.currentPageOffsetFraction, it.pageCount) },
        restore = {
          DaylighterPagerStateImpl(
            initialPage = it[0] as Int,
            initialPageOffsetFraction = it[1] as Float,
            updatedPageCount = { it[2] as Int }
          )
        }
      )
  }
}
