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

package foundation.e.privacycentralapp.features.dashboard

import android.content.Context
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.common.NavToolbarFragment

class QuickProtectionFragment : NavToolbarFragment(R.layout.fragment_quick_protection) {

    private val viewModel: DashboardViewModel by activityViewModels()

    override fun getTitle(): String = getString(R.string.quick_protection)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, true) {
            viewModel.submitAction(DashboardFeature.Action.ShowDashboardAction)
            this.isEnabled = false
            requireActivity().onBackPressed()
        }
    }
}
