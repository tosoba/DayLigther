package com.trm.daylighter.feature.widget.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.navigation.WidgetLocationDeepLinkParams
import com.trm.daylighter.core.common.navigation.WidgetTypeParam
import com.trm.daylighter.core.domain.model.Loadable
import com.trm.daylighter.core.domain.model.Location
import com.trm.daylighter.core.domain.usecase.GetAllLocationsFlowUseCase
import com.trm.daylighter.core.domain.widget.WidgetManager
import com.trm.daylighter.core.ui.model.StableValue
import com.trm.daylighter.core.ui.model.asStable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetLocationViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  getAllLocationsFlowUseCase: GetAllLocationsFlowUseCase,
  private val widgetManager: WidgetManager
) : ViewModel() {
  val locations: Flow<Loadable<List<StableValue<Location>>>> =
    getAllLocationsFlowUseCase()
      .map { it.map { locations -> locations.map(Location::asStable) } }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val selectedLocationIdFlow: StateFlow<Long?> =
    savedStateHandle.getStateFlow(
      SavedState.SELECTED_LOCATION_ID.name,
      savedStateHandle.get<String>(WidgetLocationDeepLinkParams.LOCATION_ID)?.toLong()
    )

  var selectedLocationId: Long?
    get() = selectedLocationIdFlow.value
    set(value) {
      savedStateHandle[SavedState.SELECTED_LOCATION_ID.name] = value
    }

  val mode: WidgetLocationMode
    get() =
      if (savedStateHandle.contains(WidgetLocationDeepLinkParams.LOCATION_ID)) {
        WidgetLocationMode.EDIT
      } else {
        WidgetLocationMode.ADD
      }

  private val _toastMessageResId = MutableSharedFlow<Int>()
  val toastMessageResId: SharedFlow<Int> = _toastMessageResId.asSharedFlow()

  fun onAddDayNightCycleWidget() {
    addSelectedLocationWidget(addWidget = widgetManager::addDayNightCycleWidget)
  }

  fun onAddGoldenBlueHourWidget() {
    addSelectedLocationWidget(addWidget = widgetManager::addGoldenBlueHourWidget)
  }

  fun onEditWidgetLocationClick() {
    editSelectedLocationWidget(
      when (
        WidgetTypeParam.fromName(
          requireNotNull(savedStateHandle.get<String>(WidgetLocationDeepLinkParams.WIDGET_TYPE))
        )
      ) {
        WidgetTypeParam.DAY_NIGHT_CYCLE -> widgetManager::editDayNightCycleWidget
        WidgetTypeParam.GOLDEN_BLUE_HOUR -> widgetManager::editGoldenBlueHourWidget
      }
    )
  }

  private fun addSelectedLocationWidget(addWidget: suspend (Long) -> Boolean) {
    val locationId = selectedLocationId ?: return
    viewModelScope.launch {
      if (!addWidget(locationId)) _toastMessageResId.emit(R.string.failed_to_add_widget)
    }
  }

  private fun editSelectedLocationWidget(editWidget: suspend (Int, Long) -> Unit) {
    val locationId = selectedLocationId ?: return
    viewModelScope.launch {
      editWidget(
        requireNotNull(savedStateHandle.get<String>(WidgetLocationDeepLinkParams.GLANCE_ID))
          .toInt(),
        locationId
      )
      _toastMessageResId.emit(R.string.widget_location_updated)
    }
  }

  internal enum class SavedState {
    SELECTED_LOCATION_ID
  }
}
