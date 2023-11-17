package com.tencent.wmpf.demo.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.tencent.wmpf.app.WMPFInfo
import com.tencent.wmpf.cli.api.WMPFClientDefaultExecutor
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object WMPFDemoUtil {
    private const val TAG = "WMPF.DemoUtil"
    private val executor = WMPFClientDefaultExecutor()
    private val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun execute(runnable: Runnable) {
        executor.submit {
            try {
                runnable.run()
            } catch (throwable: Throwable) {
                Log.wtf(
                        TAG,
                        "failed: " + throwable.message,
                        throwable
                )
                throw IllegalStateException(throwable)
            }
            null
        }
    }

    @Throws(NameNotFoundException::class)
    fun getWmpfVersionCode(app: Application): Int {
        val info = app.packageManager.getPackageInfo(WMPFInfo.WMPF_APP_PACKAGE_NAME, 0)
        return info.versionCode
    }

    @Throws(NameNotFoundException::class)
    fun getWmpfVersion(app: Application): String? {
        val info = app.packageManager.getApplicationInfo(
                WMPFInfo.WMPF_APP_PACKAGE_NAME,
                PackageManager.GET_META_DATA
        )
        return info.metaData.getString(WMPFInfo.WMPF_APP_PACKAGE_NAME + ".BuildInfo.BUILD_WMPF_VERSION_NAME")
    }

    fun runOnUiThread(runnable: Runnable) {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ret0 = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            val ret1 = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val ret2 = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            val ret3 = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            val ret4 = context.checkSelfPermission(Manifest.permission.CAMERA)
            val ret5 = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
            return ret0 == PackageManager.PERMISSION_GRANTED &&
                    ret1 == PackageManager.PERMISSION_GRANTED &&
                    ret2 == PackageManager.PERMISSION_GRANTED &&
                    ret3 == PackageManager.PERMISSION_GRANTED &&
                    ret4 == PackageManager.PERMISSION_GRANTED &&
                    ret5 == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun requestPermission(context: Activity) {
        try {
            ActivityCompat.requestPermissions(
                    context, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE
            ), 0)
        } catch (e: Exception) {

        }
    }

    fun getMD5String(text: String): String {
        try {
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            val digest: ByteArray = instance.digest(text.toByteArray())
            val sb = StringBuilder()
            for (b in digest) {
                val i: Int = b.toInt() and 0xff
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}