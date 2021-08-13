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

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.ToolbarFragment
import foundation.e.privacycentralapp.dummy.mapToString
import foundation.e.privacycentralapp.features.internetprivacy.InternetPrivacyFragment
import foundation.e.privacycentralapp.features.location.FakeLocationFragment
import foundation.e.privacycentralapp.features.permissions.PermissionsFragment
import foundation.e.privacycentralapp.features.trackers.TrackersFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class DashboardFragment :
    ToolbarFragment(R.layout.fragment_dashboard),
    MVIView<DashboardFeature.State, DashboardFeature.Action> {

    private val viewModel: DashboardViewModel by activityViewModels()

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
                    is DashboardFeature.SingleEvent.NavigateToQuickProtectionSingleEvent -> {
                        requireActivity().supportFragmentManager.commit {
                            add<QuickProtectionFragment>(R.id.container)
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
                        requireActivity().supportFragmentManager.commit {
                            add<PermissionsFragment>(R.id.container)
                            setReorderingAllowed(true)
                            addToBackStack("dashboard")
                        }
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
            viewModel.submitAction(DashboardFeature.Action.ShowDashboardAction)
            viewModel.submitAction(DashboardFeature.Action.ObserveDashboardAction)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addClickToMore(view.findViewById(R.id.personal_leakag_info))
        view.let {
            it.findViewById<TextView>(R.id.tap_to_enable_quick_protection).setOnClickListener {
                viewModel.submitAction(DashboardFeature.Action.ShowQuickPrivacyProtectionInfoAction)
            }
            it.findViewById<RelativeLayout>(R.id.my_location).setOnClickListener {
                viewModel.submitAction(DashboardFeature.Action.ShowFakeMyLocationAction)
            }
            it.findViewById<RelativeLayout>(R.id.internet_activity_privacy).setOnClickListener {
                viewModel.submitAction(DashboardFeature.Action.ShowInternetActivityPrivacyAction)
            }
            it.findViewById<RelativeLayout>(R.id.apps_permissions).setOnClickListener {
                viewModel.submitAction(DashboardFeature.Action.ShowAppsPermissions)
            }
            it.findViewById<RelativeLayout>(R.id.am_i_tracked).setOnClickListener {
                viewModel.submitAction(DashboardFeature.Action.ShowTrackers)
            }
        }
    }

    override fun getTitle(): String = getString(R.string.privacy_dashboard)

    private fun addClickToMore(textView: TextView) {
        val clickToMore = SpannableString(getString(R.string.click_to_learn_more))
        clickToMore.setSpan(
            ForegroundColorSpan(Color.parseColor("#007fff")),
            0,
            clickToMore.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.append(clickToMore)
    }

    override fun render(state: DashboardFeature.State) {
        when (state) {
            is DashboardFeature.State.InitialState, is DashboardFeature.State.LoadingDashboardState -> {
                view?.let {
                    it.findViewById<ProgressBar>(R.id.loadingSpinner).visibility = View.VISIBLE
                    it.findViewById<NestedScrollView>(R.id.scrollContainer).visibility = View.GONE
                }
            }
            is DashboardFeature.State.DashboardState -> {
                view?.let { view ->
                    view.findViewById<ProgressBar>(R.id.loadingSpinner).visibility = View.GONE
                    view.findViewById<NestedScrollView>(R.id.scrollContainer).visibility =
                        View.VISIBLE
                    view.findViewById<TextView>(R.id.am_i_tracked_subtitle).text = getString(
                        R.string.am_i_tracked_subtitle,
                        state.trackersCount,
                        state.activeTrackersCount
                    )
                    view.findViewById<TextView>(R.id.apps_permissions_subtitle).text = getString(
                        R.string.apps_permissions_subtitle,
                        state.totalApps,
                        state.permissionCount
                    )
                    view.findViewById<TextView>(R.id.my_location_subtitle).let { textView ->
                        textView.text = getString(
                            R.string.my_location_subtitle,
                            state.appsUsingLocationPerm,
                        )
                        textView.append(
                            SpannableString(state.locationMode.mapToString(requireContext()))
                                .also {
                                    it.setSpan(
                                        ForegroundColorSpan(Color.parseColor("#007fff")),
                                        0,
                                        it.length,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                        )
                    }
                    view.findViewById<TextView>(R.id.internet_activity_privacy_subtitle)
                        .let { textView ->
                            textView.text = getString(R.string.internet_activity_privacy_subtitle)
                            textView.append(
                                SpannableString(state.internetPrivacyMode.mapToString(requireContext()))
                                    .also {
                                        it.setSpan(
                                            ForegroundColorSpan(Color.parseColor("#007fff")),
                                            0,
                                            it.length,
                                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                        )
                                    }
                            )
                        }
                }
            }
            DashboardFeature.State.QuickProtectionState -> {
            }
        }
    }

    override fun actions(): Flow<DashboardFeature.Action> = viewModel.actions
}
