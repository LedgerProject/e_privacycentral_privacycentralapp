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

package foundation.e.privacycentralapp.features.trackers.apptrackers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacymodules.trackers.Tracker

class ToggleTrackersAdapter(
    private val itemsLayout: Int,
    private val listener: (Tracker, Boolean) -> Unit
) :
    RecyclerView.Adapter<ToggleTrackersAdapter.ViewHolder>() {

    var isEnabled = true

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)

        val toggle: Switch = view.findViewById(R.id.toggle)

        fun bind(item: Pair<Tracker, Boolean>, isEnabled: Boolean) {
            title.text = item.first.label
            toggle.isChecked = item.second
            toggle.isEnabled = isEnabled
        }
    }

    private var dataSet: List<Pair<Tracker, Boolean>> = emptyList()

    fun updateDataSet(new: List<Pair<Tracker, Boolean>>, isEnabled: Boolean) {
        this.isEnabled = isEnabled
        dataSet = new
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(itemsLayout, parent, false)
        val holder = ViewHolder(view)
        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            listener(dataSet[holder.adapterPosition].first, isChecked)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val permission = dataSet[position]
        holder.bind(permission, isEnabled)
    }

    override fun getItemCount(): Int = dataSet.size
}
