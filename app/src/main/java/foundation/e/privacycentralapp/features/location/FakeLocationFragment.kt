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

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationUpdate
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.DependencyContainer
import foundation.e.privacycentralapp.PrivacyCentralApplication
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment
import foundation.e.privacycentralapp.databinding.FragmentFakeLocationBinding
import foundation.e.privacycentralapp.domain.entities.LocationMode
import foundation.e.privacycentralapp.extensions.viewModelProviderFactoryOf
import foundation.e.privacycentralapp.features.location.FakeLocationFeature.Action
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FakeLocationFragment :
    NavToolbarFragment(R.layout.fragment_fake_location),
    MVIView<FakeLocationFeature.State, FakeLocationFeature.Action>,
    PermissionsListener {

    private var isCameraMoved: Boolean = false
    private lateinit var permissionsManager: PermissionsManager

    private val dependencyContainer: DependencyContainer by lazy {
        (this.requireActivity().application as PrivacyCentralApplication).dependencyContainer
    }

    private val viewModel: FakeLocationViewModel by viewModels {
        viewModelProviderFactoryOf { dependencyContainer.fakeLocationViewModelFactory.create() }
    }

    private lateinit var binding: FragmentFakeLocationBinding

    private var mapboxMap: MapboxMap? = null
    private var locationComponent: LocationComponent? = null
    private var hoveringMarker: ImageView? = null

    private var inputJob: Job? = null

    private var displayedLocation: Pair<Float, Float>? = null

    // Callback which updates the map in realtime.
    private val locationChangeCallback: LocationEngineCallback<LocationEngineResult> =
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                result?.lastLocation?.let {
                    displayedLocation = it.latitude.toFloat() to it.longitude.toFloat()
                    mapboxMap?.locationComponent?.forceLocationUpdate(
                        LocationUpdate.Builder().location(it).animationDuration(100)
                            .build()
                    )
                    if (!isCameraMoved) {
                        mapboxMap?.animateCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            )
                        )
                    }
                    // Only update location when location mode is set to real location or random location.
                    // It basically triggers a UI update.
                    // if (viewModel.fakeLocationFeature.state.value.location.mode != LocationMode.SPECIFIC_LOCATION) {
                    //     viewModel.submitAction(
                    //         FakeLocationFeature.Action.UpdateLocationAction(
                    //             LatLng(
                    //                 it.latitude,
                    //                 it.longitude
                    //             )
                    //         )
                    //     )
                    // }
                }
            }

            override fun onFailure(exception: Exception) {
                Log.e(TAG, "${exception.message}")
            }
        }

    companion object {
        private const val DEBOUNCE_PERIOD = 1000L
        private const val TAG = "FakeLocationFragment"
        private const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        private const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
        private const val DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.fakeLocationFeature.takeView(this, this@FakeLocationFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.fakeLocationFeature.singleEvents.collect { event ->
                when (event) {
                    is FakeLocationFeature.SingleEvent.ErrorEvent -> {
                        displayToast(event.error)
                    }
                    is FakeLocationFeature.SingleEvent.LocationUpdatedEvent ->
                        updateLocation(event.location)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_key))
    }

    override fun getTitle(): String = getString(R.string.location_title)

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFakeLocationBinding.bind(view)

        binding.mapView.setup(savedInstanceState) { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.getUiSettings().isRotateGesturesEnabled = false
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                enableLocationPlugin(style)

                mapboxMap.addOnCameraMoveListener {
                    if (binding.mapView.isEnabled) {
                        mapboxMap.cameraPosition.target.let {
                            viewModel.submitAction(
                                Action.SetSpecificLocationAction(
                                    it.latitude.toFloat(),
                                    it.longitude.toFloat()
                                )
                            )
                        }
                    }
                }
                // Bind click listeners once map is ready.
                bindClickListeners()
            }
        }
    }

    private fun getCoordinatesAfterTextChanged(
        inputLayout: TextInputLayout,
        editText: TextInputEditText,
        isLat: Boolean
    ) = { editable: Editable? ->
        inputJob?.cancel()
        if (editable != null && editable.length > 0 && editText.isEnabled) {
            inputJob = lifecycleScope.launch {
                delay(DEBOUNCE_PERIOD)
                ensureActive()
                try {
                    val value = editable.toString().toFloat()
                    val maxValue = if (isLat) 90f else 180f

                    if (value > maxValue || value < -maxValue) {
                        throw NumberFormatException("value $value is out of bounds")
                    }
                    inputLayout.error = null

                    inputLayout.setEndIconDrawable(R.drawable.ic_valid)
                    inputLayout.endIconMode = END_ICON_CUSTOM

                    // Here, value is valid, try to send the values
                    try {
                        val lat = binding.edittextLatitude.text.toString().toFloat()
                        val lon = binding.edittextLongitude.text.toString().toFloat()
                        if (lat <= 90f && lat >= -90f && lon <= 180f && lon >= -180f) {
                            Log.e("UpdateText", "")
                            mapboxMap?.moveCamera(
                                CameraUpdateFactory.newLatLng(
                                    LatLng(lat.toDouble(), lon.toDouble())
                                )
                            )
                            // viewModel.submitAction(Action.SetSpecificLocationAction(lat, lon))
                        }
                    } catch (e: NumberFormatException) {
                    }
                } catch (e: NumberFormatException) {
                    inputLayout.endIconMode = END_ICON_NONE
                    inputLayout.error = getString(R.string.location_input_error)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindClickListeners() {
        binding.radioUseRealLocation.setOnClickListener {
            viewModel.submitAction(Action.UseRealLocationAction)
        }
        binding.radioUseRandomLocation.setOnClickListener {
            viewModel.submitAction(Action.UseRandomLocationAction)
        }
        binding.radioUseSpecificLocation.setOnClickListener {
            mapboxMap?.cameraPosition?.target?.let {
                viewModel.submitAction(
                    Action.SetSpecificLocationAction(it.latitude.toFloat(), it.longitude.toFloat())
                )
            }
        }

        // binding.mapView.addOnTouchListener { _, event ->
        //     //mapboxMap.addOnCameraMoveStartedListener {
        //         // Show marker when user starts to move across the map.
        //     if (event.action == ACTION_DOWN && binding.mapView.isEnabled) {
        //         hoveringMarker?.visibility = View.VISIBLE
        //         isCameraMoved = true
        //     }
        //     binding.mapView.onTouchEvent(event)
        // }

        binding.edittextLatitude.addTextChangedListener(
            afterTextChanged = getCoordinatesAfterTextChanged(
                binding.textlayoutLatitude,
                binding.edittextLatitude,
                true
            )
        )

        binding.edittextLongitude.addTextChangedListener(
            afterTextChanged = getCoordinatesAfterTextChanged(
                binding.textlayoutLongitude,
                binding.edittextLongitude,
                false
            )
        )
    }

    @SuppressLint("MissingPermission")
    override fun render(state: FakeLocationFeature.State) {
        binding.radioUseRandomLocation.isChecked = (state.mode == LocationMode.RANDOM_LOCATION)
        binding.radioUseSpecificLocation.isChecked =
            (state.mode == LocationMode.SPECIFIC_LOCATION)
        binding.radioUseRealLocation.isChecked = (state.mode == LocationMode.REAL_LOCATION)

        binding.mapView.isEnabled = (state.mode == LocationMode.SPECIFIC_LOCATION)

        if (state.mode != LocationMode.SPECIFIC_LOCATION) {
            isCameraMoved = false
            binding.centeredMarker.isVisible = false
        } else {
            binding.mapLoader.isVisible = false
            binding.mapOverlay.isVisible = false
            binding.centeredMarker.isVisible = true

            mapboxMap?.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(state.specificLatitude?.toDouble() ?: 0.0, state.specificLongitude?.toDouble() ?: 0.0)
                )
            )
        }

        binding.textlayoutLatitude.isVisible = (state.mode == LocationMode.SPECIFIC_LOCATION)
        binding.textlayoutLongitude.isVisible = (state.mode == LocationMode.SPECIFIC_LOCATION)

        binding.edittextLatitude.setText(state.specificLatitude?.toString())
        binding.edittextLongitude.setText(state.specificLongitude?.toString())
    }

    override fun actions(): Flow<Action> = viewModel.actions

    @SuppressLint("MissingPermission")
    private fun updateLocation(lastLocation: Location?) {
        lastLocation?.let { location ->
            locationComponent?.isLocationComponentEnabled = true
            val locationUpdate = LocationUpdate.Builder()
                .location(location)
                .animationDuration(100)
                .build()
            locationComponent?.forceLocationUpdate(locationUpdate)

            // if (binding.mapView.isEnabled && !isCameraMoved) {
            //     binding.mapView.isEnabled = false
            //     mapboxMap?.moveCamera(
            //         CameraUpdateFactory.newLatLng(
            //             LatLng(
            //                 location.latitude,
            //                 location.longitude
            //             )
            //         )
            //     )
            //     isCameraMoved = false
            //     binding.mapView.isEnabled = true
            if (!binding.mapView.isEnabled) {
                binding.mapLoader.isVisible = false
                binding.mapOverlay.isVisible = false
                mapboxMap?.animateCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(location.latitude, location.longitude)
                    )
                )
            }
        } ?: run {
            locationComponent?.isLocationComponentEnabled = false
            if (!binding.mapView.isEnabled) {
                binding.mapLoader.isVisible = true
                binding.mapOverlay.isVisible = true
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationPlugin(@NonNull loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        // if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
        locationComponent = mapboxMap?.locationComponent
        locationComponent?.activateLocationComponent(
            LocationComponentActivationOptions.builder(
                requireContext(), loadedMapStyle
            ).useDefaultLocationEngine(false).build()
        )
        locationComponent?.isLocationComponentEnabled = true
        locationComponent?.cameraMode = CameraMode.NONE
        locationComponent?.renderMode = RenderMode.NORMAL
        //     //locationComponent.locationEngine?.let {
        //         it.requestLocationUpdates(
        //             LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
        //                 .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
        //                 .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build(),
        //             locationChangeCallback,
        //             Looper.getMainLooper()
        //         )
        //         it.getLastLocation(locationChangeCallback)
        //     }
        // } else {
        //     permissionsManager = PermissionsManager(this)
        //     permissionsManager.requestLocationPermissions(requireActivity())
        // }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        viewModel.submitAction(Action.Init)
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.submitAction(Action.LeaveScreen)
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            requireContext(),
            R.string.user_location_permission_explanation,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            val style = mapboxMap?.style
            if (style != null) {
                enableLocationPlugin(style)
            }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
