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
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import kotlinx.coroutines.flow.Flow

class TrackersFragment :
    NavToolbarFragment(R.layout.fragment_trackers),
    MVIView<TrackersFeature.State, TrackersFeature.Action> {

    private val viewModel: TrackersViewModel by viewModels()
    private lateinit var trackersAdapter: TrackersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.trackersFeature.takeView(this, this@TrackersFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(TrackersFeature.Action.ObserveTrackers)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackersAdapter = TrackersAdapter {
            requireActivity().supportFragmentManager.commit {
                val bundle = bundleOf("TRACKER" to it.name)
                add<TrackerAppsFragment>(R.id.container, args = bundle)
                setReorderingAllowed(true)
                addToBackStack("trackers")
            }
            // viewModel.submitAction(TrackersFeature.Action.SetSelectedTracker(it))
        }
        view.findViewById<RecyclerView>(R.id.recylcer_view_trackers)?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = trackersAdapter
        }
    }

    override fun getTitle() = getString(R.string.trackers)

    override fun render(state: TrackersFeature.State) {
        trackersAdapter.setData(state.trackers)
    }

    override fun actions(): Flow<TrackersFeature.Action> = viewModel.actions
}
