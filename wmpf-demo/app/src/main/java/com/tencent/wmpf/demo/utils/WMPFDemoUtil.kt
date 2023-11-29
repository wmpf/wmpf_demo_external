package com.tencent.wmpf.demo.utils

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.tencent.wmpf.app.WMPFInfo
import com.tencent.wmpf.cli.api.WMPFClientDefaultExecutor
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object WMPFDemoUtil {
    private const val ERR_NOT_INSTALLED = "WMPF Service APK 未安装。"
    private const val ERR_LOW_VERSION = "WMPF 版本过低，请升级到 2.1 及以上版本。当前版本为 "
    private const val TAG = "WMPF.DemoUtil"
    private val executor = WMPFClientDefaultExecutor()

    fun execute(runnable: Runnable) {
        executor.submit {
            try {
                runnable.run()
            } catch (throwable: Throwable) {
                Log.wtf(
                    TAG, "failed: " + throwable.message, throwable
                )
                throw IllegalStateException(throwable)
            }
            null
        }
    }

    @Suppress("DEPRECATION")
    @Throws(NameNotFoundException::class)
    fun getWmpfVersionCode(app: Application): Int {
        val info = app.packageManager.getPackageInfo(WMPFInfo.WMPF_APP_PACKAGE_NAME, 0)
        return info.versionCode
    }

    @Throws(NameNotFoundException::class)
    fun getWmpfVersion(app: Application): String? {
        val info = app.packageManager.getApplicationInfo(
            WMPFInfo.WMPF_APP_PACKAGE_NAME, PackageManager.GET_META_DATA
        )
        return info.metaData.getString(WMPFInfo.WMPF_APP_PACKAGE_NAME + ".BuildInfo.BUILD_WMPF_VERSION_NAME")
    }

    fun isLessThanWMPF22(app: Application): Boolean {
        /**
         * WMPF Version 请参考下载的 apk 中
         * 例如：wmpf-arm-alpha-release-v2.1.0-9010017-signed.apk versionCode 为 9010017
         */
        return getWmpfVersionCode(app) < 9020001
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

    // 示例如何获取和判断 WMPF 版本
    fun checkWMPFVersion(activity: Activity) {
        // 本 demo 需要 WMPF 2.1 及以上版本方可使用。低版本请使用 apiv1 分支
        try {
            val versionCode = getWmpfVersionCode(activity.application)
            val version = getWmpfVersion(activity.application)
            Log.i(TAG, "WMPF Service APK Version: $version($versionCode)")
            if (versionCode < 9010001) { // 9010001 为 2.1.0
                Log.e(TAG, ERR_LOW_VERSION + version)
                AlertDialog.Builder(activity).setTitle(ERR_LOW_VERSION + version)
                    .setCancelable(false).setNegativeButton("退出") { _, _ ->
                        activity.finish()
                    }.show()
            }
        } catch (e: NameNotFoundException) {
            Log.e(TAG, ERR_NOT_INSTALLED)
            AlertDialog.Builder(activity).setTitle(ERR_NOT_INSTALLED).setCancelable(false)
                .setNegativeButton("退出") { _, _ ->
                    activity.finish()
                }.show()
        }
    }
}