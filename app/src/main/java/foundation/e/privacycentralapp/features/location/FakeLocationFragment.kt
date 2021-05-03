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

package foundation.e.privacycentralapp.features.location

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.LocationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class FakeLocationFragment :
    Fragment(R.layout.fragment_fake_location),
    MVIView<FakeLocationFeature.State, FakeLocationFeature.Action> {

    private val viewModel: FakeLocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.fakeLocationFeature.takeView(this, this@FakeLocationFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.fakeLocationFeature.singleEvents.collect { event ->
                when (event) {
                    is FakeLocationFeature.SingleEvent.RandomLocationSelectedEvent -> displayToast("Random location selected")
                    is FakeLocationFeature.SingleEvent.SpecificLocationSavedEvent -> displayToast("Specific location selected")
                    is FakeLocationFeature.SingleEvent.ErrorEvent -> displayToast(event.error)
                    FakeLocationFeature.SingleEvent.RealLocationSelectedEvent -> displayToast("Real location selected")
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(FakeLocationFeature.Action.ObserveLocationAction)
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

    private fun bindClickListeners(fragmentView: View) {
        fragmentView.let {
            it.findViewById<RadioButton>(R.id.radio_use_real_location)
                .setOnClickListener { radioButton ->
                    toggleLocationType(radioButton)
                }
            it.findViewById<RadioButton>(R.id.radio_use_random_location)
                .setOnClickListener { radioButton ->
                    toggleLocationType(radioButton)
                }
            it.findViewById<RadioButton>(R.id.radio_use_specific_location)
                .setOnClickListener { radioButton ->
                    toggleLocationType(radioButton)
                }
            it.findViewById<Button>(R.id.button_add_location)
                .setOnClickListener {
                    val latitude =
                        fragmentView.findViewById<TextInputLayout>(R.id.edittext_latitude).editText?.text.toString()
                            .toDouble()
                    val longitude =
                        fragmentView.findViewById<TextInputLayout>(R.id.edittext_longitude).editText?.text.toString()
                            .toDouble()
                    saveSpecificLocation(latitude, longitude)
                }
        }
    }

    private fun saveSpecificLocation(latitude: Double, longitude: Double) {
        viewModel.submitAction(
            FakeLocationFeature.Action.AddSpecificLocationAction(latitude, longitude)
        )
    }

    private fun toggleLocationType(radioButton: View?) {
        if (radioButton is RadioButton) {
            val checked = radioButton.isChecked
            when (radioButton.id) {
                R.id.radio_use_real_location ->
                    if (checked) {
                        viewModel.submitAction(FakeLocationFeature.Action.UseRealLocationAction)
                    }
                R.id.radio_use_random_location ->
                    if (checked) {
                        viewModel.submitAction(FakeLocationFeature.Action.UseRandomLocationAction)
                    }
                R.id.radio_use_specific_location ->
                    if (checked) {
                        viewModel.submitAction(FakeLocationFeature.Action.UseSpecificLocationAction)
                    }
            }
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity()
        activity.setActionBar(toolbar)
        activity.title = "Fake My Location"
    }

    override fun render(state: FakeLocationFeature.State) {
        when (state) {
            is FakeLocationFeature.State.LocationState -> {
                Log.d("FakeMyLocation", "State: $state")
                when (state.location.mode) {
                    LocationMode.REAL_LOCATION, LocationMode.RANDOM_LOCATION ->
                        view?.let {
                            it.findViewById<RadioButton>(R.id.radio_use_random_location).isChecked =
                                (state.location.mode == LocationMode.RANDOM_LOCATION)
                            it.findViewById<RadioButton>(R.id.radio_use_real_location).isChecked =
                                (state.location.mode == LocationMode.REAL_LOCATION)
                            it.findViewById<ImageView>(R.id.dummy_img_map).visibility = View.GONE
                            it.findViewById<TextInputLayout>(R.id.edittext_latitude).visibility =
                                View.GONE
                            it.findViewById<TextInputLayout>(R.id.edittext_longitude).visibility =
                                View.GONE
                            it.findViewById<Button>(R.id.button_add_location).visibility = View.GONE
                        }
                    LocationMode.CUSTOM_LOCATION -> view?.let {
                        it.findViewById<RadioButton>(R.id.radio_use_specific_location).isChecked =
                            true
                        it.findViewById<ImageView>(R.id.dummy_img_map).visibility = View.VISIBLE
                        it.findViewById<TextInputLayout>(R.id.edittext_latitude).apply {
                            visibility = View.VISIBLE
                            editText?.text = Editable.Factory.getInstance()
                                .newEditable(state.location.latitude.toString())
                        }
                        it.findViewById<TextInputLayout>(R.id.edittext_longitude).apply {
                            visibility = View.VISIBLE
                            editText?.text = Editable.Factory.getInstance()
                                .newEditable(state.location.longitude.toString())
                        }
                        it.findViewById<Button>(R.id.button_add_location).visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun actions(): Flow<FakeLocationFeature.Action> = viewModel.actions
}
