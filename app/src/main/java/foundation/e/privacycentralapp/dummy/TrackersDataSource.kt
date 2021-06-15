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

package foundation.e.privacycentralapp.dummy

import foundation.e.privacycentralapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lineageos.blockers.BlockerInterface

data class TrackedApp(val appName: String, val isEnabled: Boolean, val iconId: Int)

data class Tracker(
    val name: String,
    val domain: String? = null,
    val ipAddress: String? = null,
    val trackedApps: List<TrackedApp>
)

object TrackersDataSource {

    private lateinit var blockerService: BlockerInterface

    val facebook = TrackedApp("Facebook", true, R.drawable.ic_facebook)
    val firefox = TrackedApp("Firefox", true, R.drawable.ic_facebook)
    val google = TrackedApp("Google", true, R.drawable.ic_facebook)
    val whatsapp = TrackedApp("Whatsapp", true, R.drawable.ic_facebook)
    val blisslauncher = TrackedApp("BlissLauncher", true, R.drawable.ic_facebook)
    val youtube = TrackedApp("Youtube", true, R.drawable.ic_facebook)

    val crashlytics = Tracker(
        "Google Crashlytics (Demo)",
        domain = "google.com",
        trackedApps = listOf(facebook, firefox)
    )

    val facebookAds = Tracker(
        "Facebook (Demo)",
        domain = "google.com",
        trackedApps = listOf(facebook, whatsapp)
    )
    val rubiconTracker = Tracker(
        "Rubicon Projects",
        domain = "google.com",
        trackedApps = listOf(google, blisslauncher, youtube)
    )
    val googleAnalytics = Tracker(
        "Google Analytics",
        domain = "google.com",
        trackedApps = listOf(facebook, firefox)
    )

    val _trackers =
        MutableStateFlow(listOf(crashlytics, facebookAds, rubiconTracker, googleAnalytics))
    val trackers = _trackers.asStateFlow()

    fun injectBlockerService(blockerInterface: BlockerInterface) {
        this.blockerService = blockerInterface
    }

    fun toggleTracker(tracker: Tracker, enable: Boolean): Boolean {
        val result = if (!enable) {
            blockerService.blockDomain(tracker.domain)
        } else {
            blockerService.unblockDomain(tracker.domain)
        }

        if (result) {
            _trackers.value = _trackers.value.map {
                if (it.name == tracker.name) {
                    it.copy(
                        trackedApps = it.trackedApps.map { app ->
                            app.copy(isEnabled = enable)
                        }
                    )
                } else it
            }
        }
        return result
    }
}
