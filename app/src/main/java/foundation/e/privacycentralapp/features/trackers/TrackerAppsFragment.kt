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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class TrackerAppsFragment :
    NavToolbarFragment(R.layout.fragment_tracker_apps),
    MVIView<TrackersFeature.State, TrackersFeature.Action> {

    private val viewModel: TrackersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.trackersFeature.takeView(this, this@TrackerAppsFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.trackersFeature.singleEvents.collect { event ->
                when (event) {
                    is TrackersFeature.SingleEvent.ErrorEvent -> displayToast(event.error)
                }
            }
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun getTitle(): String = getString(R.string.tracker)

    override fun render(state: TrackersFeature.State) {
        state.currentSelectedTracker?.let { tracker ->
            view?.findViewById<RecyclerView>(R.id.recylcer_view_tracker_apps)?.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = TrackerAppsAdapter(tracker) { tracker, grant ->
                    viewModel.submitAction(
                       TrackersFeature.Action.ToggleTrackerAction(
                            tracker,
                            grant
                        )
                    )
                }
            }
            getToolbar()?.title = tracker.name
        }
    }

    override fun actions(): Flow<TrackersFeature.Action> = viewModel.actions
}
