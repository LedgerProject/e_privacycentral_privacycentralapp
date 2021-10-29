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

import android.Manifest
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.ApplicationDescription

class AppListUseCase(
    private val permissionsModule: PermissionsPrivacyModule
) {

    fun getAppsUsingInternet(): List<ApplicationDescription> {
        return permissionsModule.getInstalledApplications()
            .filter {
                permissionsModule.getPermissions(it.packageName)
                    .contains(Manifest.permission.INTERNET)
            }.map {
                it.icon = permissionsModule.getApplicationIcon(it.packageName)
                it
            }.sortedWith(object : Comparator<ApplicationDescription> {
                override fun compare(
                    p0: ApplicationDescription?,
                    p1: ApplicationDescription?
                ): Int {
                    return if (p0?.icon != null && p1?.icon != null) {
                        p0.label.toString().compareTo(p1.label.toString())
                    } else if (p0?.icon == null) {
                        1
                    } else {
                        -1
                    }
                }
            })
    }
}
