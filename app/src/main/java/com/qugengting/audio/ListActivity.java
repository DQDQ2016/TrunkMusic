package com.qugengting.audio;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qugengting.audio.dummy.SongListAdapter;
import com.qugengting.http.HttpThread;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {

    public static Intent svintent=null;

    private HttpThread thread = null;
    public static List<SongItem> songs = new ArrayList<>();
    private ListView mlist;
    private static final String PATH = "http://112.74.49.205:8085";
    private static String musicPATH = "http://112.74.49.205:8085/musicList/";
    public static int  songIndex=-1;//当前歌曲
    private int getlist=1313;
    private static int status; //0初始状态，1暂停，2播放，3播放完成
    public static  MusicService.MyBinder myBinder;//中间人对象
    public static MyConn myConn;
    private ImageButton btn_bottom;
    private ImageView img_bottom;
    private TextView tv_songname_bottom;
    private TextView tv_artist_bottom;
    private LinearLayout bottom_layout;

    private Bitmap mBitmap;
    private String mSongName;
    private String mArtistName;



    public static int duration;//音频总长度
    public static int currentPosition;//当前进度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        EventBus.getDefault().register(this);
        getSongList();

        init();
    }


    @SuppressLint("HandlerLeak")
    final
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            try {
                String str = (String) msg.obj;

                JSONArray array = new JSONArray(str);
                for (int i = 0; i < array.length(); i++) {
                    String regEx = ".mp3";
                    String blank = "";
                    String filename = array.getString(i);
                    String[] strs = filename.split("-");
                    String songName = "";
                    for (int j = 1; j < strs.length; j++)
                        songName += strs[j];
                    SongItem item = new SongItem();
                    item.setArtistName("  " + strs[0]);
                    item.setFileName(filename);
                    String songNameReplace = songName.replaceAll(regEx, blank);
                    item.setSongName(songNameReplace);

                    songs.add(item);
                    Log.e("song  http:", item.getFileName());
                }

                SongListAdapter adapter = new SongListAdapter(getApplicationContext(), R.layout.song_item, songs);
                mlist = findViewById(R.id.mlist);
                mlist.setAdapter(adapter);
                mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        songIndex=position;

                        if(svintent==null)
                        {


                            svintent = new Intent(getApplicationContext(), MusicService.class);
                            svintent.putExtra("url", musicPATH + songs.get(position).getFileName());


                            Log.e("clicktest:", songs.get(position).getFileName());

                            myConn = new MyConn();

                            bindService(svintent,myConn,BIND_AUTO_CREATE);

                            Intent i = new Intent(getApplicationContext(), PlayActivity.class);
                            i.putExtra("filename", songs.get(position).getFileName());//传递一个字符串参数，参数的name值为“data”，数值为“hello android
                            startActivity(i);//启动另一个activity

                        }
                        else
                        {


                            myBinder.setSong(musicPATH + songs.get(position).getFileName());
                            Intent i = new Intent(getApplicationContext(), PlayActivity.class);
                            i.putExtra("filename", songs.get(position).getFileName());//传递一个字符串参数，参数的name值为“data”，数值为“hello android
                            startActivity(i);//启动另一个activity
                        }



                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private void getSongList()
    {
        thread=new HttpThread(PATH,handler,getlist);
        thread.Start();
    }

    private void init()
    {
        bottom_layout=findViewById(R.id.bottom_layout);
        img_bottom=findViewById(R.id.img_bottom);
        tv_songname_bottom=findViewById(R.id.playbar_songname);
        tv_artist_bottom=findViewById(R.id.playbar_artist);

        tv_artist_bottom.setOnClickListener(this);
        tv_songname_bottom.setOnClickListener(this);
        img_bottom.setOnClickListener(this);
        btn_bottom=findViewById(R.id.btn_bottom);
        btn_bottom.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                Log.e("test status", String.valueOf(status));
                if (status==2) {
                    status=1;
                    btn_bottom.setBackground(getDrawable(R.drawable.play_b));
                    ListActivity.myBinder.pause();
                } else {
                    ListActivity.myBinder.moveon();
                    status=2;
                    btn_bottom.setBackground(getDrawable(R.drawable.stop_b));

                }
            }
        });

        bottom_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("test click","");
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
        } else if (flag == 2) {//播放完成

            btn_bottom.setBackground(getDrawable(R.drawable.play_b));

        } else if (flag == 3) {//更新进度
            if (status == 3)//避免播放完成通知与线程通知冲突
                return;
            currentPosition = (int) updateUI.getData();

        }

        if (flag==1024)
        {

            status= (int) updateUI.getData();


            if (status==2 || status==3)
            {
                btn_bottom.setBackground(getDrawable(R.drawable.stop_b));
            }
            else
            {
                btn_bottom.setBackground(getDrawable(R.drawable.play_b));
            }
        }

        if (flag==1001)
        {

            Log.e("test","setname");
            String songname= (String) updateUI.getData();
             tv_songname_bottom.setText(songname);
             mSongName=songname;

        }

        if (flag==1002)
        {
            String artist= (String) updateUI.getData();
             tv_artist_bottom.setText(artist);
             mArtistName=artist;
        }


        if (flag==1003)
        {
            Bitmap bitmap= updateUI.getBitmap();
            img_bottom.setImageBitmap(bitmap);
            mBitmap=bitmap;
         }
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(getApplicationContext(), PlayActivity.class);
        i.putExtra("filename", songs.get(songIndex).getFileName());//传递一个字符串参数，参数的name值为“data”，数值为“hello android
        i.putExtra("ischange","1");
        i.putExtra("songname",mSongName);
        i.putExtra("artist",mArtistName);

        byte buf[] = new byte[1024*1024];
         buf = Bitmap2Bytes(mBitmap);
        i.putExtra("bitmap",buf);


        startActivity(i);//启动另一个activity
    }


    private byte[] Bitmap2Bytes(Bitmap bm){
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
             return baos.toByteArray();
          }
    public static class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取中间人对象
            myBinder = (MusicService.MyBinder) service;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }







}
