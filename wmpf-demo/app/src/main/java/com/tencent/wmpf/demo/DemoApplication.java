package com.tencent.wmpf.demo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.multidex.MultiDex;

import com.tencent.luggage.demo.wxapi.DeviceInfo;
import com.tencent.mmkv.MMKV;
import com.tencent.wmpf.app.WMPFBoot;
import com.tencent.wmpf.cli.model.WMPFDevice;
import com.tencent.wmpf.demo.utils.WMPFDemoUtil;

/**
 * For 4.4 multi dex support
 */
public class DemoApplication extends Application {
    private static String TAG = "WMPFDemoApplication";
    private static String ERR_NOT_INSTALLED = "WMPF Service APK 未安装。";
    private static String ERR_LOW_VERSION = "WMPF 版本过低，请升级到 2.1 及以上版本。当前版本为 ";

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
        // 本 demo 需要 WMPF 2.1 及以上版本方可使用。低版本请使用 apiv1 分支

        // 示例如果获取和判断 WMPF 版本
        try {
            int versionCode = WMPFDemoUtil.INSTANCE.getWmpfVersionCode(this);
            String version = WMPFDemoUtil.INSTANCE.getWmpfVersion(this);

            Log.i(TAG, "WMPF Service APK Version: " + version + "(" + versionCode + ")");
            if (versionCode < 9010001) { // 9010001 为 2.1.0
                Log.e(TAG, ERR_LOW_VERSION + version);
                Toast.makeText(this, ERR_LOW_VERSION + version, Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, ERR_NOT_INSTALLED);
            Toast.makeText(this, ERR_NOT_INSTALLED, Toast.LENGTH_LONG).show();
        }


        if (BuildConfig.FLAVOR != "experience") {
            // 体验 DEMO 需从后台获取设备信息，不在此处 init
            DeviceInfo info = DeviceInfo.INSTANCE;

            WMPFBoot.init(this, new WMPFDevice(
                    DeviceInfo.APP_ID,
                    info.getProductId(),
                    info.getKeyVersion(),
                    info.getDeviceId(),
                    info.getSignature()
            ));
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MMKV.onExit();
    }
}
