package com.trm.daylighter.widget.util.ext

import android.graphics.Paint

internal fun lazyPaint(color: Int): Lazy<Paint> =
  lazy(LazyThreadSafetyMode.NONE) { Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = color } }
