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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacymodules.ipscramblermodule.IIpScramblerModule
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge

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
        val mode: IIpScramblerModule.Status,
        val availableApps: List<ApplicationDescription>,
        val ipScrambledApps: Collection<String>,
        val selectedLocation: String,
        val availableLocationIds: List<String>
    ) {

        val isAllAppsScrambled get() = ipScrambledApps.isEmpty()
        fun getScrambledApps(): List<Pair<ApplicationDescription, Boolean>> {
            return availableApps
                .filter { it.packageName in ipScrambledApps }
                .map { it to true }
        }

        fun getApps(): List<Pair<ApplicationDescription, Boolean>> {
            return availableApps
                .filter { it.packageName !in ipScrambledApps }
                .map { it to false }
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
        data class ModeUpdatedEffect(val mode: IIpScramblerModule.Status) : Effect()
        object NoEffect : Effect()
        data class ShowAndroidVpnDisclaimerEffect(val intent: Intent) : Effect()
        data class IpScrambledAppsUpdatedEffect(val ipScrambledApps: Collection<String>) : Effect()
        data class AvailableAppsListEffect(val apps: List<ApplicationDescription>) : Effect()
        data class LocationSelectedEffect(val locationId: String) : Effect()
        data class AvailableCountriesEffect(val availableLocationsIds: List<String>) : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(
                IIpScramblerModule.Status.STOPPING,
                availableApps = emptyList(),
                ipScrambledApps = emptyList(),
                availableLocationIds = emptyList(),
                selectedLocation = ""
            ),
            coroutineScope: CoroutineScope,
            ipScramblerModule: IIpScramblerModule,
            permissionsModule: PermissionsPrivacyModule
        ) = InternetPrivacyFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.ModeUpdatedEffect -> state.copy(mode = effect.mode)
                    is Effect.IpScrambledAppsUpdatedEffect -> state.copy(ipScrambledApps = effect.ipScrambledApps)
                    is Effect.AvailableAppsListEffect -> state.copy(availableApps = effect.apps)
                    is Effect.AvailableCountriesEffect -> state.copy(availableLocationIds = effect.availableLocationsIds)
                    is Effect.LocationSelectedEffect -> state.copy(selectedLocation = effect.locationId)
                    else -> state
                }
            },
            actor = { state, action ->
                when {
                    action is Action.LoadInternetModeAction -> merge(
                        callbackFlow {
                            val listener = object : IIpScramblerModule.Listener {
                                override fun onStatusChanged(newStatus: IIpScramblerModule.Status) {
                                    offer(Effect.ModeUpdatedEffect(newStatus))
                                }

                                override fun log(message: String) {}
                                override fun onTrafficUpdate(upload: Long, download: Long, read: Long, write: Long) {}
                            }
                            ipScramblerModule.addListener(listener)
                            ipScramblerModule.requestStatus()
                            awaitClose { ipScramblerModule.removeListener(listener) }
                        },
                        flow {
                            // TODO: filter deactivated apps"
                            val apps = permissionsModule.getInstalledApplications()
                                .filter {
                                    permissionsModule.getPermissions(it.packageName)
                                        .contains(Manifest.permission.INTERNET)
                                }.map {
                                    it.icon = permissionsModule.getApplicationIcon(it.packageName)
                                    it
                                }.sortedWith(object : Comparator<ApplicationDescription> {
                                    override fun compare(
                                        p0: ApplicationDescription?,
                                        p1: ApplicationDescription?
                                    ): Int {
                                        return if (p0?.icon != null && p1?.icon != null) {
                                            p0.label.toString().compareTo(p1.label.toString())
                                        } else if (p0?.icon == null) {
                                            1
                                        } else {
                                            -1
                                        }
                                    }
                                })
                            emit(Effect.AvailableAppsListEffect(apps))
                        },
                        flowOf(Effect.IpScrambledAppsUpdatedEffect(ipScramblerModule.appList)),
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
                                    IIpScramblerModule.Status.OFF,
                                    IIpScramblerModule.Status.STOPPING
                                )
                            ) {
                                ipScramblerModule.start()
                                flowOf(Effect.ModeUpdatedEffect(IIpScramblerModule.Status.STARTING))
                            } else {
                                flowOf(Effect.ErrorEffect("Vpn already started"))
                            }
                        } else {
                            flowOf(Effect.ErrorEffect("Vpn wasn't allowed to start"))
                        }

                    action is Action.UseRealIPAction && state.mode in listOf(
                        IIpScramblerModule.Status.ON,
                        IIpScramblerModule.Status.STARTING,
                        IIpScramblerModule.Status.STOPPING
                    ) -> {
                        ipScramblerModule.stop()
                        flowOf(Effect.ModeUpdatedEffect(IIpScramblerModule.Status.STOPPING))
                    }
                    action is Action.UseHiddenIPAction
                        && state.mode in listOf(
                            IIpScramblerModule.Status.OFF,
                            IIpScramblerModule.Status.STOPPING
                        ) -> {
                        ipScramblerModule.prepareAndroidVpn()?.let {
                            flowOf(Effect.ShowAndroidVpnDisclaimerEffect(it))
                        } ?: run {
                            ipScramblerModule.start()
                            flowOf(Effect.ModeUpdatedEffect(IIpScramblerModule.Status.STARTING))
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

                    action is Action.UseHiddenIPAction
                        && effect is Effect.ShowAndroidVpnDisclaimerEffect ->
                        SingleEvent.StartAndroidVpnActivityEvent(effect.intent)

                    // Action.UseRealIPAction, Action.UseHiddenIPAction -> when (effect) {
                    //     is Effect.ModeUpdatedEffect -> {
                    //         if (effect.mode == InternetPrivacyMode.REAL_IP) {
                    //             SingleEvent.RealIPSelectedEvent
                    //         } else {
                    //             SingleEvent.HiddenIPSelectedEvent
                    //         }
                    //     }
                    //     is Effect.ErrorEffect -> {
                    //         SingleEvent.ErrorEvent(effect.message)
                    //     }
                    // }
                    else -> null
                }
            }
        )
    }
}
