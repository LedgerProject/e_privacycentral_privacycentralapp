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

import foundation.e.flowmvi.Actor
import foundation.e.flowmvi.Logger
import foundation.e.flowmvi.MVIView
import foundation.e.flowmvi.Reducer
import foundation.e.flowmvi.SingleEventProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class BaseFeature<State : Any, in Action : Any, in Effect : Any, SingleEvent : Any>(
    initialState: State,
    private val actor: Actor<State, Action, Effect>,
    private val reducer: Reducer<State, Effect>,
    private val coroutineScope: CoroutineScope,
    private val defaultLogger: Logger = {},
    private val singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>? = null
) :
    Feature<State, Action, SingleEvent> {

    private val mutex = Mutex()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state.asStateFlow()

    private val singleEventChannel = Channel<SingleEvent>()
    override val singleEvents: Flow<SingleEvent> = singleEventChannel.receiveAsFlow()

    override fun takeView(
        viewCoroutineScope: CoroutineScope,
        view: MVIView<State, Action>,
        initialActions: List<Action>,
        logger: Logger?
    ) {
        viewCoroutineScope.launch {
            sendStateUpdatesIntoView(this, view, logger ?: defaultLogger)
            handleViewActions(this, view, initialActions, logger ?: defaultLogger)
        }
    }

    private fun sendStateUpdatesIntoView(
        callerCoroutineScope: CoroutineScope,
        view: MVIView<State, Action>,
        logger: Logger
    ) {
        state
            .onStart {
                logger.invoke("State flow started")
            }
            .onCompletion {
                logger.invoke("State flow completed")
            }
            .onEach {
                logger.invoke("New state: $it")
                view.render(it)
            }
            .launchIn(callerCoroutineScope)
    }

    private fun handleViewActions(
        coroutineScope: CoroutineScope,
        view: MVIView<State, Action>,
        initialActions: List<Action>,
        logger: Logger
    ) {
        coroutineScope.launch {
            view
                .actions()
                .onStart {
                    logger.invoke("View actions flow started")
                    emitAll(initialActions.asFlow())
                }
                .onCompletion {
                    logger.invoke("View actions flow completed")
                }
                .collectIntoHandler(this, logger)
        }
    }

    override fun addExternalActions(actions: Flow<Action>, logger: Logger?) {
        coroutineScope.launch {
            actions.collectIntoHandler(this, logger ?: defaultLogger)
        }
    }

    private suspend fun Flow<Action>.collectIntoHandler(
        callerCoroutineScope: CoroutineScope,
        logger: Logger
    ) {
        onEach { action ->
            callerCoroutineScope.launch {
                logger.invoke("Received action $action")
                actor.invoke(_state.value, action)
                    .onEach { effect ->
                        mutex.withLock {
                            logger.invoke("Applying effect $effect from action $action")
                            val newState = reducer.invoke(_state.value, effect)
                            _state.value = newState
                            singleEventProducer?.also {
                                it.invoke(newState, action, effect)?.let { singleEvent ->
                                    singleEventChannel.send(
                                        singleEvent
                                    )
                                }
                            }
                        }
                    }
                    .launchIn(coroutineScope)
            }
        }
            .launchIn(callerCoroutineScope)
    }
}

fun <State : Any, Action : Any, Effect : Any, SingleEvent : Any> feature(
    initialState: State,
    actor: Actor<State, Action, Effect>,
    reducer: Reducer<State, Effect>,
    coroutineScope: CoroutineScope,
    defaultLogger: Logger = {},
    singleEventProducer: SingleEventProducer<State, Action, Effect, SingleEvent>? = null
) = BaseFeature(
    initialState,
    actor,
    reducer,
    coroutineScope,
    defaultLogger,
    singleEventProducer
)
