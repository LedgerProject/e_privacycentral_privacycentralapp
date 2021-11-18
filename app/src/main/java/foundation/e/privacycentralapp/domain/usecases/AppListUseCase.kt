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
import foundation.e.trackerfilter.api.BlockTrackersPrivacyModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AppListUseCase(
    private val permissionsModule: PermissionsPrivacyModule,
    private val blockTrackersPrivacyModule: BlockTrackersPrivacyModule,
    private val corouteineScope: CoroutineScope
) {

    private val _appsUsingInternet = MutableStateFlow<List<ApplicationDescription>>(emptyList())
    private val _installedAppsUsingInternet = MutableStateFlow<List<ApplicationDescription>>(emptyList())
    init {
        corouteineScope.launch {
            _appsUsingInternet.value = getAppsUsingInternetList()
        }
    }

    fun getInstalledAppsUsingInternet(): Flow<List<ApplicationDescription>> {
        corouteineScope.launch {
            _installedAppsUsingInternet.value = getInstalledAppsUsingInternetList()
        }
        return _installedAppsUsingInternet
    }

    fun getBlockableApps(): Flow<List<ApplicationDescription>> {
        corouteineScope.launch {
            _installedAppsUsingInternet.value = getBlockableAppsList()
        }
        return _installedAppsUsingInternet
    }

    private fun getBlockableAppsList(): List<ApplicationDescription> {
        return blockTrackersPrivacyModule.getBlockableApps()
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

    private fun getInstalledAppsUsingInternetList(): List<ApplicationDescription> {
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

    fun getAppsUsingInternet(): Flow<List<ApplicationDescription>> {
        corouteineScope.launch {
            _appsUsingInternet.value = getAppsUsingInternetList()
        }
        return _appsUsingInternet
    }

    private fun getAppsUsingInternetList(): List<ApplicationDescription> {
        return permissionsModule.getAllApplications()
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
