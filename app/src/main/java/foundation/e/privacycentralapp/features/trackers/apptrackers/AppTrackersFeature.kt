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

package foundation.e.privacycentralapp.features.trackers.apptrackers

import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.domain.usecases.TrackersStateUseCase
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import foundation.e.privacymodules.trackers.Tracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

// Define a state machine for Tracker feature.
class AppTrackersFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<AppTrackersFeature.State, AppTrackersFeature.Action, AppTrackersFeature.Effect, AppTrackersFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope,
    { message -> Log.d("TrackersFeature", message) },
    singleEventProducer
) {
    data class State(
        val appDesc: ApplicationDescription? = null,
        val isBlockingActivated: Boolean = false,
        val trackers: List<Tracker>? = null,
        val whitelist: List<Int>? = null
    ) {
        fun getTrackersStatus(): List<Pair<Tracker, Boolean>>? {
            if (trackers != null && whitelist != null) {
                return trackers.map { it to (it.id !in whitelist) }
            } else {
                return null
            }
        }
    }

    sealed class SingleEvent {
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {
        data class InitAction(val packageName: String) : Action()
        data class BlockAllToggleAction(val isBlocked: Boolean) : Action()
        data class ToggleTrackerAction(val tracker: Tracker, val isBlocked: Boolean) : Action()
    }

    sealed class Effect {
        data class SetAppEffect(val appDesc: ApplicationDescription) : Effect()
        data class AppTrackersBlockingActivatedEffect(val isBlockingActivated: Boolean) : Effect()
        data class AvailableTrackersListEffect(
            val isBlockingActivated: Boolean,
            val trackers: List<Tracker>,
            val whitelist: List<Int>
        ) : Effect()
        data class TrackersWhitelistUpdateEffect(val whitelist: List<Int>) : Effect()

        // object QuickPrivacyDisabledWarningEffect : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(),
            coroutineScope: CoroutineScope,
            trackersStateUseCase: TrackersStateUseCase
        ) = AppTrackersFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.SetAppEffect -> state.copy(appDesc = effect.appDesc)
                    is Effect.AvailableTrackersListEffect -> state.copy(
                        isBlockingActivated = effect.isBlockingActivated,
                        trackers = effect.trackers,
                        whitelist = effect.whitelist
                    )

                    is Effect.AppTrackersBlockingActivatedEffect ->
                        state.copy(isBlockingActivated = effect.isBlockingActivated)

                    is Effect.TrackersWhitelistUpdateEffect ->
                        state.copy(whitelist = effect.whitelist)
                    is Effect.ErrorEffect -> state
                }
            },
            actor = { state, action ->
                when (action) {
                    is Action.InitAction -> merge(
                        flow {
                            val appDesc =
                                trackersStateUseCase.getApplicationPermission(action.packageName)
                            emit(Effect.SetAppEffect(appDesc))

                            emit(
                                Effect.AvailableTrackersListEffect(
                                    isBlockingActivated = !trackersStateUseCase.isWhitelisted(
                                        appDesc.uid
                                    ),
                                    trackers = trackersStateUseCase.getTrackers(appDesc.uid),
                                    whitelist = trackersStateUseCase.getTrackersWhitelistIds(appDesc.uid)
                                )
                            )
                        }
                    )
                    is Action.BlockAllToggleAction ->
                        state.appDesc?.uid?.let { appUid ->
                            flow {
                                trackersStateUseCase.toggleAppWhitelist(appUid, !action.isBlocked)

                                emit(
                                    Effect.AppTrackersBlockingActivatedEffect(
                                        !trackersStateUseCase.isWhitelisted(
                                            appUid
                                        )
                                    )
                                )
                            }
                        } ?: run { flowOf(Effect.ErrorEffect("No appDesc.")) }
                    is Action.ToggleTrackerAction -> {
                        state.appDesc?.uid?.let { appUid ->
                            flow {
                                trackersStateUseCase.blockTracker(
                                    appUid,
                                    action.tracker,
                                    action.isBlocked
                                )
                                emit(
                                    Effect.TrackersWhitelistUpdateEffect(
                                        trackersStateUseCase.getTrackersWhitelistIds(appUid)
                                    )
                                )
                            }
                        } ?: run { flowOf(Effect.ErrorEffect("No appDesc.")) }
                    }
                }
            },
            singleEventProducer = { _, _, effect ->
                when (effect) {
                    is Effect.ErrorEffect -> SingleEvent.ErrorEvent(effect.message)
                    else -> null
                }
            }
        )
    }
}
