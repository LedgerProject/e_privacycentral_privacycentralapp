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

package foundation.e.privacycentralapp.features.permissions

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class PermissionAppsFragment :
    Fragment(R.layout.fragment_permission_apps),
    MVIView<PermissionsFeature.State, PermissionsFeature.Action> {

    private val viewModel: PermissionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.permissionsFeature.takeView(this, this@PermissionAppsFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.permissionsFeature.singleEvents.collect { event ->
                when (event) {
                    is PermissionsFeature.SingleEvent.ErrorEvent -> displayToast(event.error)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(
                PermissionsFeature.Action.LoadPermissionApps(
                    requireArguments().getInt(
                        "PERMISSION_ID"
                    )
                )
            )
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
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity()
        activity.setActionBar(toolbar)
        activity.title = "My Apps Permission"
    }

    override fun render(state: PermissionsFeature.State) {
        state.currentPermission?.let { permission ->
            view?.findViewById<RecyclerView>(R.id.recylcer_view_permission_apps)?.apply {
                val listOfPackages = mutableListOf<Pair<String, Boolean>>()
                permission.packagesRequested.forEach {
                    listOfPackages.add(it to permission.packagesAllowed.contains(it))
                }
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = PermissionAppsAdapter(listOfPackages) { packageName, grant ->
                    viewModel.submitAction(
                        PermissionsFeature.Action.TogglePermissionAction(
                            packageName,
                            grant
                        )
                    )
                }
            }
            view?.findViewById<TextView>(R.id.permission_control)?.text =
                getString(R.string.apps_access_to_permission, permission.name)
        }
    }

    override fun actions(): Flow<PermissionsFeature.Action> = viewModel.actions
}
