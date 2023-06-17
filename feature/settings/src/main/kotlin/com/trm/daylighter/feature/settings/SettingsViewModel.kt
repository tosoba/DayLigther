package com.trm.daylighter.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.domain.usecase.ClearGeocodingEmailPreferenceUseCase
import com.trm.daylighter.core.domain.usecase.IsGeocodingEmailPreferenceSetFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
  isGeocodingEmailPreferenceSetFlowUseCase: IsGeocodingEmailPreferenceSetFlowUseCase,
  private val clearGeocodingEmailPreferenceUseCase: ClearGeocodingEmailPreferenceUseCase
) : ViewModel() {
  val isGeocodeEmailPreferenceSetFlow: SharedFlow<Boolean> =
    isGeocodingEmailPreferenceSetFlowUseCase()
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  fun clearGeocodingEmail() {
    viewModelScope.launch { clearGeocodingEmailPreferenceUseCase() }
  }
}
