package cn.campus.ledger.overview

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.campus.ledger.Ui

class BudgetSlot : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        state: Bundle?
    ): View {
        val status = Ui.text(requireContext(), 17, "#52685C", false)
        status.gravity = Gravity.CENTER
        status.text = "月预算功能将在下一阶段开放"
        return status
    }
}
