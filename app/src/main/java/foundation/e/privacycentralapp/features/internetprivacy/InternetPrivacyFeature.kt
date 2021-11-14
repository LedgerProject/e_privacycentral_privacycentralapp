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

import android.app.Activity
import android.content.Intent
import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.domain.entities.InternetPrivacyMode
import foundation.e.privacycentralapp.domain.usecases.AppListUseCase
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import foundation.e.privacycentralapp.domain.usecases.IpScramblingStateUseCase
import foundation.e.privacymodules.ipscramblermodule.IIpScramblerModule
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn

// Define a state machine for Internet privacy feature
class InternetPrivacyFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<InternetPrivacyFeature.State, InternetPrivacyFeature.Action, InternetPrivacyFeature.Effect, InternetPrivacyFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope,
    { message -> Log.d("InternetPrivacyFeature", message) },
    singleEventProducer
) {
    data class State(
        val mode: InternetPrivacyMode,
        val availableApps: List<ApplicationDescription>,
        val ipScrambledApps: Collection<String>,
        val selectedLocation: String,
        val availableLocationIds: List<String>,
        val forceRedraw: Boolean = false
    ) {
        fun getApps(): List<Pair<ApplicationDescription, Boolean>> {
            return availableApps.map { it to (it.packageName in ipScrambledApps) }
        }

        val selectedLocationPosition get() = availableLocationIds.indexOf(selectedLocation)
    }

    sealed class SingleEvent {
        object RealIPSelectedEvent : SingleEvent()
        object HiddenIPSelectedEvent : SingleEvent()
        data class StartAndroidVpnActivityEvent(val intent: Intent) : SingleEvent()
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {
        object LoadInternetModeAction : Action()
        object UseRealIPAction : Action()
        object UseHiddenIPAction : Action()
        data class AndroidVpnActivityResultAction(val resultCode: Int) : Action()
        data class ToggleAppIpScrambled(val packageName: String, val isIpScrambled: Boolean) : Action()
        data class SelectLocationAction(val position: Int) : Action()
    }

    sealed class Effect {
        object NoEffect : Effect()
        data class ModeUpdatedEffect(val mode: InternetPrivacyMode) : Effect()
        data class QuickPrivacyUpdatedEffect(val enabled: Boolean) : Effect()
        object QuickPrivacyDisabledWarningEffect : Effect()
        data class ShowAndroidVpnDisclaimerEffect(val intent: Intent) : Effect()
        data class IpScrambledAppsUpdatedEffect(val ipScrambledApps: Collection<String>) : Effect()
        data class AvailableAppsListEffect(
            val apps: List<ApplicationDescription>,
            val ipScrambledApps: Collection<String>
        ) : Effect()
        data class LocationSelectedEffect(val locationId: String) : Effect()
        data class AvailableCountriesEffect(val availableLocationsIds: List<String>) : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(
                mode = InternetPrivacyMode.REAL_IP,
                availableApps = emptyList(),
                ipScrambledApps = emptyList(),
                availableLocationIds = emptyList(),
                selectedLocation = ""
            ),
            coroutineScope: CoroutineScope,
            ipScramblerModule: IIpScramblerModule,
            getQuickPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
            ipScramblingStateUseCase: IpScramblingStateUseCase,
            appListUseCase: AppListUseCase
        ) = InternetPrivacyFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.ModeUpdatedEffect -> state.copy(mode = effect.mode)
                    is Effect.IpScrambledAppsUpdatedEffect -> state.copy(ipScrambledApps = effect.ipScrambledApps)
                    is Effect.AvailableAppsListEffect -> state.copy(
                        availableApps = effect.apps,
                        ipScrambledApps = effect.ipScrambledApps
                    )
                    is Effect.AvailableCountriesEffect -> state.copy(availableLocationIds = effect.availableLocationsIds)
                    is Effect.LocationSelectedEffect -> state.copy(selectedLocation = effect.locationId)
                    Effect.QuickPrivacyDisabledWarningEffect -> state.copy(forceRedraw = !state.forceRedraw)
                    else -> state
                }
            },
            actor = { state, action ->
                when {
                    action is Action.LoadInternetModeAction -> merge(
                        getQuickPrivacyStateUseCase.quickPrivacyEnabledFlow.map { Effect.QuickPrivacyUpdatedEffect(it) },
                        ipScramblingStateUseCase.internetPrivacyMode.map { Effect.ModeUpdatedEffect(it) }.shareIn(scope = coroutineScope, started = SharingStarted.Lazily, replay = 0),
                        // flowOf(Effect.ModeUpdatedEffect(InternetPrivacyMode.REAL_IP)),
                        appListUseCase.getAppsUsingInternet().map { apps ->
                            if (ipScramblerModule.appList.isEmpty()) {
                                ipScramblerModule.appList =
                                    apps.map { it.packageName }.toMutableSet()
                            }
                            Effect.AvailableAppsListEffect(
                                apps,
                                ipScramblerModule.appList
                            )
                        },
                        flow {
                            val locationIds = mutableListOf("")
                            locationIds.addAll(ipScramblerModule.getAvailablesLocations().sorted())
                            emit(Effect.AvailableCountriesEffect(locationIds))
                        },
                        flowOf(Effect.LocationSelectedEffect(ipScramblerModule.exitCountry))
                    ).flowOn(Dispatchers.Default)
                    action is Action.AndroidVpnActivityResultAction ->
                        if (action.resultCode == Activity.RESULT_OK) {
                            if (state.mode in listOf(
                                    InternetPrivacyMode.REAL_IP,
                                    InternetPrivacyMode.REAL_IP_LOADING
                                )
                            ) {
                                ipScramblingStateUseCase.toggle(hideIp = true)
                                flowOf(Effect.ModeUpdatedEffect(InternetPrivacyMode.HIDE_IP_LOADING))
                            } else {
                                flowOf(Effect.ErrorEffect("Vpn already started"))
                            }
                        } else {
                            flowOf(Effect.ErrorEffect("Vpn wasn't allowed to start"))
                        }

                    action is Action.UseRealIPAction && state.mode in listOf(
                        InternetPrivacyMode.HIDE_IP,
                        InternetPrivacyMode.HIDE_IP_LOADING,
                        InternetPrivacyMode.REAL_IP_LOADING
                    ) -> {
                        if (getQuickPrivacyStateUseCase.isQuickPrivacyEnabled) {
                            ipScramblingStateUseCase.toggle(hideIp = false)
                            flowOf(Effect.ModeUpdatedEffect(InternetPrivacyMode.REAL_IP_LOADING))
                        } else {
                            flowOf(Effect.QuickPrivacyDisabledWarningEffect)
                        }
                    }
                    action is Action.UseHiddenIPAction
                        && state.mode in listOf(
                            InternetPrivacyMode.REAL_IP,
                            InternetPrivacyMode.REAL_IP_LOADING
                        ) -> {
                        if (getQuickPrivacyStateUseCase.isQuickPrivacyEnabled) {
                            ipScramblingStateUseCase.toggle(hideIp = true)
                            flowOf(Effect.ModeUpdatedEffect(InternetPrivacyMode.HIDE_IP_LOADING))
                        } else {
                            flowOf(Effect.QuickPrivacyDisabledWarningEffect)
                        }
                    }

                    action is Action.ToggleAppIpScrambled -> {
                        val ipScrambledApps = mutableSetOf<String>()
                        ipScrambledApps.addAll(ipScramblerModule.appList)
                        if (action.isIpScrambled) {
                            ipScrambledApps.add(action.packageName)
                        } else {
                            ipScrambledApps.remove(action.packageName)
                        }
                        ipScramblerModule.appList = ipScrambledApps
                        flowOf(Effect.IpScrambledAppsUpdatedEffect(ipScrambledApps = ipScrambledApps))
                    }
                    action is Action.SelectLocationAction -> {
                        val locationId = state.availableLocationIds[action.position]
                        ipScramblerModule.exitCountry = locationId
                        flowOf(Effect.LocationSelectedEffect(locationId))
                    }
                    else -> flowOf(Effect.NoEffect)
                }
            },
            singleEventProducer = { _, action, effect ->
                when {
                    effect is Effect.ErrorEffect -> SingleEvent.ErrorEvent(effect.message)
                    effect == Effect.QuickPrivacyDisabledWarningEffect -> SingleEvent.ErrorEvent("Enabled Quick Privacy to use functionalities")

                    action is Action.UseHiddenIPAction
                        && effect is Effect.ShowAndroidVpnDisclaimerEffect ->
                        SingleEvent.StartAndroidVpnActivityEvent(effect.intent)
                    else -> null
                }
            }
        )
    }
}
