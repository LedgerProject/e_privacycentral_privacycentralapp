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

import androidx.annotation.ColorInt
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

fun customizeBarChart(barChart: BarChart) {
    barChart.apply {
        description = null
        setTouchEnabled(false)
        setDrawGridBackground(false)
        setDrawBorders(false)
        axisLeft.isEnabled = false
        axisRight.isEnabled = false

        legend.isEnabled = false

        xAxis.apply {
            isEnabled = true
            position = XAxis.XAxisPosition.BOTH_SIDED
            setDrawGridLines(false)
            yOffset = 32f
            setDrawLabels(false)
            // setDrawLimitLinesBehindData(true)
            setDrawValueAboveBar(false)
        }
    }
}

fun updateGraphData(values: List<Int>, graph: BarChart, @ColorInt graphColor: Int) {

    val trackersDataSet = BarDataSet(
        values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) },
        ""
    ).apply {
        color = graphColor
        setDrawValues(false)
    }

    graph.data = BarData(trackersDataSet)
    graph.invalidate()
}
