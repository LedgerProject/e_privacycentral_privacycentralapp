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
import kotlin.random.Random

// ======================================================//
//
// ================    ====            ====   ===============
// ================    ======        ======   ================
//       ====          ========    ========   ====         ====
//       ====          =========  =========   ====         ====
//       ====          ====================   ================
//       ====          ====  ========  ====   ===============
//       ====          ====    ====    ====   ====
// ================    ====     ==     ====   ====
// ================    ====            ====   ====
//
// ======================================================//

/**
 * This whole file acts as a dummy source. All data classes and method implementations
 * are not proper ones and are subject to change anytime.
 */

/**
 * Dummmy permission data class.
 */
data class Permission(
    val name: String,
    val iconId: Int,
    val packagesRequested: List<String> = emptyList(),
    val packagesAllowed: List<String> = emptyList()
)

enum class LocationMode {
    REAL_LOCATION, RANDOM_LOCATION, CUSTOM_LOCATION
}

enum class InternetPrivacyMode {
    REAL_IP, HIDE_IP
}

data class Location(val mode: LocationMode, val latitude: Double, val longitude: Double)

object DummyDataSource {
    private val _appsUsingLocationPerm = MutableStateFlow<List<String>>(emptyList())
    val appsUsingLocationPerm = _appsUsingLocationPerm.asStateFlow()

    const val trackersCount = 77
    private val _activeTrackersCount = MutableStateFlow(10)
    val activeTrackersCount = _activeTrackersCount.asStateFlow()

    private val _location = MutableStateFlow(Location(LocationMode.REAL_LOCATION, 0.0, 0.0))
    val location = _location.asStateFlow()

    private val _internetActivityMode = MutableStateFlow(InternetPrivacyMode.REAL_IP)
    val internetActivityMode = _internetActivityMode.asStateFlow()

    val permissions = arrayOf("Body Sensor", "Calendar", "Call Logs", "Location")
    val icons = arrayOf(
        R.drawable.ic_body_monitor,
        R.drawable.ic_calendar,
        R.drawable.ic_call,
        R.drawable.ic_location
    )

    val packages = arrayOf(
        "facebook",
        "uber",
        "instagram",
        "openstreetmap",
        "truecaller",
        "netflix",
        "firefox",
        "pubg",
        "amazon",
        "calendar",
        "zohomail",
        "privacycentral"
    )

    val populatedPermission: List<Permission> by lazy {
        fetchPermissions()
    }

    private fun fetchPermissions(): List<Permission> {
        val result = mutableListOf<Permission>()
        permissions.forEachIndexed { index, permission ->
            when (index) {
                0 -> result.add(Permission(permission, icons[index]))
                1 -> {
                    val randomPackages = getRandomItems(packages, 8)
                    val grantedPackages = getRandomItems(randomPackages, 3)
                    result.add(
                        Permission(
                            permission,
                            icons[index],
                            randomPackages,
                            grantedPackages
                        )
                    )
                }
                2 -> {
                    val randomPackages = getRandomItems(packages, 10)
                    val grantedPackages = getRandomItems(randomPackages, 9)
                    result.add(
                        Permission(
                            permission,
                            icons[index],
                            randomPackages,
                            grantedPackages
                        )
                    )
                }
                3 -> {
                    val randomPackages = getRandomItems(packages, 5)
                    val grantedPackages = getRandomItems(randomPackages, 3)
                    result.add(
                        Permission(
                            permission,
                            icons[index],
                            randomPackages,
                            grantedPackages
                        )
                    )
                }
            }
        }
        return result
    }

    private fun <T> getRandomItems(data: Array<T>, limit: Int): List<T> =
        getRandomItems(data.asList(), limit)

    private fun <T> getRandomItems(data: List<T>, limit: Int): List<T> {
        val randomItems = mutableSetOf<T>()
        val localData = data.toMutableList()
        repeat(limit) {
            val generated = localData.random()
            randomItems.add(generated)
            localData.remove(generated)
        }
        return randomItems.toList()
    }

    fun getPermission(permissionId: Int): Permission {
        return populatedPermission.get(permissionId)
    }

    fun setLocationMode(locationMode: LocationMode, location: Location? = null): Boolean {
        when (locationMode) {
            LocationMode.REAL_LOCATION ->
                _location.value =
                    Location(LocationMode.REAL_LOCATION, 24.39, 71.80)
            LocationMode.RANDOM_LOCATION -> _location.value = randomLocation()
            LocationMode.CUSTOM_LOCATION -> {
                requireNotNull(location) { "Custom location should be null" }
                _location.value = location.copy(mode = LocationMode.CUSTOM_LOCATION)
            }
        }
        return true
    }

    private fun randomLocation(): Location = Location(
        LocationMode.RANDOM_LOCATION,
        Random.nextDouble(-90.0, 90.0),
        Random.nextDouble(-180.0, 180.0)
    )

    fun setInternetPrivacyMode(mode: InternetPrivacyMode): Boolean {
        _internetActivityMode.value = mode
        return true
    }
}
