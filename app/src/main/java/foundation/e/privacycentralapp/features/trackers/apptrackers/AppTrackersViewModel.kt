/*
 * Copyright (C) 2021 E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.privacycentralapp.features.trackers.apptrackers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import foundation.e.privacycentralapp.common.Factory
import foundation.e.privacycentralapp.domain.usecases.TrackersStateUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AppTrackersViewModel(
    private val trackersStateUseCase: TrackersStateUseCase
) : ViewModel() {

    private val _actions = MutableSharedFlow<AppTrackersFeature.Action>()
    val actions = _actions.asSharedFlow()

    val feature: AppTrackersFeature by lazy {
        AppTrackersFeature.create(
            coroutineScope = viewModelScope,
            trackersStateUseCase = trackersStateUseCase
        )
    }

    fun submitAction(action: AppTrackersFeature.Action) {
        Log.d("TrackersViewModel", "submitting action")
        viewModelScope.launch {
            _actions.emit(action)
        }
    }
}

class AppTrackersViewModelFactory(
    private val trackersStateUseCase: TrackersStateUseCase
) :
    Factory<AppTrackersViewModel> {
    override fun create(): AppTrackersViewModel {
        return AppTrackersViewModel(trackersStateUseCase)
    }
}
