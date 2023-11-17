package com.tencent.wmpf.demo.utils

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tencent.wmpf.app.WMPFInfo
import com.tencent.wmpf.cli.api.WMPFClientDefaultExecutor

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
}