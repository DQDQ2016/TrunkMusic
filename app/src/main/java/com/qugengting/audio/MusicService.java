package com.qugengting.audio;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MusicService extends Service {

    public MediaPlayer mediaPlayer; // 媒体播放器

    private String url;
    boolean isStopThread;//是否停止线程

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer=new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
         isStopThread=false;
        //【2】获取播放地址
        url = intent.getStringExtra("url");
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setLooping(false);//是否循环播放
            mediaPlayer.prepareAsync();//网络视频，异步
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int duration=mp.getDuration();
                    UpdateUI u=new UpdateUI();
                    u.setState(1);
                    u.setData(duration);
                    EventBus.getDefault().post(u);


                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
//                    EventBus.getDefault().post(new UpdateUI("",2));

                    UpdateUI u=new UpdateUI();
                    u.setState(2);
                    u.setData("");
                    EventBus.getDefault().post(u);
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }


        return new MyBinder();
    }




    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("音频","onUnbind()");

//        //关闭线程并释放资源
//        isStopThread=true;//避免线程仍在走
//
//        if (mediaPlayer.isPlaying())
//            mediaPlayer.release();
//        mediaPlayer=null;

        return super.onUnbind(intent);
    }


    public void stopmediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public class MyBinder extends Binder implements MyOperation {


        public  void getRingDuring(String mUri) {

            new Thread() {
                @Override
                public void run() {

            android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
            byte[] picture=null;
            try {
                if (mUri != null) {
                    HashMap<String, String> headers = null;
                    if (headers == null) {
                        headers = new HashMap<String, String>();
                    }
                    mmr.setDataSource(mUri, headers);
                }
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                EventBus.getDefault().post(new UpdateUI(title,1001));
                EventBus.getDefault().post(new UpdateUI(artist,1002));


                picture = mmr.getEmbeddedPicture();
                if (picture==null)
                {

                }
                else
                {
                    Bitmap bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
                    EventBus.getDefault().post(new UpdateUI(bitmap,1003));

                }

            } catch (Exception ex) {
            } finally {
                mmr.release();
            }
                }
            }.start();
        }

        @Override
        public void play() {//播放

            mediaPlayer.start();
            updateSeekBar();
            UpdateUI u=new UpdateUI();
            u.setState(1024);
            u.setData(2);
            EventBus.getDefault().post(u);

        }

        @Override
        public void pause() {//暂停
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                UpdateUI u=new UpdateUI();
                u.setState(1024);
                u.setData(1);
                EventBus.getDefault().post(u);
            }

        }

        @Override
        public void moveon() {//继续播放
            mediaPlayer.start();

            UpdateUI u=new UpdateUI();
            u.setState(1024);
            u.setData(2);
            EventBus.getDefault().post(u);

        }


        @Override
        public void rePlay() {//重新开始
            mediaPlayer.start();

        }

        @Override
        public void next() {
            isStopThread=true;



            int posstion= ListActivity.songIndex;
            int max=ListActivity.songs.size();
            if (posstion==max)
                posstion=0;
            else
                posstion++;

            ListActivity.songIndex=posstion;

            Log.e("test posstion", String.valueOf(posstion));
            String  murl="http://112.74.49.205:8085/musicList/"+ListActivity.songs.get(posstion).getFileName();
            try {
                getRingDuring(murl);

                stopmediaPlayer();
                mediaPlayer=new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(murl);

                mediaPlayer.setLooping(false);//是否循环播放
                mediaPlayer.prepareAsync();//网络视频，异步
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        int duration=mp.getDuration();
                        isStopThread=false;

                        UpdateUI u=new UpdateUI();
                        u.setState(1);
                        u.setData(duration);
                        EventBus.getDefault().post(u);

                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
//                        EventBus.getDefault().post(new UpdateUI("",2));

//                        int duration=mp.getDuration();
//
                        UpdateUI u=new UpdateUI();
                        u.setState(2);
                        u.setData("");
                        EventBus.getDefault().post(u);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void last() {
            isStopThread=true;

            int posstion= ListActivity.songIndex;
            int max=ListActivity.songs.size();
            Log.e("test last;max", String.valueOf(max));
            Log.e("test last;posstion", String.valueOf(posstion));
            if ((posstion-1)<0)
                posstion=max-1;
            else
                posstion-=1;
            Log.e("test last;posstion1", String.valueOf(posstion));
            ListActivity.songIndex=posstion;
            String  murl="http://112.74.49.205:8085/musicList/"+ListActivity.songs.get(posstion).getFileName();
            try {
                getRingDuring(murl);

                stopmediaPlayer();
                mediaPlayer=new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(murl);

                mediaPlayer.setLooping(false);//是否循环播放
                mediaPlayer.prepareAsync();//网络视频，异步
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        int duration=mp.getDuration();
                        isStopThread=false;
                        UpdateUI u=new UpdateUI();
                        u.setState(1);
                        u.setData(duration);
                        EventBus.getDefault().post(u);

                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
//                        EventBus.getDefault().post(new UpdateUI("",2));

//                        int duration=mp.getDuration();
//
                        UpdateUI u=new UpdateUI();
                        u.setState(2);
                        u.setData("");
                        EventBus.getDefault().post(u);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
         @Override
        public void setSong(String url) {

             isStopThread=true;
            try {
//                getRingDuring(url);

                stopmediaPlayer();
                mediaPlayer=new MediaPlayer();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(url);

                mediaPlayer.setLooping(false);//是否循环播放
                mediaPlayer.prepareAsync();//网络视频，异步
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        int duration=mp.getDuration();
                        isStopThread=false;

                        UpdateUI u=new UpdateUI();
                        u.setState(1);
                        u.setData(duration);
                        EventBus.getDefault().post(u);

                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
//                        EventBus.getDefault().post(new UpdateUI("",2));

//                        int duration=mp.getDuration();
//
                        UpdateUI u=new UpdateUI();
                        u.setState(2);
                        u.setData("");
                        EventBus.getDefault().post(u);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * 更新进度条
         */
        private void updateSeekBar() {
            //开启线程发送数据
            new Thread() {
                @Override
                public void run() {

                    while(!isStopThread){

                        try {

                            int currentPosition = mediaPlayer.getCurrentPosition();



                            UpdateUI u=new UpdateUI();
                            u.setState(3);
                            u.setData(currentPosition);
                            EventBus.getDefault().post(u);

                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                }

            }.start();
        }



        @Override
        public long getCurrentPosition() {
            return 0;
        }

        /**
         * 跳转到指定位置
         * @param position
         */
        @Override
        public void seekToPosition(int position) {
            mediaPlayer.seekTo(position);
        }

    }


}
