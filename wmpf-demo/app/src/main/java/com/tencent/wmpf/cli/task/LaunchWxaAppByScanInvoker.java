package com.tencent.wmpf.cli.task;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tencent.mm.ipcinvoker.IPCInvokeCallback;
import com.tencent.mm.ipcinvoker.annotation.Nullable;
import com.tencent.mm.ipcinvoker.tools.Log;
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker;
import com.tencent.wmpf.proto.WMPFLaunchWxaAppByQRCodeRequest;
import com.tencent.wmpf.proto.WMPFLaunchWxaAppByQRCodeResponse;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class LaunchWxaAppByScanInvoker extends Activity {
    private static final String TAG = "MicroMsg.ScanProxyUI";

    private static final String KEY_REQ = "key_req";
    public static void launchWxaByScanUI(@NonNull Context context,
                                  @NonNull WMPFLaunchWxaAppByQRCodeRequest request) {
        Intent intent = new Intent(context, LaunchWxaAppByScanInvoker.class);
        intent.putExtra(KEY_REQ, request);
        context.startActivity(intent);
    }

    private WMPFLaunchWxaAppByQRCodeRequest request;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        request = getIntent().getParcelableExtra(KEY_REQ);

        if (request == null) {
            Log.e(TAG, "request is null,return");
            finish();
            return;
        }

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
                        request.rawData = rawData;
                        WMPFIPCInvoker.invokeAsync(
                                request,
                                IPCInvokerTask_LaunchWxaAppByQrCode.class,
                                new IPCInvokeCallback<WMPFLaunchWxaAppByQRCodeResponse>() {
                                    @Override
                                    public void onCallback(WMPFLaunchWxaAppByQRCodeResponse response) {
                                        Log.i(TAG, "response:%s", response);
                                        finish();
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
