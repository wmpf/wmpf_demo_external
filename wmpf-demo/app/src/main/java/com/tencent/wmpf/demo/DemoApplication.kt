package com.tencent.wmpf.demo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.tencent.mmkv.MMKV

class DemoApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // For 4.4 multi dex support
        MultiDex.install(this)
        val rootDir = MMKV.initialize(this)
        println("mmkv root: $rootDir")
    }

    override fun onTerminate() {
        super.onTerminate()
        MMKV.onExit()
    }
}