package com.mary.onandoff.util

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

object ToastUtil {

    fun showShortToast(context: Context?, string: String?) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    fun showCustomShortToastNormal(context: Context?, string: String?) {
        val toast = Toast(context)
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER
        linearLayout.setPadding(15, 15, 15, 15)
        val textView = TextView(context)
        textView.text = string
        textView.setPadding(15, 15, 15, 15)
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5.58f)
        textView.setTextColor(Color.WHITE)
        linearLayout.addView(textView)
        toast.setView(linearLayout)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }

    fun showCustomShortToastNormal(context: Context?, string: String?, dp : Int) {
        val toast = Toast(context)
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER
        linearLayout.setPadding(15, 15, 15, 15)
        val textView = TextView(context)
        textView.text = string
        textView.setPadding(15, 15, 15, 15)
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5.58f)
        textView.setTextColor(Color.WHITE)
        linearLayout.addView(textView)
        toast.setView(linearLayout)
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM, 0, dp)
        toast.show()
    }

    fun showCustomLongToastNormal(context: Context?, string: String?) {
        val toast = Toast(context)
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.gravity = Gravity.CENTER
        linearLayout.setPadding(15, 15, 15, 15)
        val textView = TextView(context)
        textView.text = string
        textView.setPadding(15, 15, 15, 15)
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5.58f)
        textView.setTextColor(Color.WHITE)
        linearLayout.addView(textView)
        toast.setView(linearLayout)
        toast.duration = Toast.LENGTH_LONG
        toast.show()
    }
}