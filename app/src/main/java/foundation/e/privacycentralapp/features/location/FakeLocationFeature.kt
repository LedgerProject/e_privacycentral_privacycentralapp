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
import com.mapbox.mapboxsdk.geometry.LatLng
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.dummy.CityDataSource
import foundation.e.privacycentralapp.dummy.DummyDataSource
import foundation.e.privacycentralapp.dummy.Location
import foundation.e.privacycentralapp.dummy.LocationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf

// Define a state machine for Fake location feature
class FakeLocationFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>,
    private val locationApi: LocationApiDelegate
) : BaseFeature<FakeLocationFeature.State, FakeLocationFeature.Action, FakeLocationFeature.Effect, FakeLocationFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope,
    { message -> Log.d("FakeLocationFeature", message) },
    singleEventProducer
) {
    data class State(val location: Location)

    sealed class SingleEvent {
        object RandomLocationSelectedEvent : SingleEvent()
        object RealLocationSelectedEvent : SingleEvent()
        object SpecificLocationSavedEvent : SingleEvent()
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {

        // Action which is triggered everytime the location is updated.
        data class UpdateLocationAction(val latLng: LatLng) : Action()
        object UseRealLocationAction : Action()
        data class UseRandomLocationAction(
            val cities: Array<String>
        ) : Action() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as UseRandomLocationAction

                if (!cities.contentEquals(other.cities)) return false

                return true
            }

            override fun hashCode(): Int {
                return cities.contentHashCode()
            }
        }

        object UseSpecificLocationAction : Action()
        data class SetCustomFakeLocationAction(
            val latitude: Double,
            val longitude: Double
        ) : Action()
    }

    sealed class Effect {
        data class LocationUpdatedEffect(val latitude: Double, val longitude: Double) : Effect()
        object RealLocationSelectedEffect : Effect()
        object RandomLocationSelectedEffect : Effect()
        object SpecificLocationSelectedEffect : Effect()
        object SpecificLocationSavedEffect : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(
                Location(
                    LocationMode.REAL_LOCATION,
                    0.0,
                    0.0
                )
            ),
            coroutineScope: CoroutineScope,
            locationApi: LocationApiDelegate
        ) = FakeLocationFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    Effect.RandomLocationSelectedEffect -> state.copy(
                        location = state.location.copy(
                            mode = LocationMode.RANDOM_LOCATION
                        )
                    )
                    Effect.RealLocationSelectedEffect -> state.copy(
                        location = state.location.copy(
                            mode = LocationMode.REAL_LOCATION
                        )
                    )
                    is Effect.ErrorEffect, Effect.SpecificLocationSavedEffect -> state
                    is Effect.LocationUpdatedEffect -> state.copy(
                        location = state.location.copy(
                            latitude = effect.latitude,
                            longitude = effect.longitude
                        )
                    )
                    is Effect.SpecificLocationSelectedEffect -> state.copy(
                        location = state.location.copy(
                            mode = LocationMode.CUSTOM_LOCATION
                        )
                    )
                }
            },
            actor = { _, action ->
                when (action) {
                    is Action.UpdateLocationAction -> flowOf(
                        Effect.LocationUpdatedEffect(
                            action.latLng.latitude,
                            action.latLng.longitude
                        )
                    )
                    is Action.SetCustomFakeLocationAction -> {
                        val location = Location(
                            LocationMode.CUSTOM_LOCATION,
                            action.latitude,
                            action.longitude
                        )
                        locationApi.setFakeLocation(action.latitude, action.longitude)
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
                    is Action.UseRandomLocationAction -> {
                        val randomCity = CityDataSource.getRandomCity(action.cities)
                        locationApi.setFakeLocation(randomCity.latitude, randomCity.longitude)
                        val success = DummyDataSource.setLocationMode(
                            LocationMode.RANDOM_LOCATION,
                            randomCity.toRandomLocation()
                        )
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
                    is Action.UseRealLocationAction -> {
                        locationApi.startRealLocation()
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
                    is Action.UseSpecificLocationAction -> {
                        flowOf(Effect.SpecificLocationSelectedEffect)
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
            },
            locationApi = locationApi
        )
    }
}
