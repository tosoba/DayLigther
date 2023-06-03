package com.trm.daylighter.feature.widget.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.navigation.WidgetLocationDeepLinkParams
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

@HiltViewModel
class WidgetLocationViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
) : ViewModel() {
  val locations: Flow<Loadable<List<StableValue<Location>>>> =
    getAllLocationsFlowUseCase()
      .map { it.map { locations -> locations.map(Location::asStable) } }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val selectedLocationIdFlow: StateFlow<Long?> =
    savedStateHandle.getStateFlow(
      SavedState.SELECTED_LOCATION_ID.name,
      savedStateHandle.get<Long>(WidgetLocationDeepLinkParams.LOCATION_ID)
    )
  var selectedLocationId: Long?
    get() = selectedLocationIdFlow.value
    set(value) {
      savedStateHandle[SavedState.SELECTED_LOCATION_ID.name] = value
    }

  private enum class SavedState {
    SELECTED_LOCATION_ID
  }
}
