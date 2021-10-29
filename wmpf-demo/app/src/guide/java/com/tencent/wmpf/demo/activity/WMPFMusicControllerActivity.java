package com.tencent.wmpf.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.tencent.wmpf.cli.api.WMPCliDefaultExecutor;
import com.tencent.wmpf.cli.api.WMPFMusicController;
import com.tencent.wmpf.demo.Api;
import com.tencent.wmpf.demo.R;

public class WMPFMusicControllerActivity extends AppCompatActivity {

    private final WMPFMusicController musicController = new WMPFMusicController(new WMPCliDefaultExecutor());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wmpfmusic_controller);

        Button playOrPauseBtn = findViewById(R.id.btn_play_or_pause);
        Button previousBtn = findViewById(R.id.btn_previous);
        Button nextBtn = findViewById(R.id.btn_next);
        Button startQQMusicBtn = findViewById(R.id.btn_start_qq_music);

        playOrPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicController.playOrPause();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicController.previous();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicController.next();
            }
        });

        startQQMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Api.INSTANCE.launchWxaApp("wxada7aab80ba27074", "", 0, 0).subscribe();
            }
        });

    }
}
