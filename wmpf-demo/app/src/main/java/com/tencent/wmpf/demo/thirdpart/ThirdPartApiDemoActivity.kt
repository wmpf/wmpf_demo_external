package com.tencent.wmpf.demo.thirdpart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

class ThirdPartApiDemoActivity : AppCompatActivity() {
    private var mDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action.equals(ThirdpartConstants.Printer.Action.ACTION_THIRDPART)) {
            val appId = "replace with your invoke appId here"
            val timeStamp = intent.extras?.getLong(ThirdpartConstants.Printer.Key.KEY_TIME_STAMP)
            val token = intent.extras?.getString(ThirdpartConstants.Printer.Key.KEY_TOKEN)
            val tokenLocalGen = WMPFDemoUtil.getMD5String(appId + "_" + timeStamp)
            if (tokenLocalGen != token) {
                Log.e(TAG, "token invalid")
                Toast.makeText(applicationContext, "printer: token invalid!", Toast.LENGTH_LONG)
                    .show()
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
            // rawData为前端透传过来的自定义数据结构字符串，对应前端data的数据
            val rawData = intent.extras?.getString(ThirdpartConstants.Printer.Key.KEY_RAW_DATA)

            mDialog = AlertDialog.Builder(this).apply {
                setTitle(rawData)
                setOnCancelListener {
                    val intent = Intent().apply {
                        // 将执行结果通知到wmpf，否则前端接受到的回调参数会错误
                        putExtra(
                            ThirdpartConstants.Printer.Key.KEY_RESULT_CODE,
                            ThirdpartConstants.Printer.Code.CODE_SUCCESS
                        )
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }.create()
            mDialog?.show()
        }
    }

    companion object {
        private const val TAG = "ThirdPartApiDemo"
    }

    override fun onDestroy() {
        super.onDestroy()
        mDialog?.apply {
            dismiss()
        }
    }
}