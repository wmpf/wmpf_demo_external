@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection")

package com.tencent.luggage.demo.wxapi

import android.util.Log
import com.tencent.mmkv.MMKV
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.RequestsRepo
import com.tencent.wmpf.demo.RequestsRepo.KEY_TEST_PRODUCT_ID

object Constants {
    /**
     * NOTE:
     * WARNING: You should never have your app secret stored in client.
     * The following code is a MISTAKE.
     */
    const val APP_ID = BuildConfig.HOST_APPID // com.tencent.luggage.demo
    const val APP_SECRET = BuildConfig.HOST_APPSECRET

    private const val TAG = "Constants"
    private val kv = MMKV.mmkvWithID(RequestsRepo.TAG, MMKV.SINGLE_PROCESS_MODE)
    private val expiredTimeMs = kv.getLong(RequestsRepo.KEY_EXPIRED_TIME_MS, -1)
    private const val isInProductionEnv = false

    val PRODUCT_ID = if (isExpired() || isInProductionEnv) {
        0 // REPLACE YOUR OWN DEVICE INFO
    } else {
        kv.getInt(KEY_TEST_PRODUCT_ID, 0)
    }
    val KEY_VERSION = if (isExpired() || isInProductionEnv) {
        0 // REPLACE YOUR OWN DEVICE INFO
    } else {
        kv.getInt(RequestsRepo.KEY_TEST_KEY_VERSION, 0)
    }

    val DEVICE_ID = if (isExpired() || isInProductionEnv) {
        "" // REPLACE YOUR OWN DEVICE INFO
    } else {
        kv.getString(RequestsRepo.KEY_TEST_DEVICE_ID, "")!!
    }
    val SIGNATURE = if (isExpired() || isInProductionEnv) {
        "" // REPLACE YOUR OWN DEVICE INFO
    } else {
        kv.getString(RequestsRepo.KEY_TEST_SIGNATURE, "")!!
    }

    private fun isExpired(): Boolean {
        val ret = expiredTimeMs > System.currentTimeMillis()
        if (ret) Log.e(TAG, "isExpired: deviceInfo is isExpired or not init")
        return ret
    }
}