package com.trm.daylighter.core.testing.util.ext

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import app.cash.turbine.TurbineTestContext
import org.junit.Assert.assertTrue
import kotlin.properties.ReadOnlyProperty

fun AndroidComposeTestRule<*, *>.stringResource(@StringRes resId: Int) =
  ReadOnlyProperty<Any?, String> { _, _ -> activity.getString(resId) }

suspend fun <T> TurbineTestContext<T>.assertNoFurtherEvents() {
  assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
}
