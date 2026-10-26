package fr.pointage.nfc

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Replaces the screen with a scrolling column and returns the column. Big targets: used in the van. */
fun Activity.column(): LinearLayout {
    val column = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        val pad = (16 * resources.displayMetrics.density).toInt()
        setPadding(pad, pad, pad, pad)
    }
    setContentView(ScrollView(this).apply { addView(column) })
    return column
}

fun LinearLayout.text(value: String, size: Float = 18f) =
    TextView(context).apply { text = value; textSize = size; setPadding(0, 16, 0, 16) }.also(::addView)

fun LinearLayout.button(label: String, onClick: (View) -> Unit) =
    Button(context).apply { text = label; textSize = 22f; isAllCaps = false; setOnClickListener(onClick) }
        .also(::addView)
