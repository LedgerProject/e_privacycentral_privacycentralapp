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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.DependencyContainer
import foundation.e.privacycentralapp.PrivacyCentralApplication
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import foundation.e.privacycentralapp.common.ToggleAppsAdapter
import foundation.e.privacycentralapp.databinding.FragmentInternetActivityPolicyBinding
import foundation.e.privacycentralapp.domain.entities.InternetPrivacyMode
import foundation.e.privacycentralapp.extensions.viewModelProviderFactoryOf
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

    private lateinit var binding: FragmentInternetActivityPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.internetPrivacyFeature.takeView(this, this@InternetPrivacyFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.internetPrivacyFeature.singleEvents.collect { event ->
                when (event) {
                    is InternetPrivacyFeature.SingleEvent.ErrorEvent -> {
                        displayToast(event.error)
                    }
                    is InternetPrivacyFeature.SingleEvent.StartAndroidVpnActivityEvent -> {
                        launchAndroidVpnDisclaimer.launch(event.intent)
                    }
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
        binding = FragmentInternetActivityPolicyBinding.bind(view)

        binding.apps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = ToggleAppsAdapter(R.layout.ipscrambling_item_app_toggle) { packageName, isIpScrambled ->
                viewModel.submitAction(
                    InternetPrivacyFeature.Action.ToggleAppIpScrambled(
                        packageName,
                        isIpScrambled
                    )
                )
            }
        }

        binding.radioUseRealIp.radiobutton.setOnClickListener {
            viewModel.submitAction(InternetPrivacyFeature.Action.UseRealIPAction)
        }

        binding.radioUseHiddenIp.radiobutton.setOnClickListener {
            viewModel.submitAction(InternetPrivacyFeature.Action.UseHiddenIPAction)
        }

        binding.executePendingBindings()
    }

    override fun getTitle(): String = getString(R.string.ipscrambling_title)

    override fun render(state: InternetPrivacyFeature.State) {
        binding.radioUseHiddenIp.radiobutton.apply {
            isChecked = state.mode in listOf(
                InternetPrivacyMode.HIDE_IP,
                InternetPrivacyMode.HIDE_IP_LOADING
            )
            isEnabled = state.mode != InternetPrivacyMode.HIDE_IP_LOADING
        }
        binding.radioUseRealIp.radiobutton.apply {
            isChecked =
                state.mode in listOf(
                InternetPrivacyMode.REAL_IP,
                InternetPrivacyMode.REAL_IP_LOADING
            )
            isEnabled = state.mode != InternetPrivacyMode.REAL_IP_LOADING
        }

        binding.ipscramblingSelectLocation.apply {
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
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                    viewModel.submitAction(InternetPrivacyFeature.Action.SelectLocationAction(position))
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            })

            setSelection(state.selectedLocationPosition)
        }

        // TODO: this should not be mandatory.
        binding.apps.post {
            (binding.apps.adapter as ToggleAppsAdapter?)?.dataSet = state.getApps()
        }

        val viewIdsToHide = listOf(
            binding.ipscramblingLocationLabel,
            binding.selectLocationContainer,
            binding.ipscramblingSelectLocation,
            binding.ipscramblingSelectApps,
            binding.apps
        )

        when {
            state.mode in listOf(
                InternetPrivacyMode.HIDE_IP_LOADING,
                InternetPrivacyMode.REAL_IP_LOADING
            )
                || state.availableApps.isEmpty() -> {
                binding.loader.visibility = View.VISIBLE
                viewIdsToHide.forEach { it.visibility = View.GONE }
            }
            else -> {
                binding.loader.visibility = View.GONE
                viewIdsToHide.forEach { it.visibility = View.VISIBLE }
            }
        }
    }

    override fun actions(): Flow<InternetPrivacyFeature.Action> = viewModel.actions
}
