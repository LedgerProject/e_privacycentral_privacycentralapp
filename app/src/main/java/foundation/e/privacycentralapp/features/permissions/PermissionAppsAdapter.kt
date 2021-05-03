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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R

class PermissionAppsAdapter(
    private val dataSet: List<Pair<String, Boolean>>,
    private val listener: (String, Boolean) -> Unit
) :
    RecyclerView.Adapter<PermissionAppsAdapter.PermissionViewHolder>() {

    class PermissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.app_title)

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val togglePermission: Switch = view.findViewById(R.id.togglePermission)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_permission_apps, parent, false)
        val holder = PermissionViewHolder(view)
        holder.togglePermission.setOnCheckedChangeListener { _, isChecked ->
            listener(dataSet[holder.adapterPosition].first, isChecked)
        }
        view.findViewById<Switch>(R.id.togglePermission)
        return holder
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = dataSet[position]
        holder.appName.text = permission.first
        holder.togglePermission.isChecked = permission.second
    }

    override fun getItemCount(): Int = dataSet.size
}
