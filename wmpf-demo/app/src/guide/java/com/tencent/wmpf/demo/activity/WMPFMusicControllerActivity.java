package com.tencent.wmpf.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.wmpf.cli.api.WMPFClientDefaultExecutor;
import com.tencent.wmpf.cli.api.WMPFMusicController;
import com.tencent.wmpf.demo.Api;
import com.tencent.wmpf.demo.R;

public class WMPFMusicControllerActivity extends AppCompatActivity {

    private final WMPFClientDefaultExecutor executor = new WMPFClientDefaultExecutor();
    private final WMPFMusicController musicController = new WMPFMusicController(executor);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wmpfmusic_controller);

        Button playOrPauseBtn = findViewById(R.id.btn_play_or_pause);
        Button previousBtn = findViewById(R.id.btn_previous);
        Button nextBtn = findViewById(R.id.btn_next);
        Button startQQMusicBtn = findViewById(R.id.btn_start_qq_music);
        final Button queryIsPlaying = findViewById(R.id.is_playing);

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

        queryIsPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isPlaying = musicController.isPlaying();
                        queryIsPlaying.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WMPFMusicControllerActivity.this,
                                        String.format("isPlaying = [%b]", isPlaying),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                thread.start();
            }
        });

    }
}
