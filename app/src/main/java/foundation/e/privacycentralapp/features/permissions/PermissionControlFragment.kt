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
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.DummyDataSource

class PermissionControlFragment : Fragment(R.layout.fragment_permission_control) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupToolbar(toolbar)

        val permissionId = requireArguments().getInt("PERMISSION_ID")
        loadData(view, permissionId)
    }

    private fun loadData(view: View, permissionId: Int) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recylcer_view_permission_apps)
        val permission = DummyDataSource.getPermission(permissionId)
        val listOfPackages = mutableListOf<Pair<String, Boolean>>()
        permission.packagesRequested.forEach {
            listOfPackages.add(it to permission.packagesAllowed.contains(it))
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = PermissionAppsAdapter(listOfPackages)
        view.findViewById<TextView>(R.id.permission_control).text =
            getString(R.string.apps_access_to_permission, permission.name)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity()
        activity.setActionBar(toolbar)
        activity.title = "My Apps Permission"
    }
}
