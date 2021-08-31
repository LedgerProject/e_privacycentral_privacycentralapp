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

package foundation.e.privacycentralapp.features.internetprivacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import foundation.e.privacycentralapp.common.Factory
import foundation.e.privacymodules.ipscramblermodule.IIpScramblerModule
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class InternetPrivacyViewModel(
    private val ipScramblerModule: IIpScramblerModule,
    private val permissionsModule: PermissionsPrivacyModule
) : ViewModel() {

    private val _actions = MutableSharedFlow<InternetPrivacyFeature.Action>()
    val actions = _actions.asSharedFlow()

    val internetPrivacyFeature: InternetPrivacyFeature by lazy {
        InternetPrivacyFeature.create(
            coroutineScope = viewModelScope,
            ipScramblerModule = ipScramblerModule,
            permissionsModule = permissionsModule
        )
    }

    fun submitAction(action: InternetPrivacyFeature.Action) {
        viewModelScope.launch {
            _actions.emit(action)
        }
    }
}

class InternetPrivacyViewModelFactory(
    private val ipScramblerModule: IIpScramblerModule,
    private val permissionsModule: PermissionsPrivacyModule
) :
    Factory<InternetPrivacyViewModel> {
    override fun create(): InternetPrivacyViewModel {
        return InternetPrivacyViewModel(ipScramblerModule, permissionsModule)
    }
}
