package com.trm.daylighter.feature.widget.location

import android.appwidget.AppWidgetManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.daylighter.core.common.navigation.WidgetLocationRouteParams
import com.trm.daylighter.core.common.navigation.WidgetType
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
  private val widgetManager: WidgetManager,
) : ViewModel() {
  val locations: Flow<Loadable<List<StableValue<Location>>>> =
    getAllLocationsFlowUseCase()
      .map { it.map { locations -> locations.map(Location::asStable) } }
      .shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L), replay = 1)

  val selectedLocationIdFlow: StateFlow<Long?> =
    savedStateHandle.getStateFlow(
      SavedState.SELECTED_LOCATION_ID.name,
      savedStateHandle.get<String>(WidgetLocationRouteParams.LOCATION_ID)?.toLong(),
    )

  var selectedLocationId: Long?
    get() = selectedLocationIdFlow.value
    set(value) {
      savedStateHandle[SavedState.SELECTED_LOCATION_ID.name] = value
    }

  val mode: WidgetLocationMode
    get() =
      if (savedStateHandle.contains(AppWidgetManager.EXTRA_APPWIDGET_ID)) WidgetLocationMode.EDIT
      else WidgetLocationMode.ADD

  private val _widgetStatus = MutableSharedFlow<Int>()
  val widgetStatus: SharedFlow<Int> = _widgetStatus.asSharedFlow()

  fun onAddDayNightCycleWidget() {
    addSelectedLocationWidget(addWidget = widgetManager::addDayNightCycleWidget)
  }

  fun onAddGoldenBlueHourWidget() {
    addSelectedLocationWidget(addWidget = widgetManager::addGoldenBlueHourWidget)
  }

  fun onConfirmEditWidgetLocationClick() {
    editSelectedLocationWidget(
      when (
        WidgetType.fromName(
          requireNotNull(savedStateHandle.get<String>(WidgetLocationRouteParams.WIDGET_TYPE))
        )
      ) {
        WidgetType.DAY_NIGHT_CYCLE -> widgetManager::editDayNightCycleWidget
        WidgetType.GOLDEN_BLUE_HOUR -> widgetManager::editGoldenBlueHourWidget
      }
    )
  }

  private fun addSelectedLocationWidget(addWidget: suspend (Long) -> Boolean) {
    val locationId = selectedLocationId ?: return
    viewModelScope.launch {
      if (!addWidget(locationId)) _widgetStatus.emit(R.string.failed_to_add_widget)
    }
  }

  private fun editSelectedLocationWidget(editWidget: (Int, Long) -> Unit) {
    val locationId = selectedLocationId ?: return
    editWidget(
      requireNotNull(savedStateHandle.get<String>(AppWidgetManager.EXTRA_APPWIDGET_ID)).toInt(),
      locationId,
    )
    _widgetStatus.tryEmit(R.string.widget_location_updated)
  }

  internal enum class SavedState {
    SELECTED_LOCATION_ID
  }
}
