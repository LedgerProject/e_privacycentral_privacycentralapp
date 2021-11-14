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

import android.app.AppOpsManager
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import foundation.e.privacycentralapp.data.repositories.LocalStateRepository
import foundation.e.privacycentralapp.domain.entities.LocationMode
import foundation.e.privacycentralapp.dummy.CityDataSource
import foundation.e.privacymodules.location.IFakeLocationModule
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.AppOpModes
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.random.Random

class FakeLocationStateUseCase(
    private val fakeLocationModule: IFakeLocationModule,
    private val permissionsModule: PermissionsPrivacyModule,
    private val localStateRepository: LocalStateRepository,
    private val citiesRepository: CityDataSource,
    private val appDesc: ApplicationDescription,
    private val appContext: Context,
    private val coroutineScope: CoroutineScope
) {
    private val _locationMode = MutableStateFlow(LocationMode.REAL_LOCATION)
    val locationMode: StateFlow<LocationMode> = _locationMode

    init {
        coroutineScope.launch {
            localStateRepository.quickPrivacyEnabledFlow.collect {
                applySettings(it, localStateRepository.fakeLocation)
            }
        }
    }

    private val locationManager: LocationManager
        get() = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun getLocationMode(): Triple<LocationMode, Float?, Float?> {
        val fakeLocation = localStateRepository.fakeLocation
        return if (fakeLocation != null && _locationMode.value == LocationMode.SPECIFIC_LOCATION) {
            Triple(
                LocationMode.SPECIFIC_LOCATION,
                fakeLocation.first,
                fakeLocation.second
            )
        } else {
            Triple(_locationMode.value, null, null)
        }
    }

    private fun acquireLocationPermission() {
        permissionsModule.toggleDangerousPermission(
            appDesc,
            android.Manifest.permission.ACCESS_FINE_LOCATION, true
        )

        // permissionsModule.setAppOpMode(
        //     appDesc, AppOpsManager.OPSTR_COARSE_LOCATION,
        //     AppOpModes.ALLOWED
        // )
        // permissionsModule.setAppOpMode(
        //     appDesc, AppOpsManager.OPSTR_FINE_LOCATION,
        //     AppOpModes.ALLOWED
        // )
    }

    private fun applySettings(isQuickPrivacyEnabled: Boolean, fakeLocation: Pair<Float, Float>?) {
        if (isQuickPrivacyEnabled && fakeLocation != null) {
            if (permissionsModule.getAppOpMode(appDesc, AppOpsManager.OPSTR_MOCK_LOCATION) != AppOpModes.ALLOWED) {
                permissionsModule.setAppOpMode(appDesc, AppOpsManager.OPSTR_MOCK_LOCATION, AppOpModes.ALLOWED)
            }
            fakeLocationModule.startFakeLocation()
            fakeLocationModule.setFakeLocation(fakeLocation.first.toDouble(), fakeLocation.second.toDouble())
            _locationMode.value = if (fakeLocation in citiesRepository.citiesLocationsList) LocationMode.RANDOM_LOCATION
            else LocationMode.SPECIFIC_LOCATION
        } else {
            fakeLocationModule.stopFakeLocation()
            _locationMode.value = LocationMode.REAL_LOCATION
        }
    }

    fun setSpecificLocation(latitude: Float, longitude: Float) {
        setFakeLocation(latitude to longitude)
    }

    fun setRandomLocation() {
        val randomIndex = Random.nextInt(citiesRepository.citiesLocationsList.size)
        val location = citiesRepository.citiesLocationsList[randomIndex]

        setFakeLocation(location)
    }

    private fun setFakeLocation(location: Pair<Float, Float>) {
        localStateRepository.fakeLocation = location
        applySettings(true, location)
    }

    fun stopFakeLocation() {
        localStateRepository.fakeLocation = null
        applySettings(true, null)
    }

    val currentLocation = MutableStateFlow<Location?>(null)

    private var localListener = object : LocationListener {
        val providerName = LocationManager.NETWORK_PROVIDER

        override fun onLocationChanged(location: Location) {
            Log.e("DebugLoc", "onLocationChanged $location")
            currentLocation.value = location
        }

        // Deprecated since API 29, never called.
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {
            Log.e("DebugLoc", "ProvuderEnabled: $provider")
            reset(provider)
        }

        override fun onProviderDisabled(provider: String?) {
            Log.e("DebugLoc", "ProvuderDisabled: $provider")
            reset(provider)
        }

        private fun reset(provider: String?) {
            if (provider == providerName) {
                stopListeningLocation()
                currentLocation.value = null
                startListeningLocation()
            }
        }
    }

    fun startListeningLocation() {
        requestLocationUpdates(localListener)
    }

    fun stopListeningLocation() {
        removeUpdates(localListener)
    }

    fun requestLocationUpdates(listener: LocationListener) {
        acquireLocationPermission()
        try {
            Log.e("DebugLoc", "requestLocationUpdates")
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, // TODO: tight this with fakelocation module.
                0L,
                0f,
                listener
            )
            // locationManager.requestLocationUpdates(
            //     LocationManager.NETWORK_PROVIDER, // TODO: tight this with fakelocation module.
            //     0L,
            //     0f,
            //     listener
            // )

            val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            location?.let { listener.onLocationChanged(it) }
        } catch (se: SecurityException) {
            Log.e("DebugLoc", "Missing permission", se)
        }
    }

    fun removeUpdates(listener: LocationListener) {
        locationManager.removeUpdates(listener)
    }
}
