package cn.campus.ledger.entry

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import cn.campus.ledger.R
import cn.campus.ledger.Ui
import cn.campus.ledger.data.Bill
import cn.campus.ledger.data.LedgerDao
import cn.campus.ledger.data.LedgerDb
import cn.campus.ledger.data.Money
import java.time.LocalDate

class EntryActivity : Activity() {

    private var bill = Bill()
    private var day: LocalDate = LocalDate.now()
    private lateinit var amount: EditText
    private lateinit var note: EditText
    private lateinit var type: RadioGroup
    private lateinit var category: Spinner
    private lateinit var dateButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var dao: LedgerDao
    private var ready = false
    private var busy = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_entry)
        Ui.insets(findViewById(R.id.editor_scroll))
        dao = LedgerDb.get(this)
        val id = intent.getLongExtra(BILL_ID, 0L)
        amount = findViewById(R.id.amount)
        note = findViewById(R.id.note)
        category = findViewById(R.id.category)
        type = findViewById(R.id.type)
        dateButton = findViewById(R.id.day)
        saveButton = findViewById(R.id.save)
        deleteButton = findViewById(R.id.delete)
        findViewById<TextView>(R.id.heading).text = if (id == 0L) "记一笔" else "编辑账单"
        type.setOnCheckedChangeListener { _, _ -> setCategories("") }
        dateButton.setOnClickListener { chooseDate() }
        saveButton.setOnClickListener { saveBill() }
        deleteButton.setOnClickListener { confirmDelete() }
        findViewById<View>(R.id.cancel).setOnClickListener { finish() }

        when {
            state != null && state.containsKey(FORM_DAY) -> {
                bill.id = id
                day = LocalDate.parse(state.getString(FORM_DAY)!!)
                populate(
                    state.getBoolean(FORM_INCOME),
                    state.getString(FORM_CATEGORY).orEmpty(),
                    state.getString(FORM_AMOUNT).orEmpty(),
                    state.getString(FORM_NOTE).orEmpty()
                )
            }

            id == 0L -> {
                intent.getStringExtra(DEFAULT_DAY)?.let { day = LocalDate.parse(it) }
                populate(false, EXPENSE[0], "", "")
            }

            else -> {
                findViewById<View>(R.id.form).visibility = View.INVISIBLE
                LedgerDb.run(
                    job = { dao.bill(id) },
                    onSuccess = { loaded ->
                        if (isFinishing || isDestroyed) return@run
                        if (loaded == null) {
                            finish()
                            return@run
                        }
                        bill = loaded
                        day = LocalDate.parse(loaded.day)
                        populate(
                            loaded.income,
                            loaded.category,
                            Money.format(loaded.cents),
                            loaded.note
                        )
                    },
                    onFailure = {
                        notifyUser("读取账单失败")
                        finish()
                    }
                )
            }
        }
    }

    private fun populate(income: Boolean, selected: String, value: String, memo: String) {
        type.check(if (income) R.id.income else R.id.expense)
        setCategories(selected)
        amount.setText(value)
        note.setText(memo)
        dateButton.text = day.toString()
        deleteButton.visibility = if (bill.id == 0L) View.GONE else View.VISIBLE
        findViewById<View>(R.id.form).visibility = View.VISIBLE
        ready = true
    }

    private fun setCategories(selected: String) {
        val items = if (type.checkedRadioButtonId == R.id.income) INCOME else EXPENSE
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        category.adapter = adapter
        category.setSelection(maxOf(0, adapter.getPosition(selected)))
    }

    private fun chooseDate() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                day = LocalDate.of(year, month + 1, dayOfMonth)
                dateButton.text = day.toString()
            },
            day.year,
            day.monthValue - 1,
            day.dayOfMonth
        ).show()
    }

    private fun saveBill() {
        if (!ready || busy) return
        val parsed = try {
            Money.parse(amount.text.toString().trim())
        } catch (_: IllegalArgumentException) {
            amount.error = "金额须大于零，最多八位整数和两位小数"
            amount.requestFocus()
            return
        }
        bill.cents = parsed
        bill.income = type.checkedRadioButtonId == R.id.income
        bill.category = category.selectedItem as String
        bill.day = day.toString()
        bill.note = note.text.toString().trim()
        setBusy(true)
        LedgerDb.run(
            job = {
                if (bill.id != 0L) {
                    dao.update(bill) == 1
                } else {
                    dao.add(bill)
                    true
                }
            },
            onSuccess = { updated ->
                if (!updated) notifyUser("这笔账单已被删除")
                finish()
            },
            onFailure = { writeFailed() }
        )
    }

    private fun confirmDelete() {
        if (busy || !ready) return
        AlertDialog.Builder(this)
            .setTitle("删除这笔账单？")
            .setMessage(bill.category + "  ¥" + Money.format(bill.cents))
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ ->
                setBusy(true)
                LedgerDb.run(
                    job = { dao.remove(bill) },
                    onSuccess = { _ -> finish() },
                    onFailure = { writeFailed() }
                )
            }
            .show()
    }

    private fun setBusy(value: Boolean) {
        busy = value
        saveButton.isEnabled = !value
        deleteButton.isEnabled = !value
    }

    private fun writeFailed() {
        if (isFinishing || isDestroyed) return
        setBusy(false)
        notifyUser("保存更改失败，请重试")
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        if (!ready) return
        out.putBoolean(FORM_INCOME, type.checkedRadioButtonId == R.id.income)
        out.putString(FORM_AMOUNT, amount.text.toString())
        out.putString(FORM_CATEGORY, category.selectedItem as String)
        out.putString(FORM_DAY, day.toString())
        out.putString(FORM_NOTE, note.text.toString())
    }

    companion object {
        const val BILL_ID = "bill_id"
        const val DEFAULT_DAY = "default_day"

        private const val FORM_INCOME = "form_income"
        private const val FORM_AMOUNT = "form_amount"
        private const val FORM_CATEGORY = "form_category"
        private const val FORM_DAY = "form_day"
        private const val FORM_NOTE = "form_note"

        private val EXPENSE = arrayOf("餐饮", "交通", "学习", "日用", "其他")
        private val INCOME = arrayOf("生活费", "兼职", "其他")
    }
}
