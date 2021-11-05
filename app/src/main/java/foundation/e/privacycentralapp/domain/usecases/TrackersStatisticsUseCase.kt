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

import foundation.e.privacymodules.trackers.ITrackTrackersPrivacyModule

class TrackersStatisticsUseCase(
    private val trackTrackersPrivacyModule: ITrackTrackersPrivacyModule
) {

    fun getPastDayTrackersCalls(): List<Int> {
        return trackTrackersPrivacyModule.getPastDayTrackersCalls()
    }

    fun getDayMonthYearStatistics(): Triple<List<Int>, List<Int>, List<Int>> {
        return Triple(
            trackTrackersPrivacyModule.getPastDayTrackersCalls(),
            trackTrackersPrivacyModule.getPastMonthTrackersCalls(),
            trackTrackersPrivacyModule.getPastYearTrackersCalls()
        )
    }

    fun getDayMonthYearCounts(): Triple<Int, Int, Int> {
        return Triple(
            trackTrackersPrivacyModule.getPastDayTrackersCount(),
            trackTrackersPrivacyModule.getPastMonthTrackersCount(),
            trackTrackersPrivacyModule.getPastYearTrackersCount()
        )
    }
}
