@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection")

package com.tencent.luggage.demo.wxapi

import android.util.Log
import com.tencent.mmkv.MMKV
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.RequestsRepo
import com.tencent.wmpf.demo.RequestsRepo.KEY_TEST_PRODUCT_ID

object DeviceInfo {
    /**
     * NOTE:
     * WARNING: You should never have your app secret stored in client.
     * The following code is a MISTAKE.
     * temp here!
     */
    const val APP_ID = BuildConfig.HOST_APPID // com.tencent.luggage.demo
    const val APP_SECRET = BuildConfig.HOST_APPSECRET

    private const val TAG = "Constants"
    private val kv = MMKV.mmkvWithID(RequestsRepo.TAG, MMKV.SINGLE_PROCESS_MODE)
    private val expiredTimeMs = kv.getLong(RequestsRepo.KEY_EXPIRED_TIME_MS, -1)
    private const val isInProductionEnv = false

    val PRODUCT_ID: Int
        get() {
            if (isExpired() || isInProductionEnv) {
                return 0 // REPLACE YOUR OWN DEVICE INFO
            } else {
                return kv.getInt(KEY_TEST_PRODUCT_ID, 0)
            }
        }

    val KEY_VERSION: Int
        get() {
            if (isExpired() || isInProductionEnv) {
                return 0 // REPLACE YOUR OWN DEVICE INFO
            } else {
                return kv.getInt(RequestsRepo.KEY_TEST_KEY_VERSION, 0)
            }
        }

    val DEVICE_ID: String
        get() {
            if (isExpired() || isInProductionEnv) {
                return "" // REPLACE YOUR OWN DEVICE INFO
            } else {
                return kv.getString(RequestsRepo.KEY_TEST_DEVICE_ID, "")!!
            }
        }

    val SIGNATURE: String
        get() {
            if (isExpired() || isInProductionEnv) {
                return "" // REPLACE YOUR OWN DEVICE INFO
            } else {
                return kv.getString(RequestsRepo.KEY_TEST_SIGNATURE, "")!!
            }
        }

     fun isExpired(): Boolean {
        val ret = expiredTimeMs > System.currentTimeMillis()
        if (ret) Log.e(TAG, "isExpired: deviceInfo is isExpired or not init")
        return ret
    }
}