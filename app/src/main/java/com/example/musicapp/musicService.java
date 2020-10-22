package com.example.musicapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class musicService extends Service {
    private MediaPlayer mediaPlayer;
    private  String path;
    private int pausePosition;
    private MyBinder myBinder;
    public musicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static class MyBinder extends Binder {

    }
}
