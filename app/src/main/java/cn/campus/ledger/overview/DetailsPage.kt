package cn.campus.ledger.overview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.campus.ledger.Ui
import cn.campus.ledger.entry.EntryActivity
import java.time.LocalDate
import java.time.YearMonth

class DetailsPage : MonthlyPage() {

    private var adapter: BillListAdapter? = null
    private var list: RecyclerView? = null
    private var displayedMonth: YearMonth? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        state: Bundle?
    ): View {
        val spacing = Ui.dp(requireContext(), 16)
        val view = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            setPadding(spacing, 0, spacing, spacing)
            clipToPadding = false
        }
        val created = BillListAdapter(
            add = { newEntry() },
            edit = { id ->
                startActivity(
                    Intent(requireContext(), EntryActivity::class.java)
                        .putExtra(EntryActivity.BILL_ID, id)
                )
            }
        )
        view.adapter = created
        list = view
        adapter = created
        return view
    }

    private fun newEntry() {
        val month = selectedMonth()
        val today = LocalDate.now()
        val date = if (month == YearMonth.from(today)) today else month.atDay(1)
        startActivity(
            Intent(requireContext(), EntryActivity::class.java)
                .putExtra(EntryActivity.DEFAULT_DAY, date.toString())
        )
    }

    override fun display(report: MonthReport) {
        adapter?.show(report)
        if (selectedMonth() != displayedMonth) list?.scrollToPosition(0)
        displayedMonth = selectedMonth()
    }

    override fun onDestroyView() {
        list?.adapter = null
        list = null
        adapter = null
        super.onDestroyView()
    }
}
