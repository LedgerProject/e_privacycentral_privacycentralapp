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

// Define a state machine for Dashboard Feature
object DashboardFeature {
    sealed class State {
        object DashboardState : State()
        object QuickProtectionState : State()
    }

    sealed class SingleEvent {
        object NavigateToQuickProtectionSingleEvent : SingleEvent()
        object NavigateToTrackersSingleEvent : SingleEvent()
        object NavigateToInternetActivityPolicySingleEvent : SingleEvent()
        object NavigateToLocationSingleEvent : SingleEvent()
        object NavigateToPermissionManagementSingleEvent : SingleEvent()
    }

    sealed class Action {
        object ShowQuickPrivacyProtectionInfoAction : Action()
        object ShowDashboardAction : Action()
    }

    sealed class Effect {
        object OpenQuickPrivacyProtectionEffect : Effect()
        object OpenDashboardEffect : Effect()
    }
}

private val reducer: Reducer<DashboardFeature.State, DashboardFeature.Effect> = { _, effect ->
    when (effect) {
        DashboardFeature.Effect.OpenQuickPrivacyProtectionEffect -> DashboardFeature.State.QuickProtectionState
        DashboardFeature.Effect.OpenDashboardEffect -> DashboardFeature.State.DashboardState
    }
}

private val actor: Actor<DashboardFeature.State, DashboardFeature.Action, DashboardFeature.Effect> =
    { _, action ->
        when (action) {
            DashboardFeature.Action.ShowQuickPrivacyProtectionInfoAction -> flowOf(DashboardFeature.Effect.OpenQuickPrivacyProtectionEffect)
            DashboardFeature.Action.ShowDashboardAction -> flowOf(DashboardFeature.Effect.OpenDashboardEffect)
        }
    }

fun homeFeature(
    initialState: DashboardFeature.State = DashboardFeature.State.DashboardState,
    coroutineScope: CoroutineScope
) = BaseFeature<DashboardFeature.State, DashboardFeature.Action, DashboardFeature.Effect, DashboardFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope
)
