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

package foundation.e.privacycentralapp.features.trackers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.DependencyContainer
import foundation.e.privacycentralapp.PrivacyCentralApplication
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.AppsAdapter
import foundation.e.privacycentralapp.common.NavToolbarFragment
import foundation.e.privacycentralapp.databinding.FragmentTrackersBinding
import foundation.e.privacycentralapp.databinding.TrackersItemGraphBinding
import foundation.e.privacycentralapp.extensions.viewModelProviderFactoryOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class TrackersFragment :
    NavToolbarFragment(R.layout.fragment_trackers),
    MVIView<TrackersFeature.State, TrackersFeature.Action> {

    private val dependencyContainer: DependencyContainer by lazy {
        (this.requireActivity().application as PrivacyCentralApplication).dependencyContainer
    }

    private val viewModel: TrackersViewModel by viewModels {
        viewModelProviderFactoryOf { dependencyContainer.trackersViewModelFactory.create() }
    }

    private lateinit var binding: FragmentTrackersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.trackersFeature.takeView(this, this@TrackersFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.trackersFeature.singleEvents.collect { event ->
                when (event) {
                    is TrackersFeature.SingleEvent.ErrorEvent -> {
                        displayToast(event.error)
                    }
                    is TrackersFeature.SingleEvent.OpenAppDetailsEvent -> {
                        displayToast(event.packageName)
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(TrackersFeature.Action.InitAction)
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTrackersBinding.bind(view)

        listOf(binding.graphDay, binding.graphMonth, binding.graphYear).forEach {
            it.graph.apply {
                description = null
                setTouchEnabled(false)
                setDrawGridBackground(false)
                setDrawBorders(false)
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                xAxis.isEnabled = false
                legend.isEnabled = false
            }
        }

        binding.apps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = AppsAdapter(R.layout.trackers_item_app) { packageName ->
                viewModel.submitAction(
                    TrackersFeature.Action.ClickAppAction(packageName)
                )
            }
        }

        //
        // requireActivity().supportFragmentManager.commit {
        //     val bundle = bundleOf("TRACKER" to it.name)
        //     add<TrackerAppsFragment>(R.id.container, args = bundle)
        //     setReorderingAllowed(true)
        //     addToBackStack("trackers")
        // }
    }

    override fun getTitle() = getString(R.string.trackers_title)

    override fun render(state: TrackersFeature.State) {
        if (state.dayStatistics != null && state.dayTrackersCount != null) {
            renderGraph(state.dayTrackersCount, state.dayStatistics, binding.graphDay)
        }

        if (state.monthStatistics != null && state.monthTrackersCount != null) {
            renderGraph(state.monthTrackersCount, state.monthStatistics, binding.graphMonth)
        }

        if (state.yearStatistics != null && state.yearTrackersCount != null) {
            renderGraph(state.yearTrackersCount, state.yearStatistics, binding.graphYear)
        }

        state.apps?.let {
            binding.apps.post {
                (binding.apps.adapter as AppsAdapter?)?.dataSet = it
            }
        }
    }

    private fun renderGraph(trackersCount: Int, data: List<Int>, graphBinding: TrackersItemGraphBinding) {
        val trackersDataSet = BarDataSet(
            data.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) },
            getString(R.string.trackers_count_label)
        ).apply {
            color = ContextCompat.getColor(requireContext(), R.color.purple_chart)
            setDrawValues(false)
        }

        graphBinding.graph.data = BarData(trackersDataSet)
        graphBinding.graph.invalidate()
        graphBinding.trackersCountLabel.text = getString(R.string.trackers_count_label, trackersCount)
    }

    override fun actions(): Flow<TrackersFeature.Action> = viewModel.actions
}
