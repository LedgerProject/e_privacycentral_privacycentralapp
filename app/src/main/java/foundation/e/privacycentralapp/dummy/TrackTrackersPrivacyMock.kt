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

import foundation.e.privacymodules.trackers.IBlockTrackersPrivacyModule
import foundation.e.privacymodules.trackers.ITrackTrackersPrivacyModule
import foundation.e.privacymodules.trackers.Tracker

class TrackTrackersPrivacyMock :
    ITrackTrackersPrivacyModule,
    IBlockTrackersPrivacyModule {

    private val trackers = listOf(
        Tracker(1, "Crashlytics", null),
        Tracker(2, label = "Facebook", null)
    )

    override fun getPastDayTrackersCalls(): List<Int> {
        return listOf(
            2000, 2300, 130, 2500, 1000, 2000,
            2000, 2300, 130, 2500, 1000, 2000,
            2000, 2300, 130, 2500, 1000, 2000,
            2000, 2300, 130, 2500, 1000, 2000
        )
    }

    override fun getPastDayTrackersCount(): Int {
        return 30
    }

    override fun getPastMonthTrackersCalls(): List<Int> {
        return listOf(
            20000, 23000, 24130, 12500, 31000, 22000,
            20000, 23000, 24130, 12500, 31000, 22000,
            20000, 23000, 24130, 12500, 31000, 22000,
            20000, 23000, 24130, 12500, 31000, 22000,
            20000, 23000, 24130, 12500, 31000, 22000
        )
    }

    override fun getPastMonthTrackersCount(): Int {
        return 43
    }

    override fun getPastYearTrackersCalls(): List<Int> {
        return listOf(
            620000, 823000, 424130, 712500, 831000, 922000,
            620000, 823000, 424130, 712500, 831000, 922000
        )
    }

    override fun getPastYearTrackersCount(): Int {
        return 46
    }

    override fun getTrackersCount(): Int {
        return 72
    }

    override fun getTrackersForApp(appUid: Int): List<Tracker> {
        return trackers
    }

    private var isBlockingEnabled = false
    private val appWhitelist = mutableSetOf<Int>()
    private val trackersWhitelist = mutableMapOf<Int, MutableSet<Tracker>>()

    override fun addListener(listener: IBlockTrackersPrivacyModule.Listener) {
        TODO("Not yet implemented")
    }

    override fun removeListener(listener: IBlockTrackersPrivacyModule.Listener) {
        TODO("Not yet implemented")
    }

    override fun clearListeners() {
        TODO("Not yet implemented")
    }

    override fun disableBlocking() {}

    override fun enableBlocking() {}

    override fun getWhiteList(appUid: Int): List<Tracker> {
        return trackersWhitelist[appUid]?.toList() ?: emptyList()
    }

    override fun getWhiteListedApp(): List<Int> {
        return appWhitelist.toList()
    }

    override fun isBlockingEnabled(): Boolean {
        return isBlockingEnabled
    }

    override fun isWhiteListEmpty(): Boolean {
        return appWhitelist.isEmpty() &&
            (trackersWhitelist.isEmpty() || trackersWhitelist.values.all { it.isEmpty() })
    }

    override fun isWhitelisted(appUid: Int): Boolean {
        return appUid in appWhitelist
    }

    override fun setWhiteListed(tracker: Tracker, appUid: Int, isWhiteListed: Boolean) {
        if (appUid !in trackersWhitelist) {
            trackersWhitelist[appUid] = mutableSetOf<Tracker>()
        }

        if (isWhiteListed) {
            trackersWhitelist[appUid]?.add(tracker)
        } else {
            trackersWhitelist[appUid]?.remove(tracker)
        }
    }

    override fun setWhiteListed(appUid: Int, isWhiteListed: Boolean) {
        if (isWhiteListed) {
            appWhitelist.add(appUid)
        } else {
            appWhitelist.remove(appUid)
        }
    }
}
