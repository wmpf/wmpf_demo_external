package com.tencent.wmpf.demo.ui

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

open class ApiActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "WMPF-API"
    }

    protected fun showOk(message: String) {
        Log.i(TAG, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    protected fun showFail(message: String, e: Throwable?) {
        val msg = if (e != null) "$message: ${e.message}" else message
        Log.e(TAG, msg)
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    protected fun showNotSupported(apiName: String) {
        val msg = "当前版本 WMPF 暂不支持 $apiName"
        Log.e(TAG, msg)
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    protected fun invokeWMPFApi(name: String, runnable: Runnable) {
        invokeWMPFApi(name, false, runnable)
    }

    protected fun invokeWMPFApi(name: String, verbose: Boolean, runnable: Runnable) {
        WMPFDemoUtil.execute {
            try {
                runnable.run()
                if (verbose) {
                    showOk("$name 成功")
                }
            } catch (e: WMPFApiException) {
                // WMPF 主动抛出的异常
                showFail("$name 失败", e)
            } catch (e: Exception) {
                showFail("$name 失败", e)
            }
        }
    }

}