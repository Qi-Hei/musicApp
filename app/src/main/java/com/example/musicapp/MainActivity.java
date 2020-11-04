package com.example.musicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicapp.Fragment.Fragment_add;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

   private ImageView nextIV,playIV,lastIV,albumIV,menuIV,modeIV;
   private TextView singerTV,songTV;
   private SearchView musicSearch;
   private RecyclerView musicRV;
   private List<LocalMusicBean> MainData, SetData;
   private LocalMusicAdapter musicAdapter;
   private MediaPlayer mediaPlayer;

    //定时器
    private Timer timer;
    //互斥变量
    boolean isSeekbarChanging;

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
    private Fragment fragment_add;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int Notification_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = new MediaPlayer();
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
        fragmentManager = getSupportFragmentManager();
        fragment_add = new Fragment_add();
        transaction = fragmentManager.beginTransaction();
    }

    /* 设置每一项的点击事件  */
    private void setEventListener() {
        musicAdapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                LocalMusicBean bean = SetData.get(position);
                playMusicOnService(Integer.parseInt(bean.getId())-1);
            }
        });
        //设置单曲循环/列表循环/随机循环
        modeIV.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                myBinder.setPlayModule();
                if(myBinder.getPlayModule()==0){
                    Toast.makeText(MainActivity.this,"随机播放",Toast.LENGTH_SHORT).show();
                    modeIV.setImageResource(R.mipmap.music_random);
                }
                else if(myBinder.getPlayModule()==1){
                    Toast.makeText(MainActivity.this,"单曲循环",Toast.LENGTH_SHORT).show();
                    modeIV.setImageResource(R.mipmap.music_cycle);
                }
                else {
                    Toast.makeText(MainActivity.this,"列表循环",Toast.LENGTH_SHORT).show();
                    modeIV.setImageResource(R.mipmap.music_list);
                }

            }
        });

        menuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


    }

    private void playMusicOnService( int Id) {
        //根据传入id播放音乐
        if(Id<10 && isNetAvailable)
            Toast.makeText(MainActivity.this,"在线歌曲",Toast.LENGTH_SHORT).show();
        //设置服务信息
        myBinder.setMusic(Id);
        playIV.setImageResource(R.mipmap.music_pause);
    }


    private void setDrawer() {
        navigationView.setItemIconTintList(null);
        navigationView.getChildAt(0).setVerticalScrollBarEnabled(false);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_home:
                        Log.d("ItemSelectiedLister","home");
                        transaction =fragmentManager.beginTransaction();
                        transaction.remove(fragment_add).commit();
                        break;
                    case R.id.menu_add:
                        transaction  = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment,fragment_add).commit();
                        Log.d("ItemSelectedListener","add");
                        break;
                    case R.id.menu_function:
                        Toast.makeText(MainActivity.this,"功能",Toast.LENGTH_SHORT).show();
                        Log.d("ItemSelectedListener","function");
                        break;
                    case R.id.menu_about:
                        Toast.makeText(MainActivity.this,"关于",Toast.LENGTH_SHORT).show();
                        Log.d("ItemSelectedListener","about");
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                transaction.addToBackStack(null);
                return true;
            }
        });

    }

    private void setSearchList() {
        //设置SearchView默认是否自动缩小为图标
        musicSearch.setIconifiedByDefault(true);
        musicSearch.setFocusable(false);
        //设置搜索框监听器
        musicSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //点击搜索时激发
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //输入时激发
                if(TextUtils.isEmpty(newText)) {
                    SetData.clear();
                    SetData.addAll(MainData);
                    musicAdapter.notifyDataSetChanged();
                }else {
                    //根据输入内容对RecycleView进行搜索
                    SetData.clear();
                    for(LocalMusicBean bean:MainData){
                        if(bean.getSong().contains(newText)|| bean.getSinger().contains(newText)){
                            SetData.add(bean);
                        }
                    }
                    musicAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    private void setReceiver() {
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                songTV.setText(intent.getStringExtra("music_song"));
                singerTV.setText(intent.getStringExtra("music_singer"));
                currentId = intent.getIntExtra("music_Id",-1);
                seekBar.setMax(intent.getIntExtra("music_duration",0));
            }
        };
        IntentFilter intentFilter = new IntentFilter("UI_info");
        registerReceiver(myReceiver,intentFilter);
    }

    private void serviceConn() {
        serviceConnection = new ServiceConnection(){

            @SuppressLint("HandlerLeak")
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myBinder =(musicService.MyBinder) service;
                //如果主activity启动时正在播放
                if(myBinder.getMediaPlayState() ==1){
                    playIV.setImageResource(R.mipmap.music_pause);
                    songTV.setText(myBinder.getMusicSong());
                    singerTV.setText(myBinder.getMusicSinger());
                    currentId=myBinder.getMusicId();
                    //设置进度条大小
                    seekBar.setMax(myBinder.getMusicDuration());
                }

                //传递播放列表
                myBinder.setData(MainData);

                //初始化进度条
                seekBar.setProgress(0);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //响应用户点击设置进度条
                        if(fromUser && currentId !=-1 && currentId !=-2){
                            myBinder.seekToPosition(seekBar.getProgress());
                        }
                        else if(fromUser){
                            Toast.makeText(MainActivity.this, "请选择播放音乐", Toast.LENGTH_SHORT).show();
                            seekBar.setProgress(0);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                handler = new Handler();
                runnable = new Runnable() {
                    private int pre=-1, pos;
                    @Override
                    public void run() {
                        pos = myBinder.getPlayPosition();
                        if(currentId!=-1 && currentId!=-2)
                            seekBar.setProgress(pos);
                        Log.d("RunnablePos", String.valueOf(pos));

                        if(pre!=pos) handler.postDelayed(runnable, 1000);
                        else handler.postDelayed(runnable, 2000);
                        pre = pos;
                    }
                };
                handler.post(runnable);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    private void loadLocalMusicData() {
        //加载本地存储当中的音乐mp3文件到集合当中
        LocalMusicBean bean;
        // 获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        // 获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 开始查询地址
        Cursor cursor  = resolver.query(uri,null,null,null,null);
        // 遍历Cursor
        int id = -1;
        while (cursor.moveToNext()){
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sId = String.valueOf(id+1);
            String path =cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration =cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            //封装数据
            LocalMusicBean localMusicBean =new LocalMusicBean(sId,song,singer,album,duration,path);
            MainData.add(localMusicBean);
        }
        if(id==0) {
            bean = new LocalMusicBean("0", "没有找到本地歌曲", "", "", 0, "");
            MainData.add(bean);
            currentId=-2;
        } else{
            currentId=-1;
        }
        musicDataSize = MainData.size();
        SetData.addAll(MainData);
        //数据源变化，提示适配器更新
        musicAdapter.notifyDataSetChanged();
    }
    /*
     设置RecyclerView
     */
    private void setRecycleView() {
        MainData = new ArrayList<>();
        SetData = new ArrayList<>();
        //创建适配器对象
        musicAdapter = new LocalMusicAdapter(this, SetData);
        musicRV.setAdapter(musicAdapter);
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
        modeIV = findViewById(R.id.music_mode);
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

        //获取状态栏高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        Notification_height = getResources().getDimensionPixelSize(resourceId);

        seekBar.setProgress(0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.music_bottom_next:
                if (currentId==musicDataSize-1) {
                    Toast.makeText(this,"没有下一首了嗷~",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentId=currentId+1;
                playMusicOnService(currentId);
                break;
            case R.id.music_play:
                if(currentId==-1){
                    //没有播放音乐
                    Toast.makeText(this, "请选择播放音乐", Toast.LENGTH_SHORT).show();
                    return;
                }
                int state = myBinder.getMediaPlayState();
                if(state==1) {
                    myBinder.pauseMusic();
                    playIV.setImageResource(R.mipmap.music_play);
                } else if(state==0){
                    myBinder.playMusic();
                    playIV.setImageResource(R.mipmap.music_pause);
                } else if(state==2){
                    Toast.makeText(this, "播放结束了~", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.music_last:
                if (currentId==0) {
                    Toast.makeText(this,"已经是第一首了嗷~",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentId = currentId-1;
                playMusicOnService(currentId);
                break;
        }

    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
        handler.removeCallbacks(runnable);
        unbindService(serviceConnection);
    }
}