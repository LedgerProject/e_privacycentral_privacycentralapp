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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.Permission

class PermissionsAdapter(
    private val context: Context,
    private val dataSet: List<Permission>,
    private val listener: (Int) -> Unit
) :
    RecyclerView.Adapter<PermissionsAdapter.PermissionViewHolder>() {

    class PermissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.permission_title)
        val permissionCountView: TextView = view.findViewById(R.id.permission_count)
        val permissionIcon: ImageView = view.findViewById(R.id.permission_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_permission, parent, false)
        val holder = PermissionViewHolder(view)
        view.setOnClickListener { listener(holder.adapterPosition) }
        return holder
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        val permission = dataSet[position]
        holder.nameView.text = permission.name
        holder.permissionCountView.text = context.getString(
            R.string.apps_allowed,
            permission.packagesAllowed.size,
            permission.packagesRequested.size
        )
        holder.permissionIcon.setImageResource(permission.iconId)
    }

    override fun getItemCount(): Int = dataSet.size
}
