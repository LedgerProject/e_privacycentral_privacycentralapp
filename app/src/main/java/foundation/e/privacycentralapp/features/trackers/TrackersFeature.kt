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
import foundation.e.privacycentralapp.domain.usecases.AppListUseCase
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import foundation.e.privacycentralapp.domain.usecases.TrackersStatisticsUseCase
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import foundation.e.privacymodules.trackers.Tracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

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
    { message -> Log.d("TrackersFeature", message) },
    singleEventProducer
) {
    data class State(
        val dayStatistics: List<Int>? = null,
        val dayTrackersCount: Int? = null,
        val monthStatistics: List<Int>? = null,
        val monthTrackersCount: Int? = null,
        val yearStatistics: List<Int>? = null,
        val yearTrackersCount: Int? = null,
        val apps: List<ApplicationDescription>? = null,

        val trackers: List<Tracker> = emptyList(),
        val currentSelectedTracker: Tracker? = null
    )

    sealed class SingleEvent {
        data class ErrorEvent(val error: String) : SingleEvent()
        data class OpenAppDetailsEvent(val appDesc: ApplicationDescription) : SingleEvent()
    }

    sealed class Action {
        object InitAction : Action()
        data class ClickAppAction(val packageName: String) : Action()
    }

    sealed class Effect {
        data class TrackersStatisticsLoadedEffect(
            val dayStatistics: List<Int>? = null,
            val dayTrackersCount: Int? = null,
            val monthStatistics: List<Int>? = null,
            val monthTrackersCount: Int? = null,
            val yearStatistics: List<Int>? = null,
            val yearTrackersCount: Int? = null
        ) : Effect()
        data class AvailableAppsListEffect(
            val apps: List<ApplicationDescription>
        ) : Effect()
        data class OpenAppDetailsEffect(val appDesc: ApplicationDescription) : Effect()
        object QuickPrivacyDisabledWarningEffect : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(),
            coroutineScope: CoroutineScope,
            getPrivacyStateUseCase: GetQuickPrivacyStateUseCase,
            trackersStatisticsUseCase: TrackersStatisticsUseCase,
            appListUseCase: AppListUseCase
        ) = TrackersFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.TrackersStatisticsLoadedEffect -> state.copy(
                        dayStatistics = effect.dayStatistics,
                        dayTrackersCount = effect.dayTrackersCount,
                        monthStatistics = effect.monthStatistics,
                        monthTrackersCount = effect.monthTrackersCount,
                        yearStatistics = effect.yearStatistics,
                        yearTrackersCount = effect.yearTrackersCount
                    )
                    is Effect.AvailableAppsListEffect -> state.copy(apps = effect.apps)

                    is Effect.ErrorEffect -> state
                    else -> state
                }
            },
            actor = { state, action ->
                when (action) {
                    Action.InitAction -> merge(
                        flow {
                            val statistics = trackersStatisticsUseCase.getDayMonthYearStatistics()
                            val counts = trackersStatisticsUseCase.getDayMonthYearCounts()
                            emit(
                                Effect.TrackersStatisticsLoadedEffect(
                                    dayStatistics = statistics.first,
                                    dayTrackersCount = counts.first,
                                    monthStatistics = statistics.second,
                                    monthTrackersCount = counts.second,
                                    yearStatistics = statistics.third,
                                    yearTrackersCount = counts.third
                                )
                            )
                        },
                        appListUseCase.getBlockableApps().map { apps ->
                            Effect.AvailableAppsListEffect(apps)
                        }
                    )

                    is Action.ClickAppAction -> flowOf(
                        if (getPrivacyStateUseCase.isQuickPrivacyEnabled) {
                            state.apps?.find { it.packageName == action.packageName }?.let {
                                Effect.OpenAppDetailsEffect(it)
                            } ?: run { Effect.ErrorEffect("Can't find back app.") }
                        } else Effect.QuickPrivacyDisabledWarningEffect
                    )
                }
            },
            singleEventProducer = { _, _, effect ->
                when (effect) {
                    is Effect.ErrorEffect -> SingleEvent.ErrorEvent(effect.message)
                    is Effect.OpenAppDetailsEffect -> SingleEvent.OpenAppDetailsEvent(effect.appDesc)
                    Effect.QuickPrivacyDisabledWarningEffect -> SingleEvent.ErrorEvent("Enabled Quick Privacy to use functionalities")
                    else -> null
                }
            }
        )
    }
}
