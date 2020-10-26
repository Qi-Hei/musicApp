package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

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
    private int currentId = 2;
    private musicService.MyBinder myBinder;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}