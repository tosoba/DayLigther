package com.trm.daylighter.core.common.util

inline fun <reified T> Any.takeIfInstance(): T? = this as? T
