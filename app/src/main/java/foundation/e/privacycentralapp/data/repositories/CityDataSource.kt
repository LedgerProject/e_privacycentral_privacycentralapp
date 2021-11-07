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

object CityDataSource {
    private val BARCELONA = Pair(41.3851f, 2.1734f)
    private val BUDAPEST = Pair(47.4979f, 19.0402f)
    private val ABU_DHABI = Pair(24.4539f, 54.3773f)
    private val HYDERABAD = Pair(17.3850f, 78.4867f)
    private val QUEZON_CITY = Pair(14.6760f, 121.0437f)
    private val PARIS = Pair(48.8566f, 2.3522f)
    private val LONDON = Pair(51.5074f, 0.1278f)
    private val SHANGHAI = Pair(31.2304f, 121.4737f)
    private val MADRID = Pair(40.4168f, 3.7038f)
    private val LAHORE = Pair(31.5204f, 74.3587f)
    private val CHICAGO = Pair(41.8781f, 87.6298f)

    val citiesLocationsList = listOf(
        BARCELONA,
        BUDAPEST,
        ABU_DHABI,
        HYDERABAD,
        QUEZON_CITY,
        PARIS,
        LONDON,
        SHANGHAI,
        MADRID,
        LAHORE,
        CHICAGO
    )
}
