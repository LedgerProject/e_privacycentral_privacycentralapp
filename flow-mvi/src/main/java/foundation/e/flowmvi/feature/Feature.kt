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

package foundation.e.flowmvi.feature

import foundation.e.flowmvi.Logger
import foundation.e.flowmvi.MVIView
import foundation.e.flowmvi.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface Feature<State : Any, in Action : Any, SingleEvent : Any> : Store<State, Action> {
    val singleEvents: Flow<SingleEvent>

    /**
     * Call this method when a new [View] is ready to render the state of this MVFlow object.
     *
     * @param viewCoroutineScope the scope of the view. This will be used to launch a coroutine which will run listening
     * to actions until this scope is cancelled.
     * @param view the view that will render the state.
     * @param initialActions an optional list of Actions that can be passed to introduce an initial action into the
     * screen (for example, to trigger a refresh of data).
     * @param logger Optional [Logger] to log events inside this MVFlow object associated with this view (but not
     * others). If null, a default logger might be used.
     */
    fun takeView(
        viewCoroutineScope: CoroutineScope,
        view: MVIView<State, Action>,
        initialActions: List<Action> = emptyList(),
        logger: Logger? = null
    )

    /**
     * This method adds an external source of actions into the MVFlow object.
     *
     * This might be useful if you need to update your state based on things happening outside the [View], such as
     * timers, external database updates, push notifications, etc.
     *
     * @param actions the flow of events. You might want to have a look at
     * [kotlinx.coroutines.flow.callbackFlow].
     * @param logger Optional [Logger] to log events inside this MVFlow object associated with this external Flow (but
     * not others). If null, a default logger might be used.
     */
    fun addExternalActions(
        actions: Flow<Action>,
        logger: Logger? = null
    )
}
