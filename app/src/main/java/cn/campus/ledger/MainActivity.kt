package cn.campus.ledger

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.campus.ledger.overview.BudgetSlot
import cn.campus.ledger.overview.DetailsPage
import cn.campus.ledger.overview.MonthlyPage
import cn.campus.ledger.overview.StatsPage
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : FragmentActivity() {

    private var month: YearMonth = YearMonth.now()
    private var tab: Int = 0

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        month = state?.getString("month")?.let { YearMonth.parse(it) } ?: YearMonth.now()
        tab = state?.getInt("tab") ?: 0
        setContentView(R.layout.activity_main)
        Ui.insets(findViewById(R.id.root))
        findViewById<View>(R.id.previous).setOnClickListener { moveMonth(-1) }
        findViewById<View>(R.id.next).setOnClickListener { moveMonth(1) }
        TABS.forEachIndexed { index, id ->
            findViewById<View>(id).setOnClickListener { openTab(index) }
        }
        refreshHeader()
        if (supportFragmentManager.findFragmentById(R.id.content) == null) showPage()
    }

    fun selectedMonth(): YearMonth = month

    private fun moveMonth(step: Int) {
        month = month.plusMonths(step.toLong())
        refreshHeader()
        val page = supportFragmentManager.findFragmentById(R.id.content)
        (page as? MonthlyPage)?.reload()
    }

    private fun openTab(destination: Int) {
        if (tab == destination) return
        tab = destination
        refreshHeader()
        showPage()
    }

    private fun showPage() {
        val page: Fragment = when (tab) {
            1 -> StatsPage()
            2 -> BudgetSlot()
            else -> DetailsPage()
        }
        supportFragmentManager.beginTransaction().replace(R.id.content, page).commit()
    }

    private fun refreshHeader() {
        findViewById<TextView>(R.id.month).text = month.format(MONTH_LABEL)
        findViewById<TextView>(R.id.page_title).text = TITLES[tab]
        TABS.forEachIndexed { index, id ->
            val nav = findViewById<TextView>(id)
            nav.isSelected = index == tab
            nav.setBackgroundResource(if (index == tab) R.drawable.active_tab else android.R.color.transparent)
        }
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putString("month", month.toString())
        out.putInt("tab", tab)
    }

    companion object {
        private val MONTH_LABEL: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
        private val TABS = intArrayOf(R.id.nav_ledger, R.id.nav_stats, R.id.nav_budget)
        private val TITLES = arrayOf("校园账本", "分类统计", "生活费预算")
    }
}
