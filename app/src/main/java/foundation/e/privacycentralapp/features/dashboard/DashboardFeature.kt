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
import foundation.e.privacycentralapp.domain.usecases.FakeLocationStateUseCase
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import foundation.e.privacycentralapp.domain.usecases.IpScramblingStateUseCase
import foundation.e.privacycentralapp.domain.usecases.TrackersStateUseCase
import foundation.e.privacycentralapp.domain.usecases.TrackersStatisticsUseCase
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
    data class State(
        val isQuickPrivacyEnabled: Boolean = false,
        val isAllTrackersBlocked: Boolean = false,
        val locationMode: LocationMode = LocationMode.REAL_LOCATION,
        val internetPrivacyMode: InternetPrivacyMode = InternetPrivacyMode.REAL_IP,
        val totalGraph: Int? = null,
        // val graphData
        val trackersCount: Int? = null,
        val dayTrackersCount: Int? = null,
        val dayStatistics: List<Int>? = null
    )

    sealed class SingleEvent {
        object NavigateToTrackersSingleEvent : SingleEvent()
        object NavigateToInternetActivityPrivacySingleEvent : SingleEvent()
        object NavigateToLocationSingleEvent : SingleEvent()
        object NavigateToPermissionsSingleEvent : SingleEvent()
    }

    sealed class Action {
        object InitAction : Action()
        object TogglePrivacyAction : Action()
        object ShowFakeMyLocationAction : Action()
        object ShowInternetActivityPrivacyAction : Action()
        object ShowAppsPermissions : Action()
        object ShowTrackers : Action()
    }

    sealed class Effect {
        object NoEffect : Effect()
        data class UpdateStateEffect(val isEnabled: Boolean) : Effect()
        data class IpScramblingModeUpdatedEffect(val mode: InternetPrivacyMode) : Effect()
        data class TrackersStatisticsUpdatedEffect(
            val dayStatistics: List<Int>,
            val dayTrackersCount: Int,
            val trackersCount: Int
        ) : Effect()
        data class TrackersBlockedUpdatedEffect(val areAllTrackersBlocked: Boolean) : Effect()
        data class UpdateLocationModeEffect(val mode: LocationMode) : Effect()
        object OpenFakeMyLocationEffect : Effect()
        object OpenInternetActivityPrivacyEffect : Effect()
        object OpenAppsPermissionsEffect : Effect()
        object OpenTrackersEffect : Effect()
    }

    companion object {
        fun create(
            coroutineScope: CoroutineScope,
            getPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
            ipScramblingStateUseCase: IpScramblingStateUseCase,
            trackersStatisticsUseCase: TrackersStatisticsUseCase,
            trackersStateUseCase: TrackersStateUseCase,
            fakeLocationStateUseCase: FakeLocationStateUseCase
        ): DashboardFeature =
            DashboardFeature(
                initialState = State(),
                coroutineScope,
                reducer = { state, effect ->
                    when (effect) {
                        is Effect.UpdateStateEffect -> state.copy(isQuickPrivacyEnabled = effect.isEnabled)
                        is Effect.IpScramblingModeUpdatedEffect -> state.copy(internetPrivacyMode = effect.mode)
                        is Effect.TrackersStatisticsUpdatedEffect -> state.copy(
                            dayStatistics = effect.dayStatistics,
                            dayTrackersCount = effect.dayTrackersCount,
                            trackersCount = effect.trackersCount
                        )

                        is Effect.TrackersBlockedUpdatedEffect -> state.copy(
                            isAllTrackersBlocked = effect.areAllTrackersBlocked
                        )
                        is Effect.UpdateLocationModeEffect -> state.copy(locationMode = effect.mode)

                        else -> state
                    }
                },
                actor = { _: State, action: Action ->
                    Log.d("Feature", "action: $action")
                    when (action) {
                        Action.TogglePrivacyAction -> {
                            getPrivacyStateUseCase.toggle()
                            flowOf(Effect.NoEffect)
                        }

                        Action.InitAction -> merge(
                            getPrivacyStateUseCase.quickPrivacyEnabledFlow.map {

                                Effect.UpdateStateEffect(it)
                            },
                            ipScramblingStateUseCase.internetPrivacyMode.map {
                                Effect.IpScramblingModeUpdatedEffect(it)
                            },
                            flow {
                                emit(
                                    Effect.TrackersStatisticsUpdatedEffect(
                                        dayStatistics = trackersStatisticsUseCase.getPastDayTrackersCalls(),
                                        dayTrackersCount = trackersStatisticsUseCase.getPastDayTrackersCount(),
                                        trackersCount = trackersStatisticsUseCase.getTrackersCount()
                                    )
                                )
                            },
                            trackersStateUseCase.areAllTrackersBlocked.map {
                                Effect.TrackersBlockedUpdatedEffect(it)
                            },
                            fakeLocationStateUseCase.locationMode.map {
                                Effect.UpdateLocationModeEffect(it)
                            }
                        )
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
