package com.trm.daylighter.widget.util.ext

import android.graphics.Paint

internal fun antiAliasPaint(color: Int): Paint =
  Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = color }
