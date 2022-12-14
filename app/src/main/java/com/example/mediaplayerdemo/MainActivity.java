package com.example.mediaplayerdemo;

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

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

//继承View.OnClickListener，是按钮放在一起更直观，用另一种方法来设置按钮点击监听
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //定义三个按钮并实例化MediaPlayer
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageView imageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button play = (Button)findViewById(R.id.play);
        Button pause = (Button)findViewById(R.id.pause);
        Button stop = (Button)findViewById(R.id.stop);
        imageView =(ImageView) findViewById(R.id.imageView);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            initMediaPlayer();//初始化MediaPlayer
        }
    }
    /*
     * 初始化MediaPlayer
     * */
    private void initMediaPlayer(){
        try {
            //此处有点简单了，调用MediaPlayer是安卓提供的最简单的播放器，并不能做其他操作
            File file = new File(Environment.getExternalStorageDirectory(),"music.mp3");
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource("/storage/emulated/0/music.mp3");
            Log.d("MainActivity", "initMediaPlayer: absolutePath = " + mediaMetadataRetriever.getEmbeddedPicture());
            Glide.with(this).load(mediaMetadataRetriever.getEmbeddedPicture()).into(imageView);
            mediaPlayer.setDataSource(file.getPath());//指定音频文件的路径
            mediaPlayer.prepare();//让MediaPlayer进入到准备状态
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else {
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.play:
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();//开始播放
                }
                break;
            case R.id.pause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();//暂停播放
                }
                break;
            case R.id.stop:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.reset();//停止播放
                    initMediaPlayer();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();

        }
    }
}