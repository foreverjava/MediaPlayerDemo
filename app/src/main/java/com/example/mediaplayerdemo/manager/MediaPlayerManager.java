package com.example.mediaplayerdemo.manager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created By LicaiWen
 * To DO:
 */
public class MediaPlayerManager {
    private static String TAG = "MediaPlayerManager";
    private MediaPlayer mediaPlayer;
    //用于存外部实现了OnMusicProgressListener重写了它的方法的类的实例
    private OnMusicProgressListener musicProgressListener;
    private static final int H_PROGRESS=1000;
    public static final int MEDIA_STATUS_PLAY =0;
    public static final int MEDIA_STATUS_PAUSE =1;
    public static final int MEDIA_STATUS_STOP =2;
    public static  int MEDIA_STATUS=MEDIA_STATUS_STOP;
    private Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case H_PROGRESS:
                    if(musicProgressListener!=null){
                        Log.d(TAG, "handleMessage: " + mediaPlayer.getCurrentPosition());
                        int currentPosition=mediaPlayer.getCurrentPosition();
                        //position 是当前的百分比，记得要先强转为float，不然全是0；
                        int position=(int)((float)currentPosition/(float)duration()*100);
                        //调用外部重写的方法
                        musicProgressListener.onProgress(currentPosition,position);
                        //1秒后再次发送Message，接着又是handle。循环
                        handler.sendEmptyMessageDelayed(H_PROGRESS,1000);
                    }
            }
            return false;
        }
    });
    public MediaPlayerManager(MediaPlayer mediaPlayer ){
        this.mediaPlayer=mediaPlayer;
    }


    //继续播放不需要重置
    public void continuePlay(){
        mediaPlayer.start();
        handler.sendEmptyMessage(H_PROGRESS);
        MEDIA_STATUS=MEDIA_STATUS_PLAY;
    }
    public void stopPlay(){
        mediaPlayer.stop();
        handler.removeMessages(H_PROGRESS);
        MEDIA_STATUS=MEDIA_STATUS_STOP;
    }
    public void setLooping(boolean isLooping){
        mediaPlayer.setLooping(isLooping);
    }
    //获取当前位置
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    //获取中位置
    public int duration(){
        return   mediaPlayer.getDuration();
    }
    //实现接口
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        mediaPlayer.setOnCompletionListener(listener);
    }
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener){
        mediaPlayer.setOnErrorListener(listener);
    }
    public void setOnProgressListener(OnMusicProgressListener listener){
        //获取外部的OnMusicProgressListener
        //这里的listener相当于是一个类实现了OnMusicProgressListener接口并重写了onProgress(int progress,int position)方法，listener是这个类的实例
        //当用musicProgressListener去调用onProgress方法的时候，便是调用你在外部重写的方法，注意这里的技巧是获取了外部的listener，在内部调用它重写的方法。
        //什么时候调用呢？巧妙利用handle实现循环调用，更新数据。
        musicProgressListener=listener;
    }

    //创建一个接口
    public interface OnMusicProgressListener{
        void onProgress(int progress,int position);

    }

}

