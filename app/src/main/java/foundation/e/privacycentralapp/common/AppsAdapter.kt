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

package foundation.e.privacycentralapp.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacymodules.permissions.data.ApplicationDescription

class AppsAdapter(
    private val itemsLayout: Int,
    private val listener: (String) -> Unit
) :
    RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.title)

        fun bind(item: ApplicationDescription) {
            appName.text = item.label

            itemView.findViewById<ImageView>(R.id.icon).setImageDrawable(item.icon)
        }
    }

    var dataSet: List<ApplicationDescription> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(itemsLayout, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener { _ ->
            listener(dataSet[holder.adapterPosition].packageName)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = dataSet[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = dataSet.size
}
