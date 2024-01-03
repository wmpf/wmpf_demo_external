package com.tencent.wmpf.demo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tencent.mm.ipcinvoker.tools.Log
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.demo.utils.WMPFDemoUtil.execute
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.uuzuche.lib_zxing.activity.ZXingLibrary

class LaunchWxaAppByScanInvoker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ZXingLibrary.initDisplayOpinion(this)
        doScanImpl()
    }

    private fun doScanImpl() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
        val intent = Intent(this, CaptureActivity::class.java)
        startActivityForResult(intent, REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            if (data?.extras != null) {
                val bundle = data.extras
                val retCode = bundle?.getInt(CodeUtils.RESULT_TYPE)
                Log.i(TAG, "retCode:%d", retCode)
                when (retCode) {
                    CodeUtils.RESULT_SUCCESS -> {
                        val rawData = bundle.getString(CodeUtils.RESULT_STRING)
                        Log.i(TAG, "rawData:%s", rawData)
                        execute {
                            try {
                                WMPF.getInstance().miniProgramApi.launchByQRScanCode(rawData)
                            } catch (e: WMPFApiException) {
                                Log.e(TAG, "launchByQRScanCode fail: $e")
                            }
                        }
                        finish()
                    }

                    CodeUtils.RESULT_FAILED -> {
                        Log.e(TAG, "scan fail, return")
                        finish()
                    }
                }
            } else {
                Log.e(TAG, "data is null, return")
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "WMPF.ScanProxyUI"
        fun launchWxaByScanUI(context: Context) {
            val intent = Intent(context, LaunchWxaAppByScanInvoker::class.java)
            context.startActivity(intent)
        }

        private const val REQ_CODE = 100
    }
}