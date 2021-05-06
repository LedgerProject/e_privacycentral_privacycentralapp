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
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.NonNull
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.LocationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FakeLocationFragment :
    Fragment(R.layout.fragment_fake_location),
    MVIView<FakeLocationFeature.State, FakeLocationFeature.Action>,
    PermissionsListener {

    private lateinit var permissionsManager: PermissionsManager
    private val viewModel: FakeLocationViewModel by viewModels()

    private lateinit var mapView: FakeLocationMapView
    private lateinit var mapboxMap: MapboxMap
    private var hoveringMarker: ImageView? = null

    private var mutableLatLongFlow = MutableStateFlow(LatLng())
    private var latLong = mutableLatLongFlow.asStateFlow()

    companion object {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_key))
    }

    private fun displayToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupToolbar(toolbar)
        mapView = view.findViewById<FakeLocationMapView>(R.id.mapView)
            .setup(savedInstanceState) { mapboxMap ->
                this.mapboxMap = mapboxMap
                mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                    enableLocationPlugin(style)

                    hoveringMarker = ImageView(requireContext())
                        .apply {
                            setImageResource(R.drawable.mapbox_marker_icon_default)
                            val params = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
                            )
                            layoutParams = params
                        }
                    mapView.addView(hoveringMarker)
                    initDroppedMarker(style)
                    mapboxMap.addOnCameraMoveListener {
                        mutableLatLongFlow.value = mapboxMap.cameraPosition.target
                    }
                    mapboxMap.addOnCameraIdleListener { Log.d("Mapview", "camera move ended") }
                }
            }
        bindClickListeners(view)
    }

    private fun bindClickListeners(fragmentView: View) {
        val latEditText =
            fragmentView.findViewById<TextInputLayout>(R.id.edittext_latitude).editText
        val longEditText =
            fragmentView.findViewById<TextInputLayout>(R.id.edittext_longitude).editText

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
                    val latitude = latEditText?.text.toString().toDouble()
                    val longitude = longEditText?.text.toString().toDouble()
                    saveSpecificLocation(latitude, longitude)
                }
        }

        lifecycleScope.launch {
            latLong.collect {
                latEditText?.text =
                    Editable.Factory.getInstance().newEditable(it.latitude.toString())
                longEditText?.text =
                    Editable.Factory.getInstance().newEditable(it.longitude.toString())
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
                        }
                    LocationMode.CUSTOM_LOCATION -> view?.let {
                        it.findViewById<RadioButton>(R.id.radio_use_specific_location).isChecked =
                            true
                    }
                }
            }
            FakeLocationFeature.State.InitialState -> {
            }
        }
    }

    override fun actions(): Flow<FakeLocationFeature.Action> = viewModel.actions

    @SuppressLint("MissingPermission")
    private fun enableLocationPlugin(@NonNull loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {

            val locationComponent: LocationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    requireContext(), loadedMapStyle
                ).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.NORMAL
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(requireActivity())
        }
    }

    private fun initDroppedMarker(loadedMapStyle: Style) {
        // Add the marker image to map
        loadedMapStyle.apply {
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_map_marker_blue,
                requireContext().theme
            )
                ?.let {
                    addImage(
                        "dropped-icon-image",
                        it
                    )
                }
            addSource(GeoJsonSource("dropped-marker-source-id"))
            addLayer(
                SymbolLayer(
                    DROPPED_MARKER_LAYER_ID,
                    "dropped-marker-source-id"
                ).apply {
                    setProperties(
                        iconImage("dropped-icon-image"),
                        visibility(NONE),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                    )
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
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
            val style = mapboxMap.style
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

fun FakeLocationMapView.setup(savedInstanceState: Bundle?, callback: OnMapReadyCallback) =
    this.apply {
        onCreate(savedInstanceState)
        getMapAsync(callback)
    }
