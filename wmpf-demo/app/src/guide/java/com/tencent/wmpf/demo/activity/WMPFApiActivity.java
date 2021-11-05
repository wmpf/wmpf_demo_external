package com.tencent.wmpf.demo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.wmpf.cli.api.WMPF;
import com.tencent.wmpf.demo.R;

/**
 * Created by complex.zeng on 11/5/21 4:40 PM.
 */
public class WMPFApiActivity extends AppCompatActivity {
    private static final String TAG = "WMPF.CLI.ApiActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_api);
        Button enableKeyboardControl = findViewById(R.id.btn_enable_keyboard_control);
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
    }

}
