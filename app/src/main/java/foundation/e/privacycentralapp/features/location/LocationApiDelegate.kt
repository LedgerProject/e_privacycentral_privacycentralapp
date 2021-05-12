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

package foundation.e.privacycentralapp.features.location

import android.app.AppOpsManager
import android.util.Log
import foundation.e.privacymodules.location.IFakeLocation
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.AppOpModes
import foundation.e.privacymodules.permissions.data.ApplicationDescription

class LocationApiDelegate(
    private val fakeLocationModule: IFakeLocation,
    private val permissionsModule: PermissionsPrivacyModule,
    private val appDesc: ApplicationDescription
) {

    private val TAG = LocationApiDelegate::class.simpleName

    fun setFakeLocation(latitude: Double, longitude: Double) {
        if (permissionsModule.getAppOpMode(appDesc, AppOpsManager.OPSTR_MOCK_LOCATION) != AppOpModes.ALLOWED) {
            permissionsModule.setAppOpMode(
                appDesc, AppOpsManager.OPSTR_MOCK_LOCATION,
                AppOpModes.ALLOWED
            )
        }
        try {
            fakeLocationModule.startFakeLocation()
        } catch (e: Exception) {
            Log.e(TAG, "Can't startFakeLocation", e)
        }
        fakeLocationModule.setFakeLocation(latitude, longitude)
    }

    fun stopFakeLocation() {
        try {
            permissionsModule.setAppOpMode(
                appDesc, AppOpsManager.OPSTR_MOCK_LOCATION,
                AppOpModes.IGNORED
            )
            permissionsModule.setAppOpMode(
                appDesc, AppOpsManager.OPSTR_MOCK_LOCATION,
                AppOpModes.IGNORED
            )
            fakeLocationModule.stopFakeLocation()
        } catch (e: Exception) {
            Log.e(TAG, "Can't stop FakeLocation", e)
        }
    }

    fun startRealLocation() {
        stopFakeLocation()
        try {
            permissionsModule.setAppOpMode(
                appDesc, AppOpsManager.OPSTR_COARSE_LOCATION,
                AppOpModes.ALLOWED
            )
            permissionsModule.setAppOpMode(
                appDesc, AppOpsManager.OPSTR_FINE_LOCATION,
                AppOpModes.ALLOWED
            )
        } catch (e: Exception) {
            Log.e(TAG, "Can't start RealLocation", e)
        }
    }
}
