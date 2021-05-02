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

package foundation.e.privacycentralapp.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import foundation.e.flowmvi.feature.BaseFeature
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _actions = MutableSharedFlow<DashboardFeature.Action>()
    val actions = _actions.asSharedFlow()

    val homeFeature: BaseFeature<DashboardFeature.State, DashboardFeature.Action,
        DashboardFeature.Effect, DashboardFeature.SingleEvent> by lazy {
        homeFeature(coroutineScope = viewModelScope)
    }

    fun submitAction(action: DashboardFeature.Action) {
        viewModelScope.launch {
            _actions.emit(action)
        }
    }
}