package com.bytedance.videoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity {

    private Button buttonPlay;
    private Button buttonPause;
    private Button buttonOpen;
    private VideoView videoView;
    private SeekBar seekBar;
    private TextView textCurTime;
    private TextView textTotalTime;
    private ChangeSeekBar changeSeekBar;

    private String videoPath;
    private int curProgress;
    private boolean playingFlag;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
    private static final String TAG = "main";
    private static final int REQUEST_VIDEO_PATH = 1;

    //定义一个AsyncTask来更新seekBar
    @SuppressLint("StaticFieldLeak")
    private class ChangeSeekBar extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while(videoView.isPlaying()){
                publishProgress(videoView.getCurrentPosition());
                SystemClock.sleep(100);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            seekBar.setProgress(values[0]);
            curProgress = values[0];
            textCurTime.setText(SIMPLE_DATE_FORMAT.format(new Date(values[0] - TimeZone.getDefault().getRawOffset())));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //从外部打开时，在Intent中获取视频地址
        Uri uri = getIntent().getData();
        if (uri != null){
            videoPath = uri.toString();
        }

        Log.d(TAG, "onCreate: curProgress: " + String.valueOf(curProgress) + " playingFlag: " + String.valueOf(playingFlag));
        //旋转屏幕后恢复这些值。
        if (savedInstanceState != null){
            curProgress = savedInstanceState.getInt("curProgress");
            playingFlag = savedInstanceState.getBoolean("playingFlag");
            videoPath = savedInstanceState.getString("videoPath");
        }
        Log.d(TAG, "onCreate: curProgress: " + String.valueOf(curProgress) + " playingFlag: " + String.valueOf(playingFlag));

        //横屏时设置为全屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(R.layout.activity_main);
        setTitle("Video Player");
        videoView = (VideoView) findViewById(R.id.videoView);
        buttonPause = (Button) findViewById(R.id.buttonPause);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        textCurTime = (TextView) findViewById(R.id.curTime);
        textTotalTime = (TextView) findViewById(R.id.totalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        buttonOpen = (Button) findViewById(R.id.buttonOpen);

        //添加MediaControl并做一些layout的小修改
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            videoView.setLayoutParams(layoutParams);

        }

        //默认打开
        if (videoPath == null)
            videoPath = "android.resource://" + this.getPackageName() + "/" + R.raw.bytedance;
        Log.d(TAG, "videoPath: " + videoPath);

        videoView.setVideoPath(videoPath);

        //加载好视频后设置一些界面上的值
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(videoView.getDuration());
                textTotalTime.setText(SIMPLE_DATE_FORMAT.format(new Date(videoView.getDuration() - TimeZone.getDefault().getRawOffset())));
                textCurTime.setText(SIMPLE_DATE_FORMAT.format(new Date(curProgress - TimeZone.getDefault().getRawOffset())));
                Log.d(TAG, "seekTo " + String.valueOf(curProgress));
                videoView.seekTo(curProgress);
                seekBar.setProgress(curProgress);
                if (playingFlag){
                    videoView.start();
                    changeSeekBar = new ChangeSeekBar();
                    changeSeekBar.execute();
                }
            }
        });

        //双击播放/暂停
        videoView.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_TIME = 500;
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
                    if (videoView.isPlaying()){
                        videoView.pause();
                    }
                    else{
                        videoView.start();
                        changeSeekBar = new ChangeSeekBar();
                        changeSeekBar.execute();
                    }
                }
                lastClickTime = currentTimeMillis;
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
                changeSeekBar = new ChangeSeekBar();
                changeSeekBar.execute();
            }
        });

        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_VIDEO_PATH);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    textCurTime.setText(SIMPLE_DATE_FORMAT.format(new Date(progress - TimeZone.getDefault().getRawOffset())));
                    curProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    @Override
    protected void onPause() {
        curProgress = videoView.getCurrentPosition();
        playingFlag = videoView.isPlaying();
        Log.d(TAG, "onPause: curProgress: " + String.valueOf(curProgress) + " playingFlag: " + String.valueOf(playingFlag));
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("curProgress", curProgress);
        outState.putBoolean("playingFlag", playingFlag);
        outState.putString("videoPath", videoPath);
        Log.d(TAG, "onSaveInstanceState: curProgress: " + String.valueOf(curProgress) + " playingFlag: " + String.valueOf(playingFlag));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_VIDEO_PATH && resultCode == Activity.RESULT_OK){
            if (data != null){
                Uri uri = data.getData();
                if (uri != null) {
                    videoView.setVideoURI(uri);
                    videoPath = uri.toString();
                    curProgress = 0;
                    Log.d(TAG, "onActivityResult: videoPath = " + uri.toString());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
