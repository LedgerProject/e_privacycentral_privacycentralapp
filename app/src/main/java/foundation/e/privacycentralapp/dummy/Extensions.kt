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

package foundation.e.privacycentralapp.dummy

import android.content.Context
import foundation.e.privacycentralapp.R
import foundation.e.privacycentralapp.domain.entities.LocationMode

fun LocationMode.mapToString(context: Context): String = when (this) {
    LocationMode.REAL_LOCATION -> context.getString(R.string.real_location_mode)
    LocationMode.RANDOM_LOCATION -> context.getString(R.string.random_location_mode)
    LocationMode.SPECIFIC_LOCATION -> context.getString(R.string.fake_location_mode)
}
