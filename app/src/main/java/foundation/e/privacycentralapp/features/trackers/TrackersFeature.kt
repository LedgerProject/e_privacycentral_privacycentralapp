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

package foundation.e.privacycentralapp.features.trackers

import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.dummy.Tracker
import foundation.e.privacycentralapp.dummy.TrackersDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Define a state machine for Tracker feature.
class TrackersFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<TrackersFeature.State, TrackersFeature.Action, TrackersFeature.Effect, TrackersFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope,
    { message -> Log.d("PermissionsFeature", message) },
    singleEventProducer
) {
    data class State(
        val trackers: List<Tracker> = emptyList(),
        val currentSelectedTracker: Tracker? = null
    )

    sealed class SingleEvent {
        data class ErrorEvent(val error: String) : SingleEvent()
        object BlockerErrorEvent : SingleEvent()
    }

    sealed class Action {
        object ObserveTrackers : Action()
        data class SetSelectedTracker(val tracker: Tracker) : Action()
        data class ToggleTrackerAction(
            val tracker: Tracker,
            val grant: Boolean
        ) : Action()
    }

    sealed class Effect {
        data class TrackersLoadedEffect(val trackers: List<Tracker>) : Effect()
        data class TrackerSelectedEffect(val tracker: Tracker) : Effect()
        data class TrackerToggleEffect(val result: Boolean) : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(),
            coroutineScope: CoroutineScope
        ) = TrackersFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.TrackersLoadedEffect -> State(effect.trackers)
                    is Effect.TrackerSelectedEffect -> state.copy(currentSelectedTracker = effect.tracker)
                    is Effect.ErrorEffect -> state
                    is Effect.TrackerToggleEffect -> state
                }
            },
            actor = { state, action ->
                when (action) {
                    Action.ObserveTrackers -> TrackersDataSource.trackers.map {
                        Effect.TrackersLoadedEffect(
                            it
                        )
                    }
                    is Action.SetSelectedTracker -> flowOf(
                        Effect.TrackerSelectedEffect(
                            action.tracker
                        )
                    )

                    is Action.ToggleTrackerAction -> {
                        if (state.currentSelectedTracker != null) {
                            val result = TrackersDataSource.toggleTracker(
                                state.currentSelectedTracker,
                                action.grant
                            )
                            flowOf(Effect.TrackerToggleEffect(result))
                        } else {
                            flowOf(Effect.ErrorEffect("Can't toggle tracker"))
                        }
                    }
                }
            },
            singleEventProducer = { _, _, effect ->
                when (effect) {
                    is Effect.ErrorEffect -> SingleEvent.ErrorEvent(effect.message)
                    is Effect.TrackerToggleEffect -> SingleEvent.BlockerErrorEvent
                    else -> null
                }
            }
        )
    }
}
