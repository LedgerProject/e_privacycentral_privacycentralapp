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

import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.feature.BaseFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf

// Define a state machine for HomeFeature
object HomeFeature {
    sealed class State {
        object Loading : State()
        object Loaded : State()
    }

    sealed class SingleEvent {
        object NavigateToQuickProtection : SingleEvent()
        object NavigateToTrackers : SingleEvent()
        object NavigateToInternetActivityPolicy : SingleEvent()
        object NavigateToLocation : SingleEvent()
        object NavigateToPermissionManagement : SingleEvent()
    }

    sealed class Action {
        object load : Action()
    }

    sealed class Effect {
        object LoadingFinished : Effect()
    }
}

private val reducer: Reducer<HomeFeature.State, HomeFeature.Effect> = { _, effect ->
    when (effect) {
        HomeFeature.Effect.LoadingFinished -> HomeFeature.State.Loaded
    }
}

private val actor: Actor<HomeFeature.State, HomeFeature.Action, HomeFeature.Effect> =
    { _, action ->
        when (action) {
            HomeFeature.Action.load -> flowOf(HomeFeature.Effect.LoadingFinished)
        }
    }

fun homeFeature(
    initialState: HomeFeature.State = HomeFeature.State.Loading,
    coroutineScope: CoroutineScope
) = BaseFeature<HomeFeature.State, HomeFeature.Action, HomeFeature.Effect, HomeFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope
)
