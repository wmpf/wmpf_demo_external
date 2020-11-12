package com.tencent.wmpf.demo.utils

import android.content.Context
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker
import java.lang.Exception

object InvokeTokenHelper {
    private const val TAG = "InvokeTokenHelper"
    private var context: Context? = null

    fun initInvokeToken(context: Context) {
        this.context = context
        val invokeToken = getInvokeToken()
        WMPFIPCInvoker.initInvokeToken(invokeToken)
    }

    private fun getInvokeToken(): String {
        if (context == null) {
            throw Exception("need invoke initInvokeToken.")
        }
        val pref = context!!.getSharedPreferences(TAG, 0)
        return pref?.getString(TAG, "")!!
    }

    fun initInvokeToken(invokeToken: String) {
        if (context == null) {
            throw Exception("need invoke initInvokeToken.")
        }
        val pref = context!!.getSharedPreferences(TAG, 0)
        val editor = pref?.edit()
        editor?.putString(TAG, invokeToken)?.apply()
        WMPFIPCInvoker.initInvokeToken(invokeToken)
    }

}

