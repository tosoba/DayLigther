package com.trm.daylighter.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.domain.model.Loadable
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.domain.usecase.SetDefaultLocationUseCase
import com.trm.daylighter.ui.model.StableValue
import com.trm.daylighter.ui.model.asStable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class LocationsViewModel
@Inject
constructor(
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
  private val setDefaultLocationUseCase: SetDefaultLocationUseCase,
) : ViewModel() {
  val locations: Flow<Loadable<List<StableValue<Location>>>> =
    getAllLocationsFlowUseCase()
      .map { it.map { locations -> locations.map(Location::asStable) } }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  fun setDefaultLocation(id: Long) {
    viewModelScope.launch { setDefaultLocationUseCase(id = id) }
  }
}
