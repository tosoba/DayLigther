package com.trm.daylighter.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.domain.usecase.IsGeocodingEmailPreferenceSetFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
  isGeocodingEmailPreferenceSetFlowUseCase: IsGeocodingEmailPreferenceSetFlowUseCase,
) : ViewModel() {
  val isGeocodeEmailPreferenceSetFlow: SharedFlow<Boolean> =
    isGeocodingEmailPreferenceSetFlowUseCase()
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)
}
