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

package foundation.e.privacycentralapp.data.repositories

import android.content.Context

class LocalStateRepository(context: Context) {
    companion object {
        private const val SHARED_PREFS_FILE = "localState"
        private const val KEY_QUICK_PRIVACY = "quickPrivacy"
    }

    val sharedPref = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

    var isQuickPrivacyEnabled: Boolean
        get() = sharedPref.getBoolean(KEY_QUICK_PRIVACY, false)
        set(value) = set(KEY_QUICK_PRIVACY, value)

    private fun set(key: String, value: Boolean) {
        sharedPref.edit().putBoolean(key, value).commit()
    }
}
