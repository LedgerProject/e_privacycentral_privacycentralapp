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

package foundation.e.privacycentralapp.features.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.DependencyContainer
import foundation.e.privacycentralapp.PrivacyCentralApplication
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import foundation.e.privacycentralapp.common.customizeBarChart
import foundation.e.privacycentralapp.common.updateGraphData
import foundation.e.privacycentralapp.databinding.FragmentDashboardBinding
import foundation.e.privacycentralapp.domain.entities.InternetPrivacyMode
import foundation.e.privacycentralapp.domain.entities.LocationMode
import foundation.e.privacycentralapp.extensions.viewModelProviderFactoryOf
import foundation.e.privacycentralapp.features.dashboard.DashboardFeature.State
import foundation.e.privacycentralapp.features.internetprivacy.InternetPrivacyFragment
import foundation.e.privacycentralapp.features.location.FakeLocationFragment
import foundation.e.privacycentralapp.features.trackers.TrackersFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class DashboardFragment :
    NavToolbarFragment(R.layout.fragment_dashboard),
    MVIView<DashboardFeature.State, DashboardFeature.Action> {

    private val dependencyContainer: DependencyContainer by lazy {
        (this.requireActivity().application as PrivacyCentralApplication).dependencyContainer
    }

    private val viewModel: DashboardViewModel by activityViewModels {
        viewModelProviderFactoryOf { dependencyContainer.dashBoardViewModelFactory.create() }
    }

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.dashboardFeature.takeView(this, this@DashboardFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.dashboardFeature.singleEvents.collect { event ->
                when (event) {
                    is DashboardFeature.SingleEvent.NavigateToLocationSingleEvent -> {
                        requireActivity().supportFragmentManager.commit {
                            add<FakeLocationFragment>(R.id.container)
                            setReorderingAllowed(true)
                            addToBackStack("dashboard")
                        }
                    }
                    is DashboardFeature.SingleEvent.NavigateToInternetActivityPrivacySingleEvent -> {
                        requireActivity().supportFragmentManager.commit {
                            add<InternetPrivacyFragment>(R.id.container)
                            setReorderingAllowed(true)
                            addToBackStack("dashboard")
                        }
                    }
                    is DashboardFeature.SingleEvent.NavigateToPermissionsSingleEvent -> {
                        val intent = Intent("android.intent.action.MANAGE_PERMISSIONS")
                        requireActivity().startActivity(intent)
                    }
                    DashboardFeature.SingleEvent.NavigateToTrackersSingleEvent -> {
                        requireActivity().supportFragmentManager.commit {
                            add<TrackersFragment>(R.id.container)
                            setReorderingAllowed(true)
                            addToBackStack("dashboard")
                        }
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(DashboardFeature.Action.InitAction)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)

        customizeBarChart(binding.graph)

        binding.togglePrivacyCentral.setOnClickListener {
            viewModel.submitAction(DashboardFeature.Action.TogglePrivacyAction)
        }
        binding.myLocation.container.setOnClickListener {
            viewModel.submitAction(DashboardFeature.Action.ShowFakeMyLocationAction)
        }
        binding.internetActivityPrivacy.container.setOnClickListener {
            viewModel.submitAction(DashboardFeature.Action.ShowInternetActivityPrivacyAction)
        }
        binding.appsPermissions.container.setOnClickListener {
            viewModel.submitAction(DashboardFeature.Action.ShowAppsPermissions)
        }

        binding.amITracked.container.setOnClickListener {
            viewModel.submitAction(DashboardFeature.Action.ShowTrackers)
        }
    }

    override fun getTitle(): String {
        return getString(R.string.dashboard_title)
    }

    override fun render(state: State) {

        binding.stateLabel.text = getString(
            if (state.isQuickPrivacyEnabled) R.string.dashboard_state_label_on
            else R.string.dashboard_state_label_off
        )

        binding.togglePrivacyCentral.setImageResource(
            if (state.isQuickPrivacyEnabled) R.drawable.ic_quick_privacy_on
            else R.drawable.ic_quick_privacy_off
        )
        binding.stateLabel.setTextColor(
            getColor(
                requireContext(),
                if (state.isQuickPrivacyEnabled) R.color.green_on
                else R.color.orange_off
            )
        )

        val trackersEnabled = state.isQuickPrivacyEnabled && state.isAllTrackersBlocked
        binding.stateTrackers.text = getString(
            if (trackersEnabled) R.string.dashboard_state_trackers_on
            else R.string.dashboard_state_trackers_off
        )
        binding.stateTrackers.setTextColor(
            getColor(
                requireContext(),
                if (trackersEnabled) R.color.green_on
                else R.color.black_text
            )
        )

        val geolocEnabled = state.isQuickPrivacyEnabled && state.locationMode != LocationMode.REAL_LOCATION
        binding.stateGeolocation.text = getString(
            if (geolocEnabled) R.string.dashboard_state_geolocation_on
            else R.string.dashboard_state_geolocation_off
        )
        binding.stateGeolocation.setTextColor(
            getColor(
                requireContext(),
                if (geolocEnabled) R.color.green_on
                else R.color.black_text
            )
        )

        val ipAddressEnabled = state.isQuickPrivacyEnabled && state.internetPrivacyMode != InternetPrivacyMode.REAL_IP
        val isLoading = state.isQuickPrivacyEnabled && state.internetPrivacyMode in listOf(
            InternetPrivacyMode.HIDE_IP_LOADING,
            InternetPrivacyMode.REAL_IP_LOADING
        )
        binding.stateIpAddress.text = getString(
            if (ipAddressEnabled) R.string.dashboard_state_ipaddress_on
            else R.string.dashboard_state_ipaddress_off
        )

        binding.stateIpAddressLoader.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.stateIpAddress.visibility = if (!isLoading) View.VISIBLE else View.GONE

        binding.stateIpAddress.setTextColor(
            getColor(
                requireContext(),
                if (ipAddressEnabled) R.color.green_on
                else R.color.black_text
            )
        )

        state.dayStatistics?.let {
            updateGraphData(it, binding.graph, getColor(requireContext(), R.color.e_blue2))
        }

        binding.graphLegend.text = getString(R.string.dashboard_graph_trackers_legend, state.dayTrackersCount?.toString() ?: "No")

        if (state.dayTrackersCount != null && state.trackersCount != null) {
            binding.amITracked.subTitle = getString(R.string.dashboard_am_i_tracked_subtitle, state.trackersCount, state.dayTrackersCount)
        } else {
            binding.amITracked.subTitle = getString(R.string.trackers_title)
        }

        binding.myLocation.subTitle = getString(
            if (state.isQuickPrivacyEnabled &&
                state.locationMode != LocationMode.REAL_LOCATION
            )
                R.string.dashboard_location_subtitle_on
            else R.string.dashboard_location_subtitle_off
        )

        binding.internetActivityPrivacy.subTitle = getString(
            if (state.isQuickPrivacyEnabled &&
                state.internetPrivacyMode != InternetPrivacyMode.REAL_IP
            )
                R.string.dashboard_internet_activity_privacy_subtitle_on
            else R.string.dashboard_internet_activity_privacy_subtitle_off
        )

        binding.executePendingBindings()
    }

    override fun actions(): Flow<DashboardFeature.Action> = viewModel.actions
}
