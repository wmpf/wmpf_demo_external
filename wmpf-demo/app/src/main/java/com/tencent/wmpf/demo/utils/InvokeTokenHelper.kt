package com.tencent.wmpf.demo.utils

import android.content.Context
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker

object InvokeTokenHelper {
    private const val TAG = "InvokeTokenHelper"

    fun initInvokeToken(context: Context) {
        val invokeToken = getInvokeToken(context)
        WMPFIPCInvoker.initInvokeToken(invokeToken)
    }

    private fun getInvokeToken(context: Context): String {
        val pref = context.getSharedPreferences(TAG, 0)
        return pref.getString(TAG, "")!!
    }

    fun initInvokeToken(context: Context, invokeToken: String) {
        val pref = context.getSharedPreferences(TAG, 0)
        val editor = pref.edit()
        editor.putString(TAG, invokeToken).apply()
        WMPFIPCInvoker.initInvokeToken(invokeToken)
    }

}

