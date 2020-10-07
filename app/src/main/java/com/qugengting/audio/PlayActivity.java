package com.qugengting.audio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends Activity {
    private static final String TAG = PlayActivity.class.getSimpleName();
    private boolean isPrepare = false;

    private static int duration;//音频总长度
    private static int currentPosition;//当前进度

    private static int status; //0初始状态，1暂停，2播放，3播放完成

    private SeekBar seekBar;
    private static String PATH = "";

    private TextView tvCurrent;
    private TextView tvDuration;
    public static TextView tvName;
    public static TextView tvArtist;
    private ImageButton btnPlay;
    private LinearLayout playview;
    public static ImageView logoview;
    private ImageButton btn_next;
    private ImageButton btn_last;








    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);


        EventBus.getDefault().register(this);
        Intent i = getIntent();//获取相关的intent

        PATH="http://112.74.49.205:8085/musicList/";


        seekBar = findViewById(R.id.seekBar);
        tvCurrent = findViewById(R.id.tv_current);
        tvDuration = findViewById(R.id.tv_duration);
        btnPlay = findViewById(R.id.btn_play);
        playview = findViewById(R.id.play_view);
        logoview= findViewById(R.id.tv_logo);
        tvName=findViewById(R.id.tv_name);
        tvArtist=findViewById(R.id.tv_artist);
        btn_next=findViewById(R.id.btn_next);
        btn_last=findViewById(R.id.btn_last);


        if (i.getStringExtra("ischange")!=null)
        {
            seekBar.setMax(ListActivity.duration);
            tvDuration.setText(getTime(ListActivity.duration));
            tvCurrent.setText(getTime(seekBar.getProgress()));
            byte buf[] = i.getByteArrayExtra("bitmap");
            Bitmap photo_bmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
            logoview.setImageBitmap(photo_bmp);
            tvArtist.setText(i.getStringExtra("artist"));
            tvName.setText(i.getStringExtra("songname"));
            btnPlay.setBackground(getDrawable(R.drawable.stop));

        }
        else
        {
            PATH+=i.getStringExtra("filename");
            Log.e("test songPath",PATH);
            ListActivity.myBinder.getRingDuring(PATH);

        }

        btn_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.myBinder.last();

            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListActivity.myBinder.next();

            }
        });




        btnPlay.setEnabled(true);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Log.e(TAG, "is test msg ");

                Log.e("test status", String.valueOf(status));
                if (status==2) {
                    status=1;
                    btnPlay.setBackground(getDrawable(R.drawable.play));
                    ListActivity.myBinder.pause();
                } else {
                    ListActivity.myBinder.moveon();
                    status=2;
                    btnPlay.setBackground(getDrawable(R.drawable.stop));

                }
            }
        });
//        loadingCover(PATH);
        seekBar.setProgress(0);//设置进度为0
        seekBar.setSecondaryProgress(0);//设置缓冲进度为0
        seekBar.setEnabled(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onProgressChanged(SeekBar mSeekBar, int progress, boolean fromUser) {
                tvCurrent.setText(getTime(mSeekBar.getProgress()));
                if (progress!=0 && status==2)
                btnPlay.setBackground(getDrawable(R.drawable.stop));
                else
                    if(status==1)
                    btnPlay.setBackground(getDrawable(R.drawable.play));

            }

            @Override
            public void onStartTrackingTouch(SeekBar mSeekBar) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onStopTrackingTouch(SeekBar mSeekBar) {
                if (status==2) {
                    ListActivity.myBinder.seekToPosition(mSeekBar.getProgress());
//                    mediaPlayer.seekTo(mSeekBar.getProgress());
                    tvCurrent.setText(getTime(mSeekBar.getProgress()));
                } else {
                    if (status==1) {
                        status=2;
                        ListActivity.myBinder.seekToPosition(mSeekBar.getProgress());
//                        mediaPlayer.seekTo(mSeekBar.getProgress());
                        tvCurrent.setText(getTime(mSeekBar.getProgress()));
                        ListActivity.myBinder.moveon();
                        btnPlay.setBackground(getDrawable(R.drawable.stop));
//                        btnPlay.setText("暂停");
                    }
                }
            }
        });




    }






    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe(threadMode = ThreadMode.MAIN)  //3.0之后，需要加注解
    public void onEventMainThread(UpdateUI updateUI) {
        int flag = updateUI.getState();

        Log.d("音频状态", status + " ---- " + flag);
        //【3】设置进度
        if (flag == 1) {//准备完成，获取音频长度
            duration  = (int) updateUI.getData();
            Log.d(TAG, "总长度" + duration);
            //设置总长度
            seekBar.setMax(duration);
            tvDuration.setText(getTime(duration));
            ListActivity.myBinder.play();
            status=2;


        } else if (flag == 2) {//播放完成
            Log.d(TAG, "播放完成～");
            btnPlay.setBackground(getDrawable(R.drawable.play));

            status = 3;//已完成
            //重置信息
            seekBar.setProgress(0);
            tvCurrent.setText("00:00");
//            button.setText("重新播放");

//            updateNotification(3);

        } else if (flag == 3) {//更新进度
            if (status == 3)//避免播放完成通知与线程通知冲突
                return;
            currentPosition = (int) updateUI.getData();
            Log.d(TAG, "当前进度" + currentPosition);

            //设置进度
            tvCurrent.setText(getTime(currentPosition));

            seekBar.setProgress(currentPosition);
        } else if (flag == 1024)
        {
            status= (int) updateUI.getData();
        }else if (flag==1001)
        {
            Log.e("test1111111","");
            String songname= (String) updateUI.getData();
            tvName.setText(songname);

        }else if (flag==1002)
        {
            String artist= (String) updateUI.getData();
            tvArtist.setText(artist);
         }else if (flag==1003)
        {
            Bitmap bitmap= updateUI.getBitmap();
            logoview.setImageBitmap(bitmap);
        }
    }








    private String getTime(int duration) {
        int i = duration / 1000;
        int h = i / (60 * 60);
        String sh;
        if (h == 0) {
            sh = "00";
        } else {
            sh = String.valueOf(h);
        }
        int m = i / 60 % 60;
        String sm;
        if (m == 0) {
            sm = "00";
        } else {
            sm = String.valueOf(m);
            if (sm.length() == 1) {
                sm = "0" + sm;
            }
        }
        int s = i % 60;
        String ss;
        if (s == 0) {
            ss = "00";
        } else {
            ss = String.valueOf(s);
            if (ss.length() == 1) {
                ss = "0" + ss;
            }
        }
        return    sm + ":" + ss;
    }





}
