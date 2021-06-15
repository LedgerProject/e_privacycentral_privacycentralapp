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

package foundation.e.privacycentralapp.features.trackers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.Tracker

class TrackersAdapter(
    private var dataSet: List<Tracker> = emptyList(),
    private val listener: (Tracker) -> Unit
) :
    RecyclerView.Adapter<TrackersAdapter.TrackerViewHolder>() {

    class TrackerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_tracker, parent, false)
        val holder = TrackerViewHolder(view)
        holder.titleView.setOnClickListener { listener(dataSet[holder.adapterPosition]) }
        return holder
    }

    override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
        val tracker = dataSet[position]
        holder.titleView.text = tracker.name
    }

    override fun getItemCount(): Int = dataSet.size

    fun setData(data: List<Tracker>) {
        this.dataSet = data
    }
}
