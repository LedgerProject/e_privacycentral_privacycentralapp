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
import android.widget.RadioButton
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.InternetPrivacyMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class InternetPrivacyFragment :
    Fragment(R.layout.fragment_internet_activity_policy),
    MVIView<InternetPrivacyFeature.State, InternetPrivacyFeature.Action> {

    private val viewModel: InternetPrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.internetPrivacyFeature.takeView(this, this@InternetPrivacyFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.internetPrivacyFeature.singleEvents.collect { event ->
                when (event) {
                    is InternetPrivacyFeature.SingleEvent.ErrorEvent -> displayToast(event.error)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupToolbar(toolbar)
        bindClickListeners(view)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity()
        activity.setActionBar(toolbar)
        activity.title = "My Internet Activity Privacy"
    }

    private fun bindClickListeners(fragmentView: View) {
        fragmentView.let {
            it.findViewById<RadioButton>(R.id.radio_use_real_ip)
                .setOnClickListener { radioButton ->
                    toggleIP(radioButton)
                }
            it.findViewById<RadioButton>(R.id.radio_use_hidden_ip)
                .setOnClickListener { radioButton ->
                    toggleIP(radioButton)
                }
        }
    }

    private fun toggleIP(radioButton: View?) {
        if (radioButton is RadioButton) {
            val checked = radioButton.isChecked
            when (radioButton.id) {
                R.id.radio_use_real_ip ->
                    if (checked) {
                        viewModel.submitAction(InternetPrivacyFeature.Action.UseRealIPAction)
                    }
                R.id.radio_use_hidden_ip ->
                    if (checked) {
                        viewModel.submitAction(InternetPrivacyFeature.Action.UseHiddenIPAction)
                    }
            }
        }
    }

    override fun render(state: InternetPrivacyFeature.State) {
        view?.let {
            it.findViewById<RadioButton>(R.id.radio_use_hidden_ip).isChecked =
                state.mode == InternetPrivacyMode.HIDE_IP
            it.findViewById<RadioButton>(R.id.radio_use_real_ip).isChecked =
                state.mode == InternetPrivacyMode.REAL_IP
        }
    }

    override fun actions(): Flow<InternetPrivacyFeature.Action> = viewModel.actions
}
