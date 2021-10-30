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

package foundation.e.privacycentralapp.domain.usecases

import android.content.Intent
import android.util.Log
import foundation.e.privacycentralapp.data.repositories.LocalStateRepository
import foundation.e.privacycentralapp.domain.entities.InternetPrivacyMode
import foundation.e.privacymodules.ipscramblermodule.IIpScramblerModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IpScramblingStateUseCase(
    private val ipScramblerModule: IIpScramblerModule,
    private val localStateRepository: LocalStateRepository,
    private val coroutineScope: CoroutineScope
) {

    // private val internetPrivacyModeMutableFlow = MutableStateFlow(InternetPrivacyMode.REAL_IP)
    val internetPrivacyMode: StateFlow<InternetPrivacyMode> = callbackFlow {
        val listener = object : IIpScramblerModule.Listener {
            override fun onStatusChanged(newStatus: IIpScramblerModule.Status) {
                offer(map(newStatus))
            }

            override fun log(message: String) {}
            override fun onTrafficUpdate(
                upload: Long,
                download: Long,
                read: Long,
                write: Long
            ) {
            }
        }
        ipScramblerModule.addListener(listener)
        ipScramblerModule.requestStatus()
        awaitClose { ipScramblerModule.removeListener(listener) }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = InternetPrivacyMode.REAL_IP
    )

    init {
        coroutineScope.launch {
            localStateRepository.quickPrivacyEnabledFlow.collect {
                applySettings(it, localStateRepository.isIpScramblingEnabled)
            }
        }
    }

    fun toggle(hideIp: Boolean): Intent? {
        if (!localStateRepository.isQuickPrivacyEnabled) return null

        localStateRepository.isIpScramblingEnabled = hideIp
        return applySettings(true, hideIp)
    }

    private fun applySettings(isQuickPrivacyEnabled: Boolean, isIpScramblingEnabled: Boolean): Intent? {
        when {
            isQuickPrivacyEnabled && isIpScramblingEnabled -> when (internetPrivacyMode.value) {
                InternetPrivacyMode.REAL_IP, InternetPrivacyMode.REAL_IP_LOADING -> {
                    val intent = ipScramblerModule.prepareAndroidVpn()
                    if (intent != null) {
                        return intent
                    } else {
                        ipScramblerModule.start()
                    }
                }
                else -> {
                    Log.d("testQPFlow", "Not starting tor, already in started state")
                }
            }
            else -> when (internetPrivacyMode.value) {
                InternetPrivacyMode.HIDE_IP, InternetPrivacyMode.HIDE_IP_LOADING -> ipScramblerModule.stop()

                else -> {
                    Log.d("testQPFlow", "Not stoping tor, already in stop or stoping state")
                }
            }
        }
        return null
    }

    private fun map(status: IIpScramblerModule.Status): InternetPrivacyMode {
        return when (status) {
            IIpScramblerModule.Status.OFF -> InternetPrivacyMode.REAL_IP
            IIpScramblerModule.Status.ON -> InternetPrivacyMode.HIDE_IP
            IIpScramblerModule.Status.STARTING -> InternetPrivacyMode.HIDE_IP_LOADING
            IIpScramblerModule.Status.STOPPING,
            IIpScramblerModule.Status.START_DISABLED -> InternetPrivacyMode.REAL_IP_LOADING
        }
    }
}
