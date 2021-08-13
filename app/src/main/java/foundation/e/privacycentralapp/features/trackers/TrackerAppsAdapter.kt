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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.dummy.Tracker

class TrackerAppsAdapter(
    private var tracker: Tracker,
    private val listener: (Tracker, Boolean) -> Unit
) :
    RecyclerView.Adapter<TrackerAppsAdapter.TrackerViewHolder>() {

    class TrackerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.app_title)
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val toggleBlocker: Switch = view.findViewById(R.id.toggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_toggle, parent, false)
        val holder = TrackerViewHolder(view)
        holder.toggleBlocker.setOnClickListener {
            if (it is Switch) {
                listener(tracker, it.isChecked)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
        val app = tracker.trackedApps[position]
        holder.titleView.text = app.appName
        holder.toggleBlocker.isChecked = app.isEnabled
    }

    override fun getItemCount(): Int = tracker.trackedApps.size
}
