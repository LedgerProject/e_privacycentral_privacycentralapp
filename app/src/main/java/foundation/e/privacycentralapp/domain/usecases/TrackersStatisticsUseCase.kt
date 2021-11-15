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
        return trackTrackersPrivacyModule.getPastDayTrackersCalls().pruneEmptyHistoric()
    }

    fun getDayMonthYearStatistics(): Triple<List<Int>, List<Int>, List<Int>> {
        return Triple(
            trackTrackersPrivacyModule.getPastDayTrackersCalls().pruneEmptyHistoric(),
            trackTrackersPrivacyModule.getPastMonthTrackersCalls().pruneEmptyHistoric(),
            trackTrackersPrivacyModule.getPastYearTrackersCalls().pruneEmptyHistoric()
        )
    }

    fun getDayMonthYearCounts(): Triple<Int, Int, Int> {
        return Triple(
            trackTrackersPrivacyModule.getPastDayTrackersCount(),
            trackTrackersPrivacyModule.getPastMonthTrackersCount(),
            trackTrackersPrivacyModule.getPastYearTrackersCount()
        )
    }

    fun getPastDayTrackersCount(): Int {
        return trackTrackersPrivacyModule.getPastDayTrackersCount()
    }

    fun getTrackersCount(): Int {
        return trackTrackersPrivacyModule.getTrackersCount()
    }

    private fun List<Int>.pruneEmptyHistoric(): List<Int> {
        val result = mutableListOf<Int>()
        reversed().forEach {
            if (result.isNotEmpty() || it != 0) {
                result.add(it)
            }
        }
        if (result.isEmpty() && !isEmpty()) {
            result.add(last())
        }
        return result
    }
}
