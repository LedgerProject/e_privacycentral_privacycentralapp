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

import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.dummy.DummyDataSource
import foundation.e.privacycentralapp.dummy.InternetPrivacyMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

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
    { message -> Log.d("FakeLocationFeature", message) },
    singleEventProducer
) {
    data class State(val mode: InternetPrivacyMode)

    sealed class SingleEvent {
        object RealIPSelectedEvent : SingleEvent()
        object HiddenIPSelectedEvent : SingleEvent()
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {
        object LoadInternetModeAction : Action()
        object UseRealIPAction : Action()
        object UseHiddenIPAction : Action()
    }

    sealed class Effect {
        data class ModeUpdatedEffect(val mode: InternetPrivacyMode) : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(InternetPrivacyMode.REAL_IP),
            coroutineScope: CoroutineScope
        ) = InternetPrivacyFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.ModeUpdatedEffect -> state.copy(mode = effect.mode)
                    is Effect.ErrorEffect -> state
                }
            },
            actor = { _, action ->
                when (action) {
                    Action.LoadInternetModeAction -> flowOf(Effect.ModeUpdatedEffect(DummyDataSource.internetActivityMode.value))
                    Action.UseHiddenIPAction, Action.UseRealIPAction -> flow {
                        val success =
                            DummyDataSource.setInternetPrivacyMode(if (action is Action.UseHiddenIPAction) InternetPrivacyMode.HIDE_IP else InternetPrivacyMode.REAL_IP)
                        emit(
                            if (success) Effect.ModeUpdatedEffect(DummyDataSource.internetActivityMode.value) else Effect.ErrorEffect(
                                "Couldn't update internet mode"
                            )
                        )
                    }
                }
            },
            singleEventProducer = { _, action, effect ->
                when (action) {
                    Action.UseRealIPAction, Action.UseHiddenIPAction -> when (effect) {
                        is Effect.ModeUpdatedEffect -> {
                            if (effect.mode == InternetPrivacyMode.REAL_IP) {
                                SingleEvent.RealIPSelectedEvent
                            } else {
                                SingleEvent.HiddenIPSelectedEvent
                            }
                        }
                        is Effect.ErrorEffect -> {
                            SingleEvent.ErrorEvent(effect.message)
                        }
                    }
                    else -> null
                }
            }
        )
    }
}
