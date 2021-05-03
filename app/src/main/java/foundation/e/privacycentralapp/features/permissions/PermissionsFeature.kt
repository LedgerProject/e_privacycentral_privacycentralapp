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

package foundation.e.privacycentralapp.features.permissions

import android.util.Log
import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import foundation.e.flowmvi.feature.BaseFeature
import foundation.e.privacycentralapp.dummy.DummyDataSource
import foundation.e.privacycentralapp.dummy.Permission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Define a state machine for Internet privacy feature
class PermissionsFeature(
    initialState: State,
    coroutineScope: CoroutineScope,
    reducer: Reducer<State, Effect>,
    actor: Actor<State, Action, Effect>,
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>
) : BaseFeature<PermissionsFeature.State, PermissionsFeature.Action, PermissionsFeature.Effect, PermissionsFeature.SingleEvent>(
    initialState,
    actor,
    reducer,
    coroutineScope,
    { message -> Log.d("PermissionsFeature", message) },
    singleEventProducer
) {
    data class State(
        val permissions: List<Permission> = emptyList(),
        val currentPermission: Permission? = null
    )

    sealed class SingleEvent {
        data class ErrorEvent(val error: String) : SingleEvent()
    }

    sealed class Action {
        object ObservePermissions : Action()
        data class LoadPermissionApps(val id: Int) : Action()
        data class TogglePermissionAction(
            val packageName: String,
            val grant: Boolean
        ) : Action()
    }

    sealed class Effect {
        data class PermissionsLoadedEffect(val permissions: List<Permission>) : Effect()
        data class PermissionLoadedEffect(val permission: Permission) : Effect()
        object PermissionToggledEffect : Effect()
        data class ErrorEffect(val message: String) : Effect()
    }

    companion object {
        fun create(
            initialState: State = State(),
            coroutineScope: CoroutineScope
        ) = PermissionsFeature(
            initialState, coroutineScope,
            reducer = { state, effect ->
                when (effect) {
                    is Effect.PermissionsLoadedEffect -> State(effect.permissions)
                    is Effect.PermissionLoadedEffect -> state.copy(currentPermission = effect.permission)
                    is Effect.ErrorEffect -> state
                    Effect.PermissionToggledEffect -> state
                }
            },
            actor = { state, action ->
                when (action) {
                    Action.ObservePermissions -> DummyDataSource.populatedPermission.map {
                        Effect.PermissionsLoadedEffect(it)
                    }
                    is Action.LoadPermissionApps -> flowOf(
                        Effect.PermissionLoadedEffect(
                            DummyDataSource.getPermission(action.id)
                        )
                    )

                    is Action.TogglePermissionAction -> {
                        if (state.currentPermission != null) {
                            DummyDataSource.togglePermission(
                                state.currentPermission.id,
                                action.packageName,
                                action.grant
                            )
                            flowOf(Effect.PermissionToggledEffect)
                        } else {
                            flowOf(Effect.ErrorEffect("Can't update permission"))
                        }
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
