package com.source.wally.utils

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.source.wally.R
import kotlinx.android.synthetic.main.dialog_app_use.view.*

object AlertDialogUtils {

    fun showDialogForAppUse(activity: Activity?) {
        if (activity != null) {
            val inflater = activity.layoutInflater
            val alertDialogBuilder = AlertDialog.Builder(activity)
            val view = inflater.inflate(R.layout.dialog_app_use, null)
            alertDialogBuilder.setView(view)
            alertDialogBuilder.setCancelable(true)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.setCancelable(true)
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            view.clDismiss.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }
}