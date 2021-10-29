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

import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.domain.entities.InternetPrivacyMode
import foundation.e.privacycentralapp.domain.entities.LocationMode
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import foundation.e.privacycentralapp.domain.usecases.IpScramblingStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

// Define a state machine for Dashboard Feature
class DashboardFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<DashboardFeature.State,
    DashboardFeature.Action,
    DashboardFeature.Effect,
    DashboardFeature.SingleEvent>(
    initialState, actor, reducer, coroutineScope, { message -> Log.d("DashboardFeature", message) },
    singleEventProducer
) {
    sealed class State() {
        object LoadingState : State()
        data class DisabledState(
            val totalGraph: Int = 230,
            // val graphData
            val trackersCount: Int = 77,
            val activeTrackersCount: Int = 22
        ) : State()
        data class EnabledState(
            val isAllTrackersBlocked: Boolean = false,
            val locationMode: LocationMode = LocationMode.CUSTOM_LOCATION,
            val internetPrivacyMode: InternetPrivacyMode,
            val totalGraph: Int = 150,
            // val graphData
            val trackersCount: Int = 80,
            val activeTrackersCount: Int = 10
        ) : State()
    }

    sealed class SingleEvent {
        object NavigateToQuickProtectionSingleEvent : SingleEvent()
        object NavigateToTrackersSingleEvent : SingleEvent()
        object NavigateToInternetActivityPrivacySingleEvent : SingleEvent()
        object NavigateToLocationSingleEvent : SingleEvent()
        object NavigateToPermissionsSingleEvent : SingleEvent()
    }

    sealed class Action {
        object InitAction : Action()

        object TogglePrivacyAction : Action()
        // object ShowQuickPrivacyProtectionInfoAction : Action()
        // object ObserveDashboardAction : Action()
        // object ShowDashboardAction : Action()
        object ShowFakeMyLocationAction : Action()
        object ShowInternetActivityPrivacyAction : Action()
        object ShowAppsPermissions : Action()
        object ShowTrackers : Action()
    }

    sealed class Effect {
        object NoEffect : Effect()
        data class UpdateStateEffect(val isEnabled: Boolean) : Effect()
        data class IpScramblingModeUpdatedEffect(val mode: InternetPrivacyMode) : Effect()

        object OpenQuickPrivacyProtectionEffect : Effect()
        data class OpenDashboardEffect(
            val trackersCount: Int,
            val activeTrackersCount: Int,
            val totalApps: Int,
            val permissionCount: Int,
            val appsUsingLocationPerm: Int,
            val locationMode: LocationMode,
            val internetPrivacyMode: InternetPrivacyMode
        ) : Effect()

        object LoadingDashboardEffect : Effect()
        data class UpdateActiveTrackersCountEffect(val count: Int) : Effect()
        data class UpdateTotalTrackersCountEffect(val count: Int) : Effect()
        data class UpdateLocationModeEffect(val mode: LocationMode) : Effect()
        data class UpdateInternetActivityModeEffect(val mode: InternetPrivacyMode) : Effect()
        data class UpdateAppsUsingLocationPermEffect(val apps: Int) : Effect()

        object OpenFakeMyLocationEffect : Effect()
        object OpenInternetActivityPrivacyEffect : Effect()
        object OpenAppsPermissionsEffect : Effect()
        object OpenTrackersEffect : Effect()
    }

    companion object {
        fun create(
            coroutineScope: CoroutineScope,
            getPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
            ipScramblingStateUseCase: IpScramblingStateUseCase
        ): DashboardFeature =
            DashboardFeature(
                initialState = State.DisabledState(),
                coroutineScope,
                reducer = { state, effect ->
                    if (state is State.LoadingState) state
                    else when (effect) {
                        is Effect.UpdateStateEffect -> when {
                            effect.isEnabled && state is State.EnabledState
                                || !effect.isEnabled && state is State.DisabledState -> state
                            effect.isEnabled && state is State.DisabledState -> State.EnabledState(
                                isAllTrackersBlocked = false,
                                locationMode = LocationMode.REAL_LOCATION,
                                internetPrivacyMode = InternetPrivacyMode.REAL_IP,
                                totalGraph = state.totalGraph,
                                // val graphData
                                trackersCount = state.trackersCount,
                                activeTrackersCount = state.activeTrackersCount
                            )
                            !effect.isEnabled && state is State.EnabledState -> State.DisabledState(
                                totalGraph = state.totalGraph,
                                // val graphData
                                trackersCount = state.trackersCount,
                                activeTrackersCount = state.activeTrackersCount
                            )
                            else -> state
                        }
                        is Effect.IpScramblingModeUpdatedEffect -> if (state is State.EnabledState) state.copy(internetPrivacyMode = effect.mode)
                        else state

                        /*is Effect.OpenDashboardEffect -> State.DashboardState(
                            effect.trackersCount,
                            effect.activeTrackersCount,
                            effect.totalApps,
                            effect.permissionCount,
                            effect.appsUsingLocationPerm,
                            effect.locationMode,
                            effect.internetPrivacyMode
                            )
                            Effect.LoadingDashboardEffect -> {
                                if (state is State.InitialState) {
                                    State.LoadingDashboardState
                                } else state
                            }
                            is Effect.UpdateActiveTrackersCountEffect -> {
                                if (state is State.DashboardState) {
                                    state.copy(activeTrackersCount = effect.count)
                                } else state
                            }
                            is Effect.UpdateTotalTrackersCountEffect -> {
                                if (state is State.DashboardState) {
                                    state.copy(trackersCount = effect.count)
                                } else state
                            }
                            is Effect.UpdateInternetActivityModeEffect -> {
                                if (state is State.DashboardState) {
                                    state.copy(internetPrivacyMode = effect.mode)
                                } else state
                            }
                            is Effect.UpdateLocationModeEffect -> {
                                if (state is State.DashboardState) {
                                    state.copy(locationMode = effect.mode)
                                } else state
                            }
                            is Effect.UpdateAppsUsingLocationPermEffect -> if (state is State.DashboardState) {
                                state.copy(appsUsingLocationPerm = effect.apps)
                            } else state

                            Effect.OpenFakeMyLocationEffect -> state
                            Effect.OpenAppsPermissionsEffect -> state
                            Effect.OpenInternetActivityPrivacyEffect -> state
                            Effect.OpenTrackersEffect -> state
                            */

                        else -> state
                    }
                },
                actor = { state: State, action: Action ->
                    Log.d("Feature", "action: $action")
                    when (action) {
                        Action.TogglePrivacyAction -> {
                            if (state != State.LoadingState) {
                                getPrivacyStateUseCase.toggle()
                            }
                            flowOf(Effect.NoEffect)
                        }

                        Action.InitAction -> merge(
                            getPrivacyStateUseCase.quickPrivacyEnabledFlow.map {

                                Effect.UpdateStateEffect(it)
                            },
                            ipScramblingStateUseCase.internetPrivacyMode.map {
                                Effect.IpScramblingModeUpdatedEffect(it)
                            }
                        )
                        /*
                            Action.ObserveDashboardAction -> {
                            merge(
                            TrackersDataSource.trackers.map {
                                var activeTrackersCount: Int = 0
                                outer@ for (tracker in it) {
                                    for (app in tracker.trackedApps) {
                                        if (!app.isEnabled) {
                                            continue@outer
                                        }
                                    }
                                    activeTrackersCount++
                                }
                                Effect.UpdateActiveTrackersCountEffect(activeTrackersCount)
                            },
                            TrackersDataSource.trackers.map {
                                Effect.UpdateTotalTrackersCountEffect(it.size)
                            },
                            DummyDataSource.appsUsingLocationPerm.map {
                                Effect.UpdateAppsUsingLocationPermEffect(it.size)
                            },
                            DummyDataSource.location.map {
                                Effect.UpdateLocationModeEffect(it.mode)
                            },
                            DummyDataSource.internetActivityMode.map {
                                Effect.UpdateInternetActivityModeEffect(it)
                            }
                        )
                        Action.ShowQuickPrivacyProtectionInfoAction -> flowOf(
                            Effect.OpenQuickPrivacyProtectionEffect
                        )
                        Action.ShowDashboardAction -> flow {
                            emit(Effect.LoadingDashboardEffect)
                            emit(
                                Effect.OpenDashboardEffect(
                                    DummyDataSource.trackersCount,
                                    DummyDataSource.activeTrackersCount.value,
                                    DummyDataSource.packages.size,
                                    DummyDataSource.permissions.size,
                                    DummyDataSource.appsUsingLocationPerm.value.size,
                                    DummyDataSource.location.value.mode,
                                    DummyDataSource.internetActivityMode.value
                                )
                            )
                        }*/
                        Action.ShowFakeMyLocationAction -> flowOf(Effect.OpenFakeMyLocationEffect)
                        Action.ShowAppsPermissions -> flowOf(Effect.OpenAppsPermissionsEffect)
                        Action.ShowInternetActivityPrivacyAction -> flowOf(
                            Effect.OpenInternetActivityPrivacyEffect
                        )
                        Action.ShowTrackers -> flowOf(Effect.OpenTrackersEffect)
                    }
                },
                singleEventProducer = { state, _, effect ->
                    Log.d("DashboardFeature", "$state, $effect")
                    when (effect) {
                        is Effect.OpenFakeMyLocationEffect ->
                            SingleEvent.NavigateToLocationSingleEvent
                        is Effect.OpenInternetActivityPrivacyEffect ->
                            SingleEvent.NavigateToInternetActivityPrivacySingleEvent
                        is Effect.OpenAppsPermissionsEffect ->
                            SingleEvent.NavigateToPermissionsSingleEvent
                        is Effect.OpenTrackersEffect ->
                            SingleEvent.NavigateToTrackersSingleEvent
                        else -> null
                    }
                }
            )
    }
}
