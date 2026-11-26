package cn.campus.ledger.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.campus.ledger.R
import cn.campus.ledger.data.Bill
import cn.campus.ledger.data.Money
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

internal class BillListAdapter(
    private val add: () -> Unit,
    private val edit: (Long) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var report: MonthReport? = null

    fun show(updated: MonthReport) {
        report = updated
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = report?.bills?.size?.plus(1) ?: 0

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_HEADER else TYPE_ROW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            Header(inflater.inflate(R.layout.ledger_header, parent, false))
        } else {
            Row(inflater.inflate(R.layout.bill_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = report ?: return
        if (holder is Header) {
            val balance = data.income - data.expense
            holder.count.text = data.bills.size.toString() + " 笔记录"
            holder.income.text = signed(data.income, 1)
            holder.expense.text = signed(data.expense, -1)
            holder.balance.text = signed(balance, if (balance < 0L) -1 else 1)
            holder.empty.visibility = if (data.bills.isEmpty()) View.VISIBLE else View.GONE
            return
        }
        val row = holder as Row
        val bill: Bill = data.bills[position - 1]
        val firstOfDay = position == 1 || bill.day != data.bills[position - 2].day
        row.date.visibility = if (firstOfDay) View.VISIBLE else View.GONE
        if (firstOfDay) row.date.text = LocalDate.parse(bill.day).format(DAY)
        row.category.text = bill.category
        row.note.text = bill.note
        row.note.visibility = if (bill.note.isEmpty()) View.GONE else View.VISIBLE
        row.amount.text = (if (bill.income) "+¥" else "−¥") + Money.format(bill.cents)
        row.editable.setOnClickListener { edit(bill.id) }
    }

    private fun signed(cents: Long, sign: Int): String =
        if (cents == 0L) {
            "¥0.00"
        } else {
            (if (sign < 0) "−¥" else "+¥") + Money.format(abs(cents))
        }

    private inner class Header(root: View) : RecyclerView.ViewHolder(root) {
        val count: TextView = root.findViewById(R.id.count)
        val balance: TextView = root.findViewById(R.id.balance)
        val income: TextView = root.findViewById(R.id.month_income)
        val expense: TextView = root.findViewById(R.id.month_expense)
        val empty: View = root.findViewById(R.id.empty)

        init {
            root.findViewById<View>(R.id.add).setOnClickListener { add() }
        }
    }

    private class Row(root: View) : RecyclerView.ViewHolder(root) {
        val date: TextView = root.findViewById(R.id.date_group)
        val category: TextView = root.findViewById(R.id.bill_category)
        val note: TextView = root.findViewById(R.id.bill_note)
        val amount: TextView = root.findViewById(R.id.bill_amount)
        val editable: View = root.findViewById(R.id.edit_row)
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ROW = 1
        val DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日  EEEE", Locale.CHINA)
    }
}
