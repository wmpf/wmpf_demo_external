package com.tencent.wmpf.demo.utils

import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.lang.Error

class WMPFDemoLogger(
    private val tag: String,
    private val ctx: Context,
    private val v: TextView?,
) {
    var text = ""

    private fun runOnUIThread(runnable: Runnable) {
        if (v != null && Looper.getMainLooper() !== Looper.myLooper()) {
            v.post(runnable)
        } else {
            runnable.run()
        }
    }


    private fun log(text: String) {
        if (v != null) {
            this.text += text + "\n"
            v.text = this.text
        }
    }

    fun i(v: String) {
        Log.i(tag, v)
        runOnUIThread {
            this.log(v)
        }
    }

    fun e(text: String, error: Exception? = null) {
        var msg = text
        if (error != null) {
            Log.e(tag, "$text: $error")
            msg = "$text: ${error.message}"
        } else {
            Log.e(tag, text)
        }

        runOnUIThread {
            Toast.makeText(this.ctx, msg, Toast.LENGTH_SHORT).show()
            this.log("[E] $msg")
        }
    }


    fun clear() {
        if (v == null) return
        runOnUIThread {
            v.text = ""
            this.text = ""
        }
    }
}
