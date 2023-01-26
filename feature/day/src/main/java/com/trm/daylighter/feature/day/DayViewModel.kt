package com.trm.daylighter.feature.day

import androidx.lifecycle.ViewModel
import com.trm.daylighter.domain.model.Location
import com.trm.daylighter.domain.repo.LocationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@HiltViewModel
class DayViewModel @Inject constructor(repo: LocationRepo) : ViewModel() {
  val locations: Flow<List<Location>> = repo.getAllFlow()
}
