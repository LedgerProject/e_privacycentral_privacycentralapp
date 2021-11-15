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

package foundation.e.privacycentralapp.features.trackers.apptrackers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.DependencyContainer
import foundation.e.privacycentralapp.PrivacyCentralApplication
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import foundation.e.privacycentralapp.databinding.ApptrackersFragmentBinding
import foundation.e.privacycentralapp.extensions.viewModelProviderFactoryOf
import foundation.e.privacycentralapp.features.trackers.apptrackers.AppTrackersFeature.Action
import foundation.e.privacycentralapp.features.trackers.apptrackers.AppTrackersFeature.SingleEvent
import foundation.e.privacycentralapp.features.trackers.apptrackers.AppTrackersFeature.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class AppTrackersFragment :
    NavToolbarFragment(R.layout.apptrackers_fragment),
    MVIView<State, Action> {
    companion object {
        private val PARAM_LABEL = "PARAM_LABEL"
        private val PARAM_PACKAGE_NAME = "PARAM_PACKAGE_NAME"
        fun buildArgs(label: String, packageName: String): Bundle = bundleOf(
            PARAM_LABEL to label,
            PARAM_PACKAGE_NAME to packageName
        )
    }

    private val dependencyContainer: DependencyContainer by lazy {
        (this.requireActivity().application as PrivacyCentralApplication).dependencyContainer
    }

    private val viewModel: AppTrackersViewModel by viewModels {
        viewModelProviderFactoryOf {
            dependencyContainer.appTrackersViewModelFactory.create()
        }
    }

    private lateinit var binding: ApptrackersFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.feature.takeView(this, this@AppTrackersFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.feature.singleEvents.collect { event ->
                when (event) {
                    is SingleEvent.ErrorEvent -> displayToast(event.error)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            requireArguments().getString(PARAM_PACKAGE_NAME)?.let {
                viewModel.submitAction(Action.InitAction(it))
            }
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun getTitle(): String = requireArguments().getString(PARAM_LABEL) ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ApptrackersFragmentBinding.bind(view)

        // TODO: crash sqlite ?
        binding.blockAllToggle.setOnClickListener {
            viewModel.submitAction(Action.BlockAllToggleAction(binding.blockAllToggle.isChecked))
        }

        binding.trackers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = ToggleTrackersAdapter(R.layout.apptrackers_item_tracker_toggle) { tracker, isBlocked ->
                viewModel.submitAction(
                    Action.ToggleTrackerAction(
                        tracker,
                        isBlocked
                    )
                )
            }
        }
    }

    override fun render(state: State) {
        binding.blockAllToggle.isChecked = state.isBlockingActivated

        val trackersStatus = state.getTrackersStatus()
        if (!trackersStatus.isNullOrEmpty()) {
            binding.trackers.isVisible = true
            binding.trackers.post {
                (binding.trackers.adapter as ToggleTrackersAdapter?)?.updateDataSet(trackersStatus, state.isBlockingActivated)
            }
            binding.noTrackersYet.isVisible = false
        } else {
            binding.trackers.isVisible = false
            binding.noTrackersYet.isVisible = true
            binding.noTrackersYet.text = getString(
                if (state.isBlockingActivated)
                    R.string.apptrackers_no_trackers_yet_block_on
                else R.string.apptrackers_no_trackers_yet_block_off
            )
        }
    }

    override fun actions(): Flow<Action> = viewModel.actions
}
