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

import androidx.annotation.LayoutRes
import com.google.android.material.appbar.MaterialToolbar
import foundation.e.privacycentralapp.R

abstract class NavToolbarFragment(@LayoutRes contentLayoutId: Int) : ToolbarFragment(contentLayoutId) {

    override fun setupToolbar(toolbar: MaterialToolbar) {
        super.setupToolbar(toolbar)
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_ic_chevron_left_24dp)
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }
}
