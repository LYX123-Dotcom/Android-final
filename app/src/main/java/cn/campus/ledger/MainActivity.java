package cn.campus.ledger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;
import java.util.List;
import cn.campus.ledger.data.*;
import cn.campus.ledger.entry.EntryActivity;

public class MainActivity extends Activity {
    private final List<Bill> records = new ArrayList<>();
    private BaseAdapter adapter;
    private TextView count;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        Ui.insets(findViewById(R.id.root));
        count = findViewById(R.id.count);
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
        LedgerDb.run(() -> LedgerDb.get(this).all(), rows -> {
            if (isFinishing() || isDestroyed()) return;
            records.clear();
            records.addAll(rows);
            adapter.notifyDataSetChanged();
            count.setText("全部记录 · " + records.size() + " 笔");
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
