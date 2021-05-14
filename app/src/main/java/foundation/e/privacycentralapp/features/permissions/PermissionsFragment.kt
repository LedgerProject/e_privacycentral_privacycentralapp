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

class PermissionsFragment :
    NavToolbarFragment(R.layout.fragment_permissions),
    MVIView<PermissionsFeature.State, PermissionsFeature.Action> {

    private val viewModel: PermissionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.permissionsFeature.takeView(this, this@PermissionsFragment)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.submitAction(PermissionsFeature.Action.ObservePermissions)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getTitle(): String = "My Apps Permission"

    override fun render(state: PermissionsFeature.State) {
        view?.findViewById<RecyclerView>(R.id.recylcer_view_permissions)?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = PermissionsAdapter(requireContext(), state.permissions) { permissionId ->
                requireActivity().supportFragmentManager.commit {
                    val bundle = bundleOf("PERMISSION_ID" to permissionId)
                    add<PermissionAppsFragment>(R.id.container, args = bundle)
                    setReorderingAllowed(true)
                    addToBackStack("permissions")
                }
            }
        }
    }

    override fun actions(): Flow<PermissionsFeature.Action> = viewModel.actions
}
