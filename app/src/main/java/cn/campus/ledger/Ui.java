package cn.campus.ledger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

public final class Ui {
    private Ui() {}

    public static int dp(Context context, int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }

    public static GradientDrawable background(Context context, String color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(color));
        shape.setCornerRadius(dp(context, radius));
        return shape;
    }

    public static TextView text(Context context, int size, String color, boolean bold) {
        TextView view = new TextView(context);
        view.setTextColor(Color.parseColor(color));
        view.setTextSize(size);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    public static void insets(View root) {
        root.setOnApplyWindowInsetsListener((v, window) -> {
            android.graphics.Insets edges = window.getInsets(WindowInsets.Type.systemBars()
                    | WindowInsets.Type.ime() | WindowInsets.Type.displayCutout());
            v.setPadding(edges.left, edges.top, edges.right, edges.bottom);
            return window;
        });
    }
}
