package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

   private ImageView nextIV,playIV,lastIV,albumIV,menuIV;
   private TextView singerTV,songTV;
   private SearchView musicSearch;
   private RecyclerView musicRV;
   private List<LocalMusicBean> MainData, SetData;
   private LocalMusicAdapter MusicAdapter;

   //与服务有关的
    private  int musicDataSize;
    private int currentId = -2;
    private musicService.MyBinder myBinder;
    private ServiceConnection serviceConnection;
    private Handler handler;
    private SeekBar seekBar;
    private Runnable runnable;
    private BroadcastReceiver myReceiver;
    private boolean isNetAvailable = false;

    //侧滑栏部分
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Fragment fragment_about, fragment_introduction, fragment_message;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int Notification_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //初始化控件
        initView();

        //设置RecyclerView
        setRecycleView();

        //获取本地数据源
        loadLocalMusicData();

        //启动音乐服务
        Intent serviceStart = new Intent(this,musicService.class);
        startService(serviceStart);
        //绑定服务
        Intent mediaServiceIntent = new Intent(this, musicService.class);
        serviceConn();
        bindService(mediaServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //设置广播接受者
        setReceiver();

        //设置搜索框
        setSearchList();

        //设置侧滑栏
        setDrawer();

        //设置点击事件
        setEventListener();

        //设置菜单fragment页面
        setFragment();
        

    }

    private void setFragment() {
    }

    private void setEventListener() {
    }

    private void setDrawer() {
    }

    private void setSearchList() {
    }

    private void setReceiver() {
    }

    private void serviceConn() {
    }

    private void loadLocalMusicData() {
        //加载本地存储当中的音乐mp3文件到集合当中
        // 获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        // 获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 开始查询地址
        Cursor cursor  = resolver.query(uri,null,null,null,null);
        // 遍历Cursor
        int id =0;
        while (cursor.moveToNext()){
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sId = String.valueOf(id);
            String path =cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration =cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat simpleDateFormat =new SimpleDateFormat("mm:ss");
            String time = simpleDateFormat.format(new Date(duration));
            //封装数据
            LocalMusicBean localMusicBean =new LocalMusicBean(sId,song,singer,album,time,path);



        }



    }
    /*
     设置RecyclerView
     */
    private void setRecycleView() {
        MainData = new ArrayList<>();
        SetData = new ArrayList<>();
        //创建适配器对象
        MusicAdapter = new LocalMusicAdapter(this, SetData);
        musicRV.setAdapter(MusicAdapter);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        musicRV.setLayoutManager(layoutManager);
    }


    /*
    初始化控件
     */
    private void initView() {
        //主页面控件
        nextIV = findViewById(R.id.music_bottom_next);
        playIV = findViewById(R.id.music_play);
        lastIV = findViewById(R.id.music_last);
        albumIV = findViewById(R.id.music_bottom_icon);
        singerTV = findViewById(R.id.music_bottom_singer);
        songTV = findViewById(R.id.music_bottom_song);
        musicSearch = findViewById(R.id.music_search);
        musicRV = findViewById(R.id.music_rv);
        seekBar = findViewById(R.id.music_seekBar);

        //点击事件
        nextIV.setOnClickListener(this);
        lastIV.setOnClickListener(this);
        playIV.setOnClickListener(this);


        //侧滑栏控件
        menuIV = findViewById(R.id.menu_icon);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.music_bottom_next:

                break;
            case R.id.music_play:

                break;
            case R.id.music_last:

                break;
        }

    }
}