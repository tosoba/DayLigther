package com.trm.daylighter.core.common.util.ext

inline fun <reified T> Any.takeIfInstance(): T? = this as? T
