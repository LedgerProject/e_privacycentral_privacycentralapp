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

/*import android.app.AppOpsManager
import android.content.Intent
import android.util.Log
import foundation.e.privacycentralapp.data.repositories.LocalStateRepository
import foundation.e.privacycentralapp.domain.entities.LocationMode
import foundation.e.privacycentralapp.features.location.LocationApiDelegate
import foundation.e.privacymodules.location.IFakeLocation
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.AppOpModes
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import kotlinx.coroutines.CoroutineScope

class FakeLocationStateUseCase(
    private val fakeLocationModule: IFakeLocation,
    private val permissionsModule: PermissionsPrivacyModule,
    private val localStateRepository: LocalStateRepository,
    private val appDesc: ApplicationDescription,
    private val coroutineScope: CoroutineScope
) {

    private fun acquireLocationPermission() {
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
    //        Log.e(TAG, "Can't start RealLocation", e)
        }
    }

    private fun applySettings(isQuickPrivacyEnabled: Boolean, fakeLocationMode: LocationMode) {
        when {
          //  isQuickPrivacyEnabled ->
        }
    }


}*/
