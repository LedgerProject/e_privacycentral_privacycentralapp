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

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import foundation.e.privacycentralapp.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "My Privacy Dashboard"
        setSupportActionBar(findViewById(R.id.toolbar))

        addClickToMore(findViewById<TextView>(R.id.personal_leakag_info))
    }

    private fun addClickToMore(textView: TextView) {
        val clickToMore = SpannableString("Click to learn more")
        clickToMore.setSpan(
            ForegroundColorSpan(Color.parseColor("#007fff")),
            0,
            clickToMore.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.append(clickToMore)
    }
}
