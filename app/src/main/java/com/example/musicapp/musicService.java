package com.example.musicapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;

public class musicService extends Service {
    private MediaPlayer mediaPlayer;
    private  String path;
    private int pausePosition;
    private MyBinder myBinder;
    private String music_singer, music_song;
    private int music_Id,music_duration;
    private List<LocalMusicBean> mDatas;
    private int musicDataSize;
    private boolean playState;
    private int playModule;



    public musicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mediaPlayer!=null){
                    myBinder.play_next();
                    //playState=true;
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return true;
            }
        });

        //初始化信息
        music_Id=-1;
        music_singer="";
        music_song="";

        pausePosition = 0;
        playState=false;
        playModule=0;
        myBinder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public boolean setData(List<LocalMusicBean> musicBeanList){
            Log.d("MusicService----0----", "setData");
            mDatas = musicBeanList;
            musicDataSize = mDatas.size();
            return true;
        }

        public void setMusic(int id){
            Log.d("MusicService----1----", "setMusic");
            //重置播放
            mediaPlayer.reset();
            //进度记录清零
            pausePosition=0;
            //设置新的音乐
            music_Id=id;
            LocalMusicBean musicBean = mDatas.get(music_Id);
            try {
                //设置信息
                path = musicBean.getPath();
                mediaPlayer.setDataSource(path);
                music_song = musicBean.getSong();
                music_singer = musicBean.getSinger();
                music_duration = (int)musicBean.getDuration();
                //调用意图服务，更新activity内容
                Intent intentInfo = new Intent("com.example.musicApp.intentService");
                intentInfo.setPackage(getPackageName());
                intentInfo.putExtra("music_id", music_Id);
                intentInfo.putExtra("music_song", music_song);
                intentInfo.putExtra("music_singer", music_singer);
                intentInfo.putExtra("music_duration", music_duration);
                startService(intentInfo);
                myBinder.playMusic();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void playMusic() {
            /* 播放音乐的函数*/
            Log.d("MusicService----2----", "playMusic");
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                if (pausePosition == 0) {
                    try {
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        playState=false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //从暂停到播放
                    mediaPlayer.seekTo(pausePosition);
                    mediaPlayer.start();
                    playState=false;
                }
            }
        }

      //暂停音乐
        public void pauseMusic() {
            Log.d("MusicService----3----", "pauseMusic");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pausePosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
        }

        //下一首
        public void play_next() {
            if(playModule==2) {
                //判断是否是最后一首
                if (music_Id >= musicDataSize - 1) {
                    //从第一首开始播放
                    music_Id = -1;
                }
                music_Id = music_Id + 1;

            }
            else if(playModule==0){
                Random random = new Random();
                music_Id = random.nextInt(musicDataSize-1);
            }
            setMusic(music_Id);
            playMusic();
            playState=false;
        }

        //上一首
        public void play_last() {
            music_Id = music_Id-1;
            if(music_Id==-1) return;
            setMusic(music_Id);
            playMusic();
        }

        //停止播放
        public void stopMusic() {
            Log.d("MusicService----4----", "stopMusic");
            if (mediaPlayer != null) {
                pausePosition = 0;
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                mediaPlayer.stop();
            }
        }

        //返回播放进度
        public int getPlayPosition() {
            Log.d("MusicService----5----", "getPlayPosition");
            if(mediaPlayer!=null) {
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        //设置播放进度
        public void seekToPosition(int msec) {
            Log.d("MusicService----6----", "seekToPosition");
            if(mediaPlayer!=null) {
                if(!mediaPlayer.isPlaying()) myBinder.playMusic();
                playState=false;
                mediaPlayer.seekTo(msec);
            }
        }

        //返回播放状态
        public int getMediaPlayState() {
            Log.d("MusicService----7----", "getMediaPlayState");
            if(mediaPlayer!=null) {
                if(mediaPlayer.isPlaying()) return 1;
                else if(playState) return 2;
                else return 0;
            }
            else return 0;
        }

        //获取歌手
        public String getMusicSinger(){
            Log.d("MusicService----8----", "getMusicSinger");
            if(mediaPlayer!=null) return music_singer;
            else return "";
        }

        //获取歌曲
        public String getMusicSong() {
            Log.d("MusicService----9----", "getMusicSong");
            if(mediaPlayer!=null) return music_song;
            else return "";
        }

        //获取id
        public int getMusicId(){
            Log.d("MusicService----10----", "getMusicId");
            if(mediaPlayer!=null) return music_Id;
            else return -1;
        }

        //获取歌曲时长
        public int getMusicDuration(){
            Log.d("MusicService----11----", "getMusicDuration");
            if(mediaPlayer!=null) return mediaPlayer.getDuration();
            else return -1;
        }

        //设置播放模式 0:随机 1：单曲 2：列表
        public void setPlayModule(){
            if(playModule==0) playModule=1;
            else if(playModule==1) playModule=2;
            else playModule=0;
        }

        //获取播放模式
        public int getPlayModule(){
            return playModule;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            pausePosition = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
        }
    }
}
