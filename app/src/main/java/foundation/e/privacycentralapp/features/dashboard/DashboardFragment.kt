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
import android.view.View
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import foundation.e.flowmvi.MVIView
import foundation.e.privacycentralapp.R
import kotlinx.coroutines.flow.Flow

class DashboardFragment :
    Fragment(R.layout.fragment_dashboard),
    MVIView<DashboardFeature.State, DashboardFeature.Action> {

    private val viewModel: DashboardViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.homeFeature.takeView(this, this@DashboardFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupToolbar(toolbar)
        addClickToMore(view.findViewById(R.id.personal_leakag_info))
        view.let {
            it.findViewById<TextView>(R.id.tap_to_enable_quick_protection).setOnClickListener {
                viewModel.submitAction(DashboardFeature.Action.ShowQuickPrivacyProtectionInfoAction)
            }
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val activity = requireActivity()
        activity.setActionBar(toolbar)
        activity.title = "My Privacy Dashboard"
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

    override fun render(state: DashboardFeature.State) {
        when (state) {
            is DashboardFeature.State.QuickProtectionState -> {
                requireActivity().supportFragmentManager.commit {
                    add<QuickProtectionFragment>(R.id.container)
                    setReorderingAllowed(true)
                    addToBackStack("dashboard")
                }
            }
            else -> {
                // TODO: any remaining state must either be handled or needs to be passed down to the UI.
            }
        }
    }

    override fun actions(): Flow<DashboardFeature.Action> = viewModel.actions
}
