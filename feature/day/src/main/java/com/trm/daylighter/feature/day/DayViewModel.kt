package com.trm.daylighter.feature.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.domain.model.Loadable
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.usecase.GetAllLocationsFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

@HiltViewModel
class DayViewModel
@Inject
constructor(
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
) : ViewModel() {
  val locations: Flow<Loadable<List<Location>>> =
    getAllLocationsFlowUseCase()
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)
}
