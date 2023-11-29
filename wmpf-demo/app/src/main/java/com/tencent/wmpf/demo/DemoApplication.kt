package com.tencent.wmpf.demo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class DemoApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // For 4.4 multi dex support
        MultiDex.install(this)
    }
}