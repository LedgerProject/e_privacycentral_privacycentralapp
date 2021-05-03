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
import foundation.e.privacycentralapp.dummy.DummyDataSource
import foundation.e.privacycentralapp.dummy.Location
import foundation.e.privacycentralapp.dummy.LocationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Define a state machine for Fake location feature
class FakeLocationFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<FakeLocationFeature.State, FakeLocationFeature.Action, FakeLocationFeature.Effect, FakeLocationFeature.SingleEvent>(
    initialState, actor, reducer, coroutineScope, { message -> Log.d("FakeLocationFeature", message) },
    singleEventProducer
) {
    sealed class State {
        object InitialState : State()
        data class LocationState(val location: Location) : State()
    }

    sealed class SingleEvent {
        object RandomLocationSelectedEvent : SingleEvent()
        object RealLocationSelectedEvent : SingleEvent()
        object SpecificLocationSavedEvent : SingleEvent()
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {
        object ObserveLocationAction : Action()
        object UseRealLocationAction : Action()
        object UseRandomLocationAction : Action()
        object UseSpecificLocationAction : Action()
        data class AddSpecificLocationAction(val latitude: Double, val longitude: Double) : Action()
    }

    sealed class Effect {
        data class LocationUpdatedEffect(val location: Location) : Effect()
        object RealLocationSelectedEffect : Effect()
        object RandomLocationSelectedEffect : Effect()
        data class SpecificLocationSelectedEffect(val location: Location) : Effect()
        object SpecificLocationSavedEffect : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State.InitialState,
            coroutineScope: CoroutineScope
        ) = FakeLocationFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    Effect.RandomLocationSelectedEffect,
                    Effect.RealLocationSelectedEffect, is Effect.ErrorEffect, Effect.SpecificLocationSavedEffect -> state
                    is Effect.LocationUpdatedEffect -> State.LocationState(effect.location)
                    is Effect.SpecificLocationSelectedEffect -> State.LocationState(effect.location)
                }
            },
            actor = { _, action ->
                when (action) {
                    is Action.ObserveLocationAction -> DummyDataSource.location.map {
                        Effect.LocationUpdatedEffect(it)
                    }
                    is Action.AddSpecificLocationAction -> {
                        val location = Location(
                            LocationMode.CUSTOM_LOCATION,
                            action.latitude,
                            action.longitude
                        )
                        val success = DummyDataSource.setLocationMode(
                            LocationMode.CUSTOM_LOCATION,
                            location
                        )
                        if (success) {
                            flowOf(
                                Effect.SpecificLocationSavedEffect
                            )
                        } else {
                            flowOf(
                                Effect.ErrorEffect("Couldn't select location")
                            )
                        }
                    }
                    Action.UseRandomLocationAction -> {
                        val success = DummyDataSource.setLocationMode(LocationMode.RANDOM_LOCATION)
                        if (success) {
                            flowOf(
                                Effect.RandomLocationSelectedEffect
                            )
                        } else {
                            flowOf(
                                Effect.ErrorEffect("Couldn't select location")
                            )
                        }
                    }
                    Action.UseRealLocationAction -> {
                        val success = DummyDataSource.setLocationMode(LocationMode.REAL_LOCATION)
                        if (success) {
                            flowOf(
                                Effect.RealLocationSelectedEffect
                            )
                        } else {
                            flowOf(
                                Effect.ErrorEffect("Couldn't select location")
                            )
                        }
                    }
                    Action.UseSpecificLocationAction -> {
                        val location = DummyDataSource.location.value
                        flowOf(Effect.SpecificLocationSelectedEffect(location.copy(mode = LocationMode.CUSTOM_LOCATION)))
                    }
                }
            },
            singleEventProducer = { _, _, effect ->
                when (effect) {
                    Effect.RandomLocationSelectedEffect -> SingleEvent.RandomLocationSelectedEvent
                    Effect.SpecificLocationSavedEffect -> SingleEvent.SpecificLocationSavedEvent
                    Effect.RealLocationSelectedEffect -> SingleEvent.RealLocationSelectedEvent
                    is Effect.ErrorEffect -> SingleEvent.ErrorEvent(effect.message)
                    else -> null
                }
            }
        )
    }
}
