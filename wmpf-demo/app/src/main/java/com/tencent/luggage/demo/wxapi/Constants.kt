@file:Suppress("SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection", "SpellCheckingInspection")

package com.tencent.luggage.demo.wxapi

import com.tencent.wmpf.demo.BuildConfig

object Constants {
    /**
     * NOTE:
     * WARNING: You should never have your app secret stored in client.
     * The following code is a MISTAKE.
     */
    const val APP_ID = BuildConfig.HOST_APPID // com.tencent.luggage.demo
    const val APP_SECRET = BuildConfig.HOST_APPSECRET

    /**
     * REPLACE YOUR OWN DEVICE INFO
     */
    const val PRODUCT_ID = 0
    const val KEY_VERSION = 0
    const val DEVICE_ID = "0"
    const val SIGNATURE = "0"
}

