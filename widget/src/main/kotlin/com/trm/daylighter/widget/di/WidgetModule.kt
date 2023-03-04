package com.trm.daylighter.widget.di

import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.widget.WidgetManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WidgetModule {
  @Binds fun WidgetManagerImpl.bind(): WidgetManager
}
