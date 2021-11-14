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

package foundation.e.privacycentralapp

import android.app.Application
import android.content.Context
import android.os.Process
import foundation.e.privacycentralapp.data.repositories.LocalStateRepository
import foundation.e.privacycentralapp.domain.usecases.AppListUseCase
import foundation.e.privacycentralapp.domain.usecases.FakeLocationStateUseCase
import foundation.e.privacycentralapp.domain.usecases.GetQuickPrivacyStateUseCase
import foundation.e.privacycentralapp.domain.usecases.IpScramblingStateUseCase
import foundation.e.privacycentralapp.domain.usecases.TrackersStateUseCase
import foundation.e.privacycentralapp.domain.usecases.TrackersStatisticsUseCase
import foundation.e.privacycentralapp.dummy.CityDataSource
import foundation.e.privacycentralapp.features.dashboard.DashBoardViewModelFactory
import foundation.e.privacycentralapp.features.internetprivacy.InternetPrivacyViewModelFactory
import foundation.e.privacycentralapp.features.location.FakeLocationViewModelFactory
import foundation.e.privacycentralapp.features.trackers.TrackersViewModelFactory
import foundation.e.privacycentralapp.features.trackers.apptrackers.AppTrackersViewModelFactory
import foundation.e.privacymodules.ipscrambler.IpScramblerModule
import foundation.e.privacymodules.ipscramblermodule.IIpScramblerModule
import foundation.e.privacymodules.location.FakeLocationModule
import foundation.e.privacymodules.location.IFakeLocationModule
import foundation.e.privacymodules.permissions.PermissionsPrivacyModule
import foundation.e.privacymodules.permissions.data.ApplicationDescription
import foundation.e.trackerfilter.api.BlockTrackersPrivacyModule
import foundation.e.trackerfilter.api.TrackTrackersPrivacyModule
import kotlinx.coroutines.GlobalScope
import lineageos.blockers.BlockerInterface

/**
 * Simple container to hold application wide dependencies.
 *
 * TODO: Test if this implementation is leaky.
 */
class DependencyContainer constructor(val app: Application) {

    val context: Context by lazy { app.applicationContext }

    // Drivers
    private val fakeLocationModule: IFakeLocationModule by lazy { FakeLocationModule(app.applicationContext) }
    private val permissionsModule by lazy { PermissionsPrivacyModule(app.applicationContext) }
    private val ipScramblerModule: IIpScramblerModule by lazy { IpScramblerModule(app.applicationContext) }

    private val appDesc by lazy {
        ApplicationDescription(
            packageName = context.packageName,
            uid = Process.myUid(),
            label = context.resources.getString(R.string.app_name),
            icon = null
        )
    }

    private val blockTrackersPrivacyModule by lazy { BlockTrackersPrivacyModule.getInstance(context) }

    // Repositories
    private val localStateRepository by lazy { LocalStateRepository(context) }
    private val trackTrackersPrivacyModule by lazy { TrackTrackersPrivacyModule.getInstance(context) }

    // Usecases
    private val getQuickPrivacyStateUseCase by lazy {
        GetQuickPrivacyStateUseCase(localStateRepository)
    }
    private val ipScramblingStateUseCase by lazy {
        IpScramblingStateUseCase(ipScramblerModule, permissionsModule, appDesc, localStateRepository, GlobalScope)
    }
    private val appListUseCase = AppListUseCase(permissionsModule, GlobalScope)

    private val trackersStatisticsUseCase by lazy {
        TrackersStatisticsUseCase(trackTrackersPrivacyModule)
    }

    private val trackersStateUseCase by lazy {
        TrackersStateUseCase(blockTrackersPrivacyModule, trackTrackersPrivacyModule, permissionsModule, localStateRepository, GlobalScope)
    }

    private val fakeLocationStateUseCase by lazy {
        FakeLocationStateUseCase(
            fakeLocationModule, permissionsModule, localStateRepository, CityDataSource, appDesc, context, GlobalScope
        )
    }

    // ViewModelFactories
    val dashBoardViewModelFactory by lazy {
        DashBoardViewModelFactory(getQuickPrivacyStateUseCase, ipScramblingStateUseCase, trackersStatisticsUseCase, trackersStateUseCase, fakeLocationStateUseCase)
    }

    val fakeLocationViewModelFactory by lazy {
        FakeLocationViewModelFactory(
            getQuickPrivacyStateUseCase = getQuickPrivacyStateUseCase,
            fakeLocationStateUseCase = fakeLocationStateUseCase
        )
    }

    val blockerService = BlockerInterface.getInstance(context)

    val internetPrivacyViewModelFactory by lazy {
        InternetPrivacyViewModelFactory(ipScramblerModule, getQuickPrivacyStateUseCase, ipScramblingStateUseCase, appListUseCase)
    }

    val trackersViewModelFactory by lazy {
        TrackersViewModelFactory(getQuickPrivacyStateUseCase, trackersStatisticsUseCase, appListUseCase)
    }

    val appTrackersViewModelFactory by lazy {
        AppTrackersViewModelFactory(trackersStateUseCase)
    }
}
