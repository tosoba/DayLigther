package com.trm.daylighter.feature.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.domain.usecase.SaveLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LocationViewModel
@Inject
constructor(
  private val saveLocationUseCase: SaveLocationUseCase,
) : ViewModel() {
  private val _savedFlow = MutableSharedFlow<Unit>(replay = 1)
  val savedFlow = _savedFlow.asSharedFlow()

  fun saveLocation(latitude: Double, longitude: Double) {
    viewModelScope.launch {
      saveLocationUseCase(latitude = latitude, longitude = longitude)
      _savedFlow.emit(Unit)
    }
  }
}
