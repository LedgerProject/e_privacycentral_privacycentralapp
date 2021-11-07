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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocalStateRepository(context: Context) {
    companion object {
        private const val SHARED_PREFS_FILE = "localState"
        private const val KEY_QUICK_PRIVACY = "quickPrivacy"
        private const val KEY_IP_SCRAMBLING = "ipScrambling"
        private const val KEY_FAKE_LATITUDE = "fakeLatitude"
        private const val KEY_FAKE_LONGITUDE = "fakeLongitude"
    }

    private val sharedPref = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

    private val quickPrivacyEnabledMutableFlow = MutableStateFlow<Boolean>(sharedPref.getBoolean(KEY_QUICK_PRIVACY, false))
    var isQuickPrivacyEnabled: Boolean
        get() = quickPrivacyEnabledMutableFlow.value
        set(value) {
            set(KEY_QUICK_PRIVACY, value)
            quickPrivacyEnabledMutableFlow.value = value
        }

    var quickPrivacyEnabledFlow: Flow<Boolean> = quickPrivacyEnabledMutableFlow

    var fakeLocation: Pair<Float, Float>?
        get() = if (sharedPref.contains(KEY_FAKE_LATITUDE) && sharedPref.contains(
                    KEY_FAKE_LONGITUDE))
                        Pair(
                            sharedPref.getFloat(KEY_FAKE_LATITUDE, 0f),
                            sharedPref.getFloat(KEY_FAKE_LONGITUDE, 0f))
                else null
        set(value) {
            if (value == null) {
                sharedPref.edit()
                    .remove(KEY_FAKE_LATITUDE)
                    .remove(KEY_FAKE_LONGITUDE)
                    .commit()
            } else {
                sharedPref.edit()
                    .putFloat(KEY_FAKE_LATITUDE, value.first)
                    .putFloat(KEY_FAKE_LONGITUDE, value.second)
                    .commit()
            }
        }

    var isIpScramblingEnabled: Boolean
        get() = sharedPref.getBoolean(KEY_IP_SCRAMBLING, false)
        set(value) = set(KEY_IP_SCRAMBLING, value)

    private fun set(key: String, value: Boolean) {
        sharedPref.edit().putBoolean(key, value).commit()
    }
}
