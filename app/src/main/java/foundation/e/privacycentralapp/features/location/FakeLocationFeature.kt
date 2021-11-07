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

package foundation.e.privacycentralapp.features.location

import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.domain.entities.LocationMode
import foundation.e.privacycentralapp.domain.usecases.FakeLocationStateUseCase
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

// Define a state machine for Fake location feature
class FakeLocationFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<FakeLocationFeature.State, FakeLocationFeature.Action, FakeLocationFeature.Effect, FakeLocationFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope,
    { message -> Log.d("FakeLocationFeature", message) },
    singleEventProducer
) {
    data class State(
        val isEnabled: Boolean,
        val mode: LocationMode,
        val specificLatitude: Float? = null,
        val specificLongitude: Float? = null
    )

    sealed class SingleEvent {
        object RandomLocationSelectedEvent : SingleEvent()
        object RealLocationSelectedEvent : SingleEvent()
        object SpecificLocationSavedEvent : SingleEvent()
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {
        object Init : Action()

        // Action which is triggered everytime the location is updated.
        //data class UpdateLocationAction(val latLng: LatLng) : Action()

        object UseRealLocationAction : Action()
        object UseRandomLocationAction : Action()
        data class SetSpecificLocationAction(
            val latitude: Float,
            val longitude: Float) : Action()
    }

    sealed class Effect {
        data class QuickPrivacyUpdatedEffect(val isEnabled: Boolean): Effect()
        data class LocationModeUpdatedEffect(
            val mode: LocationMode,
            val latitude: Float? = null,
            val longitude: Float? = null) : Effect()
        data class ErrorEffect(val message: String) : Effect()
        object QuickPrivacyDisabledWarningEffect : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(
                isEnabled = false,
                mode = LocationMode.REAL_LOCATION
            ),
            getQuickPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
            fakeLocationStateUseCase: FakeLocationStateUseCase,
            coroutineScope: CoroutineScope
        ) = FakeLocationFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.QuickPrivacyUpdatedEffect -> state.copy(isEnabled = effect.isEnabled)
                    is Effect.LocationModeUpdatedEffect -> state.copy(
                        mode = effect.mode,
                        specificLatitude = effect.latitude,
                        specificLongitude = effect.longitude)

                    is Effect.ErrorEffect,
                    Effect.QuickPrivacyDisabledWarningEffect -> state
                }
            },
            actor = { state, action ->
                when (action) {
                    is Action.Init -> merge(
                        getQuickPrivacyStateUseCase.quickPrivacyEnabledFlow.map { Effect.QuickPrivacyUpdatedEffect(it) },
                        flowOf(Effect.LocationModeUpdatedEffect(fakeLocationStateUseCase.getLocationMode())))

                    // is Action.UpdateLocationAction -> flowOf(
                    //         Effect.LocationUpdatedEffect(
                    //             action.latLng.latitude,
                    //             action.latLng.longitude
                    //         )
                    //         )

                    is Action.SetSpecificLocationAction -> {
                        if (state.isEnabled) {
                            fakeLocationStateUseCase.setSpecificLocation(
                                action.latitude,
                                action.longitude
                            )
                            flowOf(
                                Effect.LocationModeUpdatedEffect(
                                    mode = LocationMode.SPECIFIC_LOCATION,
                                    latitude = action.latitude,
                                    longitude = action.longitude
                                )
                            )
                        } else flowOf(Effect.QuickPrivacyDisabledWarningEffect)
                    }
                    is Action.UseRandomLocationAction -> {
                        if (state.isEnabled) {
                        fakeLocationStateUseCase.setRandomLocation()
                        flowOf(Effect.LocationModeUpdatedEffect(LocationMode.RANDOM_LOCATION))
                    } else flowOf(Effect.QuickPrivacyDisabledWarningEffect)
                    }
                    is Action.UseRealLocationAction -> {
                        if (state.isEnabled) {
                            fakeLocationStateUseCase.stopFakeLocation()
                        flowOf(Effect.LocationModeUpdatedEffect(LocationMode.REAL_LOCATION))
                        } else flowOf(Effect.QuickPrivacyDisabledWarningEffect)
                    }
                }
            },
            singleEventProducer = { _, _, effect ->
                when (effect) {
                    Effect.QuickPrivacyDisabledWarningEffect ->
                        SingleEvent.ErrorEvent("Enabled Quick Privacy to use functionalities")
                    is Effect.ErrorEffect -> SingleEvent.ErrorEvent(effect.message)
                    else -> null
                }
            }
        )
    }
}
