package com.tencent.wmpf.demo.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.mm.ipcinvoker.IPCInvokeCallback;
import com.tencent.mm.ipcinvoker.type.IPCVoid;
import com.tencent.wmpf.cli.api.WMPF;
import com.tencent.wmpf.cli.event.AbstractOnMakePhoneCallEventHandler;
import com.tencent.wmpf.cli.event.WMPFMakePhoneData;
import com.tencent.wmpf.demo.R;

/**
 * Created by complex.zeng on 11/5/21 4:40 PM.
 */
public class WMPFApiActivity extends AppCompatActivity {
    private static final String TAG = "WMPF.CLI.ApiActivity";
    private final AbstractOnMakePhoneCallEventHandler makePhoneCallEventHandler =
            new AbstractOnMakePhoneCallEventHandler() {
                @Override
                public void onInvoke(WMPFMakePhoneData wmpfMakePhoneData,
                                     @NonNull IPCInvokeCallback<IPCVoid> ipcInvokeCallback) {
                    Log.i(TAG, String.format("phoneNumber = [%s], appId = [%s]",
                            wmpfMakePhoneData.getPhoneNumber(), wmpfMakePhoneData.getAppId()));
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_api);
        Button enableKeyboardControl = findViewById(R.id.btn_enable_keyboard_control);
        Button registerMakePhoneCallEventHandlerBtn = findViewById(R.id.btn_register_make_phone_call_event_handler);
        Button unregisterMakePhoneCallEventHandlerBtn = findViewById(R.id.btn_unregister_make_phone_call_event_handler);
        enableKeyboardControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = WMPF.getInstance().setEnableKeyboardControl(true);
                        Log.i(TAG, "run: " + success);
                    }
                });
                thread.start();
            }
        });

        registerMakePhoneCallEventHandlerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WMPF.getInstance().registerMakePhoneCallEventHandler(makePhoneCallEventHandler);
            }
        });

        unregisterMakePhoneCallEventHandlerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WMPF.getInstance().unRegisterMakePhoneCallEventHandler(makePhoneCallEventHandler);
            }
        });
    }

}
