package cn.campus.ledger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import cn.campus.ledger.data.*;
import cn.campus.ledger.entry.EntryActivity;

public class MainActivity extends Activity {
    private final List<Bill> records = new ArrayList<>();
    private BaseAdapter adapter;
    private TextView count, monthLabel, incomeTotal, expenseTotal, balanceTotal;
    private LedgerDao dao;
    private LocalDate month = LocalDate.now().withDayOfMonth(1);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        Ui.insets(findViewById(R.id.root));
        dao = LedgerDb.get(this);
        count = findViewById(R.id.count);
        monthLabel = findViewById(R.id.month);
        incomeTotal = findViewById(R.id.income_total);
        expenseTotal = findViewById(R.id.expense_total);
        balanceTotal = findViewById(R.id.balance_total);
        findViewById(R.id.summary).setBackground(Ui.background(this, "#FFFFFF", 16));
        findViewById(R.id.prev_month).setOnClickListener(v -> switchMonth(-1));
        findViewById(R.id.next_month).setOnClickListener(v -> switchMonth(1));
        ListView list = findViewById(R.id.list);
        list.setEmptyView(findViewById(R.id.empty));
        adapter = new BillAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) ->
                startActivity(new Intent(this, EntryActivity.class).putExtra(EntryActivity.BILL_ID, id)));
        findViewById(R.id.add).setOnClickListener(v -> startActivity(new Intent(this, EntryActivity.class)));
    }

    @Override public void onResume() {
        super.onResume();
        load();
    }

    private void switchMonth(int delta) {
        month = month.plusMonths(delta);
        load();
    }

    private void load() {
        final String start = month.toString();
        final String end = month.plusMonths(1).toString();
        monthLabel.setText(month.getYear() + "年" + month.getMonthValue() + "月");
        LedgerDb.run(() -> dao.month(start, end), rows -> {
            if (isFinishing() || isDestroyed()) return;
            long income = 0, expense = 0;
            for (Bill bill : rows) {
                if (bill.income) income += bill.cents;
                else expense += bill.cents;
            }
            long balance = income - expense;
            incomeTotal.setText("+¥" + Money.format(income));
            expenseTotal.setText("−¥" + Money.format(expense));
            balanceTotal.setText((balance < 0 ? "−¥" : "¥") + Money.format(Math.abs(balance)));
            records.clear();
            records.addAll(rows);
            adapter.notifyDataSetChanged();
            count.setText(rows.size() + " 笔记录");
        }, () -> Toast.makeText(this, "读取失败，请重新打开账本", Toast.LENGTH_SHORT).show());
    }

    private class BillAdapter extends BaseAdapter {
        public int getCount() { return records.size(); }
        public Bill getItem(int position) { return records.get(position); }
        public long getItemId(int position) { return getItem(position).id; }
        @Override public boolean hasStableIds() { return true; }

        public View getView(int position, View recycled, ViewGroup parent) {
            BillRow row = recycled == null ? new BillRow() : (BillRow) recycled;
            Bill bill = getItem(position);
            row.category.setText(bill.category);
            row.details.setText(bill.note.isEmpty() ? bill.day : bill.day + "  ·  " + bill.note);
            row.amount.setText((bill.income ? "+¥" : "−¥") + Money.format(bill.cents));
            return row;
        }
    }

    private class BillRow extends LinearLayout {
        final TextView category, details, amount;
        BillRow() {
            super(MainActivity.this);
            setGravity(Gravity.CENTER_VERTICAL);
            int pad = Ui.dp(getContext(), 14);
            setPadding(pad, pad, pad, pad);
            setBackground(Ui.background(getContext(), "#FFFFFF", 14));
            LinearLayout stack = new LinearLayout(getContext());
            stack.setOrientation(VERTICAL);
            category = Ui.text(getContext(), 17, "#20382B", true);
            details = Ui.text(getContext(), 13, "#52655B", false);
            details.setSingleLine(true);
            details.setEllipsize(TextUtils.TruncateAt.END);
            stack.addView(category);
            stack.addView(details);
            addView(stack, new LayoutParams(0, -2, 1));
            amount = Ui.text(getContext(), 19, "#205943", true);
            addView(amount);
        }
    }
}
