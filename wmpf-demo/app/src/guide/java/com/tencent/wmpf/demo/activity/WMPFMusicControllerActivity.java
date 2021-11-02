package com.tencent.wmpf.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.wmpf.cli.api.WMPFMusicController;
import com.tencent.wmpf.cli.api.WMPFMusicControllerInterface;
import com.tencent.wmpf.cli.api.WMPFMusicMetadata;
import com.tencent.wmpf.demo.Api;
import com.tencent.wmpf.demo.R;

public class WMPFMusicControllerActivity extends AppCompatActivity {

    private static final String TAG = "Demo.WMPFMusicCtrl";
    private final WMPFMusicController musicController = new WMPFMusicController();
    private final WMPFMusicControllerInterface.WMPFMusicStatusChangedListener listener
            = new WMPFMusicControllerInterface.WMPFMusicStatusChangedListener() {

        @Override
        public void onChanged(WMPFMusicControllerInterface.WMPFMusicPlayStatus newStatus) {
            Log.i(TAG, "new status = " + newStatus.name());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wmpfmusic_controller);

        Button playOrPauseBtn = findViewById(R.id.btn_play_or_pause);
        Button previousBtn = findViewById(R.id.btn_previous);
        Button nextBtn = findViewById(R.id.btn_next);
        Button startQQMusicBtn = findViewById(R.id.btn_start_qq_music);
        final Button queryIsPlayingBtn = findViewById(R.id.is_playing);
        Button getPlayingMusicBtn = findViewById(R.id.get_play_info);

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

        queryIsPlayingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isPlaying = musicController.isPlaying();
                        queryIsPlayingBtn.post(new Runnable() {
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

        getPlayingMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WMPFMusicMetadata musicMetadata = musicController.getPlayingMetadata();
                        if (musicMetadata != null) {
                            Log.i(TAG, "metadata = " + musicMetadata);
                        } else {
                            Log.i(TAG, "metadata is null ");
                        }
                    }
                });
                thread.start();
            }
        });
        musicController.addMusicPlayStatusListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicController.release();
    }
}
