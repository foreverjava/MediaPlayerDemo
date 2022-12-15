package com.example.mediaplayerdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mediaplayerdemo.manager.MediaPlayerManager;

import java.io.File;
import java.util.Timer;

//继承View.OnClickListener，是按钮放在一起更直观，用另一种方法来设置按钮点击监听
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //定义三个按钮并实例化MediaPlayer
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static String TAG = "MainActivity";
    MediaPlayerManager mediaPlayerManager = new MediaPlayerManager(mediaPlayer);
    protected SeekBar seekBar;//进度条
    private Timer timer;//定时器
    protected TextView tv_start;//开始时间
    protected TextView tv_end;//结束时间
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。
    private Button play;//播放按钮
    private Button pause;//暂停按钮
    private Button stop;//停止按钮
    private static final int H_PROGRESS=1000;
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case H_PROGRESS:
                    mHandler.sendEmptyMessageDelayed(1000,1000);
                    tv_start.setText(calculateTime(mediaPlayer.getCurrentPosition()/1000));
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);
        seekBar = findViewById(R.id.seekbar);
        tv_start = findViewById(R.id.tv_start);
        tv_end = findViewById(R.id.tv_end);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        Log.d("MainActivity", "onProgressChanged: 11" + mediaPlayer.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("MainActivity", "onProgressChanged: " + mediaPlayer.getDuration());
                int duration2 = mediaPlayer.getDuration() / 1000;//获取音乐总时长
                int position = mediaPlayer.getCurrentPosition();//获取当前播放的位置
                tv_start.setText(calculateTime(position / 1000));//开始时间
                tv_end.setText(calculateTime(duration2));//总时长
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = false;
                Log.d("MainActivity", "onStopTrackingTouch: " +
                        seekBar.getProgress() + "mediaPlayer.getCurrentPosition() ="
                        + mediaPlayer.getCurrentPosition());
                mediaPlayer.seekTo(seekBar.getProgress() * mediaPlayer.getDuration() / 100);//在当前位置播放
                tv_start.setText(calculateTime(mediaPlayer.getCurrentPosition() / 1000));
            }
        });
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initMediaPlayer();//初始化MediaPlayer
        }
    }


    //计算播放时间
    public String calculateTime(int time) {
        int minute;
        int second;
        if (time >= 60) {
            minute = time / 60;
            second = time % 60;
            //分钟在0~9
            if (minute < 10) {
                //判断秒
                if (second < 10) {
                    return "0" + minute + ":" + "0" + second;
                } else {
                    return "0" + minute + ":" + second;
                }
            } else {
                //分钟大于10再判断秒
                if (second < 10) {
                    return minute + ":" + "0" + second;
                } else {
                    return minute + ":" + second;
                }
            }
        } else {
            second = time;
            if (second >= 0 && second < 10) {
                return "00:" + "0" + second;
            } else {
                return "00:" + second;
            }
        }
    }


    /*
     * 初始化MediaPlayer
     * */
    private void initMediaPlayer() {
        try {
            //此处有点简单了，调用MediaPlayer是安卓提供的最简单的播放器，并不能做其他操作
            File file = new File(Environment.getExternalStorageDirectory(), "music.mp3");
            mediaPlayer.setDataSource(file.getPath());//指定音频文件的路径
            mediaPlayer.prepare();//让MediaPlayer进入到准备状态
        } catch (Exception e) {
            e.printStackTrace();
        }
        int duration2 = mediaPlayer.getDuration() / 1000;
        int position = mediaPlayer.getCurrentPosition();
        tv_start.setText(calculateTime(position / 1000));
        tv_end.setText(calculateTime(duration2));
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMediaPlayer();
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();//开始播放
                    mHandler.sendEmptyMessage(H_PROGRESS);
                }
                break;
            case R.id.pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();//暂停播放
                }
                break;
            case R.id.stop:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();//停止播放
                    initMediaPlayer();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();

        }
    }
}