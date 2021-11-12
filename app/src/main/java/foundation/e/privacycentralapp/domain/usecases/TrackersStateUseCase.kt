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

package foundation.e.privacycentralapp.domain.usecases

import foundation.e.privacycentralapp.data.repositories.LocalStateRepository
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import foundation.e.privacymodules.trackers.IBlockTrackersPrivacyModule
import foundation.e.privacymodules.trackers.ITrackTrackersPrivacyModule
import foundation.e.privacymodules.trackers.Tracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TrackersStateUseCase(
    private val blockTrackersPrivacyModule: IBlockTrackersPrivacyModule,
    private val trackersPrivacyModule: ITrackTrackersPrivacyModule,
    private val permissionsPrivacyModule: PermissionsPrivacyModule,
    private val localStateRepository: LocalStateRepository,
    coroutineScope: CoroutineScope
) {

    private val _areAllTrackersBlocked = MutableStateFlow(
        blockTrackersPrivacyModule.isBlockingEnabled() &&
            blockTrackersPrivacyModule.isWhiteListEmpty()
    )
    val areAllTrackersBlocked: StateFlow<Boolean> = _areAllTrackersBlocked

    init {
        coroutineScope.launch {
            localStateRepository.quickPrivacyEnabledFlow.collect { enabled ->
                if (enabled) {
                    blockTrackersPrivacyModule.enableBlocking()
                } else {
                    blockTrackersPrivacyModule.disableBlocking()
                }
                updateAllTrackersBlockedState()
            }
        }
    }

    private fun updateAllTrackersBlockedState() {
        _areAllTrackersBlocked.value = blockTrackersPrivacyModule.isBlockingEnabled() &&
            blockTrackersPrivacyModule.isWhiteListEmpty()
    }

    fun getApplicationPermission(packageName: String): ApplicationDescription {
        return permissionsPrivacyModule.getApplicationDescription(packageName)
    }

    fun getTrackers(appUid: Int): List<Tracker> {
        return trackersPrivacyModule.getTrackersForApp(appUid)
    }

    fun isWhitelisted(appUid: Int): Boolean {
        return blockTrackersPrivacyModule.isWhitelisted(appUid)
    }

    fun getTrackersWhitelistIds(appUid: Int): List<Int> {
        return blockTrackersPrivacyModule.getWhiteList(appUid).map { it.id }
    }

    fun toggleAppWhitelist(appUid: Int, isWhitelisted: Boolean) {
        blockTrackersPrivacyModule.setWhiteListed(appUid, isWhitelisted)
        updateAllTrackersBlockedState()
    }

    fun blockTracker(appUid: Int, tracker: Tracker, isBlocked: Boolean) {
        blockTrackersPrivacyModule.setWhiteListed(tracker, appUid, !isBlocked)
        updateAllTrackersBlockedState()
    }
}
