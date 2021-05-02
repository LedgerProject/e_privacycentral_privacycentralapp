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
import android.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.DummyDataSource

class PermissionsFragment : Fragment(R.layout.fragment_permissions) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupToolbar(toolbar)

        loadDataIntoRecyclerView(view.findViewById(R.id.recylcer_view_permissions))
    }

    private fun loadDataIntoRecyclerView(view: RecyclerView) {
        val permissions = DummyDataSource.populatedPermission
        view.layoutManager = LinearLayoutManager(requireContext())
        view.setHasFixedSize(true)
        view.adapter = PermissionsAdapter(requireContext(), permissions) { permissionId ->
            requireActivity().supportFragmentManager.commit {
                val bundle = bundleOf("PERMISSION_ID" to permissionId)
                add<PermissionControlFragment>(R.id.container, args = bundle)
                setReorderingAllowed(true)
                addToBackStack("permissions")
            }
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity()
        activity.setActionBar(toolbar)
        activity.title = "My Apps Permission"
    }
}
