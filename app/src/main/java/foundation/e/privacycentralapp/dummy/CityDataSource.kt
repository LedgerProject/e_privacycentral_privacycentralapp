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

import kotlin.random.Random

data class City(val name: String, val latitude: Double, val longitude: Double) {

    fun toRandomLocation(): Location {
        return Location(LocationMode.RANDOM_LOCATION, this.latitude, this.longitude)
    }
}

object CityDataSource {
    private val BARCELONA = Pair(41.3851, 2.1734)
    private val BUDAPEST = Pair(47.4979, 19.0402)
    private val ABU_DHABI = Pair(24.4539, 54.3773)
    private val HYDERABAD = Pair(17.3850, 78.4867)
    private val QUEZON_CITY = Pair(14.6760, 121.0437)
    private val PARIS = Pair(48.8566, 2.3522)
    private val LONDON = Pair(51.5074, 0.1278)
    private val SHANGHAI = Pair(31.2304, 121.4737)
    private val MADRID = Pair(40.4168, 3.7038)
    private val LAHORE = Pair(31.5204, 74.3587)
    private val CHICAGO = Pair(41.8781, 87.6298)

    // LatLong Array, the order should be the same as that of R.array.cities
    private val latLongArray = arrayOf(
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

    fun getRandomCity(cities: Array<String>): City {
        if (cities.size != latLongArray.size) {
            throw IllegalStateException("LatLong array must have the same number of element as in cities array.")
        }
        val randomIndex = Random.nextInt(cities.size)
        val latLong = latLongArray[randomIndex]
        return City(cities[randomIndex], latLong.first, latLong.second)
    }
}
