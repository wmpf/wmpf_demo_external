package com.tencent.wmpf.demo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.tencent.mm.ipcinvoker.annotation.Nullable;
import com.tencent.mm.ipcinvoker.tools.Log;
import com.tencent.wmpf.cli.api.WMPF;
import com.tencent.wmpf.cli.api.WMPFApiException;
import com.tencent.wmpf.demo.utils.WMPFDemoUtil;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class LaunchWxaAppByScanInvoker extends Activity {
    private static final String TAG = "WMPF.ScanProxyUI";

    public static void launchWxaByScanUI(@NonNull Context context) {
        Intent intent = new Intent(context, LaunchWxaAppByScanInvoker.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZXingLibrary.initDisplayOpinion(this);
        doScanImpl();
    }

    private final static int REQ_CODE = 100;

    private void doScanImpl() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (null != data && null != data.getExtras()) {
                Bundle bundle = data.getExtras();
                int retCode = bundle.getInt(CodeUtils.RESULT_TYPE);
                Log.i(TAG, "retCode:%d", retCode);

                switch (retCode) {
                    case CodeUtils.RESULT_SUCCESS: {
                        String rawData = bundle.getString(CodeUtils.RESULT_STRING);
                        Log.i(TAG, "rawData:%s", rawData);

                        WMPFDemoUtil.INSTANCE.execute(() -> {
                            try {
                                WMPF.getInstance().getMiniProgramApi().launchByQRScanCode(rawData);
                            } catch (WMPFApiException e) {
                                Log.e(TAG, "launchByQRScanCode fail: " + e);
                            }
                        });
                        break;
                    }

                    case CodeUtils.RESULT_FAILED: {
                        Log.e(TAG, "scan fail, return");
                        finish();
                    }
                }
            } else {
                Log.e(TAG, "data is null, return");
                finish();
            }
        }
    }
}
