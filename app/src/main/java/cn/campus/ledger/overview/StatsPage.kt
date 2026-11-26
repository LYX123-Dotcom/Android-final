package cn.campus.ledger.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import cn.campus.ledger.R
import cn.campus.ledger.Ui
import cn.campus.ledger.data.Money
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class StatsPage : MonthlyPage() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        state: Bundle?
    ): View = inflater.inflate(R.layout.expense_stats, parent, false)

    override fun display(report: MonthReport) {
        val page = requireView()
        page.findViewById<TextView>(R.id.expense_total).text = Money.format(report.expense)
        val list = page.findViewById<LinearLayout>(R.id.ranking)
        list.removeAllViews()
        if (report.expense == 0L) {
            val empty = Ui.text(requireContext(), 16, "#52685C", false)
            empty.text = "本月暂无支出"
            list.addView(empty)
            return
        }
        val percent = NumberFormat.getPercentInstance(Locale.CHINA).apply {
            maximumFractionDigits = 1
        }
        for (category in report.ranking) {
            val share = category.value.toDouble() / report.expense
            val row = layoutInflater.inflate(R.layout.expense_rank_row, list, false)
            row.findViewById<TextView>(R.id.category_name).text = category.key
            row.findViewById<TextView>(R.id.category_sum).text = "¥" + Money.format(category.value)
            row.findViewById<TextView>(R.id.category_share).text = percent.format(share)
            row.findViewById<ProgressBar>(R.id.share_bar).progress = (1000 * share).roundToInt()
            list.addView(row)
        }
    }
}
