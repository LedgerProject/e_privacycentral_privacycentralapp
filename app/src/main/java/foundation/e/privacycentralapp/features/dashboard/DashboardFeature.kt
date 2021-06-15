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
import foundation.e.privacycentralapp.dummy.DummyDataSource
import foundation.e.privacycentralapp.dummy.InternetPrivacyMode
import foundation.e.privacycentralapp.dummy.LocationMode
import foundation.e.privacycentralapp.dummy.TrackersDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
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
    sealed class State {
        object InitialState : State()
        object LoadingDashboardState : State()
        data class DashboardState(
            val trackersCount: Int,
            val activeTrackersCount: Int,
            val totalApps: Int,
            val permissionCount: Int,
            val appsUsingLocationPerm: Int,
            val locationMode: LocationMode,
            val internetPrivacyMode: InternetPrivacyMode
        ) : State()

        object QuickProtectionState : State()
    }

    sealed class SingleEvent {
        object NavigateToQuickProtectionSingleEvent : SingleEvent()
        object NavigateToTrackersSingleEvent : SingleEvent()
        object NavigateToInternetActivityPrivacySingleEvent : SingleEvent()
        object NavigateToLocationSingleEvent : SingleEvent()
        object NavigateToPermissionsSingleEvent : SingleEvent()
    }

    sealed class Action {
        object ShowQuickPrivacyProtectionInfoAction : Action()
        object ObserveDashboardAction : Action()
        object ShowDashboardAction : Action()
        object ShowFakeMyLocationAction : Action()
        object ShowInternetActivityPrivacyAction : Action()
        object ShowAppsPermissions : Action()
        object ShowTrackers : Action()
    }

    sealed class Effect {
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
        fun create(initialState: State, coroutineScope: CoroutineScope): DashboardFeature =
            DashboardFeature(
                initialState,
                coroutineScope,
                reducer = { state, effect ->
                    when (effect) {
                        Effect.OpenQuickPrivacyProtectionEffect -> State.QuickProtectionState
                        is Effect.OpenDashboardEffect -> State.DashboardState(
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
                    }
                },
                actor = { _: State, action: Action ->
                    Log.d("Feature", "action: $action")
                    when (action) {
                        Action.ObserveDashboardAction -> merge(
                            TrackersDataSource.trackers.map {
                                var activeTrackersCount: Int = 0
                                outer@ for (tracker in it) {
                                    for (app in tracker.trackedApps) {
                                        if(!app.isEnabled)  {
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
                        }
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
                    if (state is State.DashboardState && effect is Effect.OpenFakeMyLocationEffect)
                        SingleEvent.NavigateToLocationSingleEvent
                    else if (state is State.QuickProtectionState && effect is Effect.OpenQuickPrivacyProtectionEffect)
                        SingleEvent.NavigateToQuickProtectionSingleEvent
                    else if (state is State.DashboardState && effect is Effect.OpenInternetActivityPrivacyEffect)
                        SingleEvent.NavigateToInternetActivityPrivacySingleEvent
                    else if (state is State.DashboardState && effect is Effect.OpenAppsPermissionsEffect)
                        SingleEvent.NavigateToPermissionsSingleEvent
                    else if (state is State.DashboardState && effect is Effect.OpenTrackersEffect)
                        SingleEvent.NavigateToTrackersSingleEvent
                    else null
                }
            )
    }
}
