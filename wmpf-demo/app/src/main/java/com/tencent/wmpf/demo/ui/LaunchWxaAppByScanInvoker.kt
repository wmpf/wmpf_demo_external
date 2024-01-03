package com.tencent.wmpf.demo.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
        } else {
            val intent = Intent(this, CaptureActivity::class.java)
            launcher.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, CaptureActivity::class.java)
            launcher.launch(intent)
        }
    }

    private var launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            val bundle = it.data?.extras
            if (bundle != null) {
                val retCode = bundle.getInt(CodeUtils.RESULT_TYPE)
                Log.i(TAG, "retCode: %d", retCode)
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

    companion object {
        private const val TAG = "WMPF.ScanProxyUI"
        fun launchWxaByScanUI(context: Context) {
            val intent = Intent(context, LaunchWxaAppByScanInvoker::class.java)
            context.startActivity(intent)
        }
    }
}