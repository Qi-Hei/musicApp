package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

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
    }

}