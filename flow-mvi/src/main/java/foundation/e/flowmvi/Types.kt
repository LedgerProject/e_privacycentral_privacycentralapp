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

package foundation.e.flowmvi

import kotlinx.coroutines.flow.Flow

/**
 * Actor is a function that receives the current state and the action which just happened
 * and acts on it.
 *
 * It returns a [Flow] of Effects which then can be used in a reducer to reduce to a new state.
 */
typealias Actor<State, Action, Effect> = (state: State, action: Action) -> Flow<Effect>

/**
 * Reducer is a function that applies the effect to current state and return a new state.
 *
 * This function should be free from any side-effect and make sure it is idempotent.
 */
typealias Reducer<State, Effect> = (state: State, effect: Effect) -> State

typealias SingleEventProducer<State, Action, Effect, SingleEvent> = (state: State, action: Action, effect: Effect) -> SingleEvent?

/**
 * Logger is function used for logging
 */
typealias Logger = (String) -> Unit
