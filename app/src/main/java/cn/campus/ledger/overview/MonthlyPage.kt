package cn.campus.ledger.overview

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import cn.campus.ledger.MainActivity
import cn.campus.ledger.data.LedgerDb
import java.time.YearMonth

abstract class MonthlyPage : Fragment() {

    private var requestNumber = 0

    protected fun selectedMonth(): YearMonth =
        (requireActivity() as MainActivity).selectedMonth()

    override fun onResume() {
        super.onResume()
        reload()
    }

    fun reload() {
        val root: View = view ?: return
        val month = selectedMonth()
        val context = requireContext().applicationContext
        val request = ++requestNumber
        root.visibility = View.INVISIBLE
        LedgerDb.run(
            job = {
                val dao = LedgerDb.get(context)
                MonthReport(
                    dao.month(
                        month.atDay(1).toString(),
                        month.plusMonths(1).atDay(1).toString()
                    )
                )
            },
            onSuccess = { report ->
                if (request != requestNumber || root !== view) return@run
                display(report)
                root.visibility = View.VISIBLE
            },
            onFailure = {
                if (request != requestNumber || root !== view) return@run
                root.visibility = View.VISIBLE
                Toast.makeText(context, "账单读取失败，请切换月份重试", Toast.LENGTH_LONG).show()
            }
        )
    }

    protected abstract fun display(report: MonthReport)
}
