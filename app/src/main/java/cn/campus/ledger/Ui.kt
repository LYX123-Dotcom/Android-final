package cn.campus.ledger

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import kotlin.math.roundToInt

object Ui {

    fun dp(context: Context, value: Int): Int =
        (context.resources.displayMetrics.density * value).roundToInt()

    fun background(context: Context, color: String, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = dp(context, radius).toFloat()
        }

    fun text(context: Context, size: Int, color: String, bold: Boolean): TextView =
        TextView(context).apply {
            setTextColor(Color.parseColor(color))
            textSize = size.toFloat()
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    fun insets(root: View) {
        root.setOnApplyWindowInsetsListener { view, window ->
            val edges = window.getInsets(
                WindowInsets.Type.systemBars() or
                    WindowInsets.Type.ime() or
                    WindowInsets.Type.displayCutout()
            )
            view.setPadding(edges.left, edges.top, edges.right, edges.bottom)
            window
        }
    }
}

