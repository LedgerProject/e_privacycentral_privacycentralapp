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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton

/**
 * A custom [RadioButton] which displays the radio drawable on the right side.
 */
@SuppressLint("AppCompatCustomView")
class RightRadioButton : RadioButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // Returns layout direction as right-to-left to draw the compound button on right side.
    override fun getLayoutDirection(): Int {
        return LAYOUT_DIRECTION_RTL
    }
}
