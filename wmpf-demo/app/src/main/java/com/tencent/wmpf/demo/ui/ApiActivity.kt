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

    protected fun showFail(apiName: String, e: Throwable) {
        val message = "$apiName fail: ${e.message}"
        Log.e(TAG, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
        WMPFDemoUtil.execute {
            try {
                runnable.run()
            } catch (e: WMPFApiException) {
                // WMPF 主动抛出的异常
                showFail(name, e)
            } catch (e: Exception) {
                showFail(name, e)
            }
        }
    }

}