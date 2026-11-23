package cn.campus.ledger.entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.time.LocalDate;
import cn.campus.ledger.R;
import cn.campus.ledger.Ui;
import cn.campus.ledger.data.*;

public class EntryActivity extends Activity {
    public static final String BILL_ID = "bill_id";
    private static final String[] EXPENSE = {"餐饮", "交通", "学习", "日用", "其他"};
    private static final String[] INCOME = {"生活费", "兼职", "其他"};
    private Bill bill = new Bill();
    private LocalDate day = LocalDate.now();
    private EditText amount, note;
    private RadioGroup type;
    private Spinner category;
    private Button dateButton, save, delete;
    private LedgerDao dao;
    private boolean ready, busy;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_entry);
        Ui.insets(findViewById(R.id.editor_scroll));
        dao = LedgerDb.get(this);
        long id = getIntent().getLongExtra(BILL_ID, 0);
        amount = findViewById(R.id.amount);
        note = findViewById(R.id.note);
        category = findViewById(R.id.category);
        type = findViewById(R.id.type);
        dateButton = findViewById(R.id.day);
        save = findViewById(R.id.save);
        delete = findViewById(R.id.delete);
        ((TextView)findViewById(R.id.heading)).setText(id == 0 ? "记一笔" : "编辑账单");
        type.setOnCheckedChangeListener((group, checked) -> setCategories(""));
        dateButton.setOnClickListener(v -> chooseDate());
        save.setOnClickListener(v -> save());
        delete.setOnClickListener(v -> confirmDelete());
        findViewById(R.id.cancel).setOnClickListener(v -> finish());

        if (state != null && state.containsKey("form_day")) {
            bill.id = id;
            day = LocalDate.parse(state.getString("form_day"));
            populate(state.getBoolean("form_income"), state.getString("form_category"),
                    state.getString("form_amount"), state.getString("form_note"));
        } else if (id == 0) {
            populate(false, EXPENSE[0], "", "");
        } else {
            findViewById(R.id.form).setVisibility(View.INVISIBLE);
            LedgerDb.run(() -> dao.bill(id), loaded -> {
                if (isFinishing() || isDestroyed()) return;
                if (loaded == null) { finish(); return; }
                bill = loaded;
                day = LocalDate.parse(bill.day);
                populate(bill.income, bill.category, Money.format(bill.cents), bill.note);
            }, () -> { notifyUser("读取账单失败"); finish(); });
        }
    }

    private void populate(boolean income, String selected, String value, String memo) {
        type.check(income ? R.id.income : R.id.expense);
        setCategories(selected);
        amount.setText(value);
        note.setText(memo);
        dateButton.setText(day.toString());
        delete.setVisibility(bill.id == 0 ? View.GONE : View.VISIBLE);
        findViewById(R.id.form).setVisibility(View.VISIBLE);
        ready = true;
    }

    private void setCategories(String selected) {
        String[] items = type.getCheckedRadioButtonId() == R.id.income ? INCOME : EXPENSE;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        category.setAdapter(adapter);
        category.setSelection(Math.max(0, adapter.getPosition(selected)));
    }

    private void chooseDate() {
        new DatePickerDialog(this, (picker, year, month, dayOfMonth) -> {
            day = LocalDate.of(year, month + 1, dayOfMonth);
            dateButton.setText(day.toString());
        }, day.getYear(), day.getMonthValue() - 1, day.getDayOfMonth()).show();
    }

    private void save() {
        if (!ready || busy) return;
        try {
            bill.cents = Money.parse(amount.getText().toString().trim());
        } catch (IllegalArgumentException e) {
            amount.setError("金额须大于零，最多八位整数和两位小数");
            amount.requestFocus();
            return;
        }
        bill.income = type.getCheckedRadioButtonId() == R.id.income;
        bill.category = (String) category.getSelectedItem();
        bill.day = day.toString();
        bill.note = note.getText().toString().trim();
        setBusy(true);
        LedgerDb.run(() -> {
            if (bill.id != 0) return dao.update(bill) == 1;
            dao.add(bill);
            return true;
        }, updated -> {
            if (!updated) notifyUser("这笔账单已被删除");
            finish();
        }, this::writeFailed);
    }

    private void confirmDelete() {
        if (busy || !ready) return;
        new AlertDialog.Builder(this).setTitle("删除这笔账单？")
                .setMessage(bill.category + "  ¥" + Money.format(bill.cents))
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    setBusy(true);
                    LedgerDb.run(() -> dao.remove(bill), count -> finish(), this::writeFailed);
                }).show();
    }

    private void setBusy(boolean value) {
        busy = value;
        save.setEnabled(!value);
        delete.setEnabled(!value);
    }

    private void writeFailed() {
        if (isFinishing() || isDestroyed()) return;
        setBusy(false);
        notifyUser("保存更改失败，请重试");
    }

    private void notifyUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (!ready) return;
        out.putBoolean("form_income", type.getCheckedRadioButtonId() == R.id.income);
        out.putString("form_amount", amount.getText().toString());
        out.putString("form_category", (String) category.getSelectedItem());
        out.putString("form_day", day.toString());
        out.putString("form_note", note.getText().toString());
    }
}
