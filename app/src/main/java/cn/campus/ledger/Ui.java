package cn.campus.ledger;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
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

    public static Drawable ripple(Context context, String color, int radius) {
        return new RippleDrawable(ColorStateList.valueOf(Color.parseColor("#33246552")),
                background(context, color, radius), background(context, color, radius));
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
