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
import foundation.e.privacycentralapp.common.Factory
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import foundation.e.privacycentralapp.domain.usecases.IpScramblingStateUseCase
import foundation.e.privacycentralapp.domain.usecases.TrackersStatisticsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
    private val ipScramblingStateUseCase: IpScramblingStateUseCase,
    private val trackersStatisticsUseCase: TrackersStatisticsUseCase
) : ViewModel() {

    private val _actions = MutableSharedFlow<DashboardFeature.Action>()
    val actions = _actions.asSharedFlow()

    val dashboardFeature: DashboardFeature by lazy {
        DashboardFeature.create(
            coroutineScope = viewModelScope,
            getPrivacyStateUseCase = getPrivacyStateUseCase,
            ipScramblingStateUseCase = ipScramblingStateUseCase,
            trackersStatisticsUseCase = trackersStatisticsUseCase
        )
    }

    fun submitAction(action: DashboardFeature.Action) {
        viewModelScope.launch {
            _actions.emit(action)
        }
    }
}

class DashBoardViewModelFactory(
    private val getPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
    private val ipScramblingStateUseCase: IpScramblingStateUseCase,
    private val trackersStatisticsUseCase: TrackersStatisticsUseCase
) : Factory<DashboardViewModel> {
    override fun create(): DashboardViewModel {
        return DashboardViewModel(getPrivacyStateUseCase, ipScramblingStateUseCase, trackersStatisticsUseCase)
    }
}
