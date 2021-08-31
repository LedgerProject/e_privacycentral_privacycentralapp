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

package foundation.e.privacycentralapp.features.internetprivacy

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.DependencyContainer
import foundation.e.privacycentralapp.PrivacyCentralApplication
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import foundation.e.privacycentralapp.common.ToggleAppsAdapter
import foundation.e.privacycentralapp.extensions.viewModelProviderFactoryOf
import foundation.e.privacymodules.ipscramblermodule.IIpScramblerModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.Locale

class InternetPrivacyFragment :
    NavToolbarFragment(R.layout.fragment_internet_activity_policy),
    MVIView<InternetPrivacyFeature.State, InternetPrivacyFeature.Action> {

    private val dependencyContainer: DependencyContainer by lazy {
        (this.requireActivity().application as PrivacyCentralApplication).dependencyContainer
    }

    private val viewModel: InternetPrivacyViewModel by viewModels {
        viewModelProviderFactoryOf { dependencyContainer.internetPrivacyViewModelFactory.create() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.internetPrivacyFeature.takeView(this, this@InternetPrivacyFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.internetPrivacyFeature.singleEvents.collect { event ->
                when (event) {
                    is InternetPrivacyFeature.SingleEvent.ErrorEvent -> displayToast(event.error)
                    is InternetPrivacyFeature.SingleEvent.StartAndroidVpnActivityEvent ->
                        launchAndroidVpnDisclaimer.launch(event.intent)
                    InternetPrivacyFeature.SingleEvent.HiddenIPSelectedEvent -> displayToast("Your IP is hidden")
                    InternetPrivacyFeature.SingleEvent.RealIPSelectedEvent -> displayToast("Your IP is visible to internet")
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(InternetPrivacyFeature.Action.LoadInternetModeAction)
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }

    private val launchAndroidVpnDisclaimer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.submitAction(InternetPrivacyFeature.Action.AndroidVpnActivityResultAction(it.resultCode))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listOf(R.id.recycler_view_scrambled, R.id.recycler_view_to_select).forEach { viewId ->
            view.findViewById<RecyclerView>(viewId)?.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = ToggleAppsAdapter { packageName, isIpScrambled ->
                    viewModel.submitAction(
                        InternetPrivacyFeature.Action.ToggleAppIpScrambled(
                            packageName,
                            isIpScrambled
                        )
                    )
                }
            }
        }

        bindClickListeners(view)
    }

    override fun getTitle(): String = getString(R.string.internet_activity_privacy)

    private fun bindClickListeners(fragmentView: View) {
        fragmentView.let {
            it.findViewById<RadioButton>(R.id.radio_use_real_ip)
                .setOnClickListener {
                    viewModel.submitAction(InternetPrivacyFeature.Action.UseRealIPAction)
                }
            it.findViewById<RadioButton>(R.id.radio_use_hidden_ip)
                .setOnClickListener {
                    viewModel.submitAction(InternetPrivacyFeature.Action.UseHiddenIPAction)
                }
        }
    }

    override fun render(state: InternetPrivacyFeature.State) {
        view?.let {
            it.findViewById<RadioButton>(R.id.radio_use_hidden_ip).apply {
                isChecked = state.mode in listOf(
                    IIpScramblerModule.Status.ON,
                    IIpScramblerModule.Status.STARTING
                )
                isEnabled = state.mode != IIpScramblerModule.Status.STARTING
            }
            it.findViewById<RadioButton>(R.id.radio_use_real_ip)?.apply {
                isChecked =
                    state.mode in listOf(
                    IIpScramblerModule.Status.OFF,
                    IIpScramblerModule.Status.STOPPING
                )
                isEnabled = state.mode != IIpScramblerModule.Status.STOPPING
            }
            it.findViewById<TextView>(R.id.ipscrambling_tor_status)?.apply {
                when (state.mode) {
                    IIpScramblerModule.Status.STARTING -> {
                        text = getString(R.string.ipscrambling_is_starting)
                        visibility = View.VISIBLE
                    }
                    IIpScramblerModule.Status.STOPPING -> {
                        text = getString(R.string.ipscrambling_is_stopping)
                        visibility = View.VISIBLE
                    }
                    else -> {
                        text = ""
                        visibility = View.GONE
                    }
                }
            }

            it.findViewById<Spinner>(R.id.ipscrambling_select_location)?.apply {
                adapter = ArrayAdapter(
                    requireContext(), android.R.layout.simple_spinner_item,
                    state.availableLocationIds.map {
                        if (it == "") {
                            getString(R.string.ipscrambling_any_location)
                        } else {
                            Locale("", it).displayCountry
                        }
                    }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                        viewModel.submitAction(InternetPrivacyFeature.Action.SelectLocationAction(position))
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {}
                })

                setSelection(state.selectedLocationPosition)
            }

            it.findViewById<TextView>(R.id.ipscrambling_activated)?.apply {
                text = getString(
                    if (state.isAllAppsScrambled) R.string.ipscrambling_all_apps_scrambled
                    else R.string.ipscrambling_only_selected_apps_scrambled
                )
            }

            it.findViewById<RecyclerView>(R.id.recycler_view_scrambled)?.apply {
                (adapter as ToggleAppsAdapter?)?.dataSet = state.getScrambledApps()
            }
            it.findViewById<RecyclerView>(R.id.recycler_view_to_select)?.apply {
                (adapter as ToggleAppsAdapter?)?.dataSet = state.getApps()
            }

            val viewIdsToHide = listOf(
                R.id.ipscrambling_activated,
                R.id.recycler_view_scrambled,
                R.id.ipscrambling_select_apps,
                R.id.recycler_view_to_select,
                R.id.ipscrambling_location
            )
            val progressBar = it.findViewById<ProgressBar>(R.id.ipscrambling_loading)

            when {
                state.mode in listOf(
                    IIpScramblerModule.Status.STARTING,
                    IIpScramblerModule.Status.STOPPING
                )
                    || state.availableApps.isEmpty() -> {
                    progressBar?.visibility = View.VISIBLE
                    viewIdsToHide.forEach { viewId -> it.findViewById<View>(viewId)?.visibility = View.GONE }
                }
                else -> {
                    progressBar?.visibility = View.GONE
                    viewIdsToHide.forEach { viewId -> it.findViewById<View>(viewId)?.visibility = View.VISIBLE }
                }
            }
        }
    }

    override fun actions(): Flow<InternetPrivacyFeature.Action> = viewModel.actions
}
