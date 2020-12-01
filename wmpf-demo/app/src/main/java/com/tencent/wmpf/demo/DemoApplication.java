package com.tencent.wmpf.demo;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.mmkv.MMKV;
import com.tencent.wmpf.app.WMPFApplication;

/**
 * For 4.4 multi dex support
 */
public class DemoApplication extends WMPFApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);

        String rootDir = MMKV.initialize(this);
        System.out.println("mmkv root: " + rootDir);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 只需要关注Api类中的方法即可跑通WMPF
        Api.INSTANCE.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MMKV.onExit();
    }
}
