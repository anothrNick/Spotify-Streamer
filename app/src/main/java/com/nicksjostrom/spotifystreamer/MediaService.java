package com.nicksjostrom.spotifystreamer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Nick on 7/17/2015.
 */
public class MediaService extends Service implements MediaPlayer.OnCompletionListener  {
    MediaPlayer mMediaPlayer = null;

    public static String PLAY = "com.nicksjostrom.spotifystreamer.PLAY";
    public static String PAUSE = "com.nicksjostrom.spotifystreamer.PAUSE";
    public static String STOP = "com.nicksjostrom.spotifystreamer.STOP";

    public static String UPDATE_SEEK = "com.nicksjostrom.spotifystreamer.UPDATE_SEEK";
    public static String UPDATE_MAX = "com.nicksjostrom.spotifystreamer.UPDATE_MAX";

    private final Handler handler = new Handler();
    Intent intent;

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if(mMediaPlayer != null) {
                try{
                    intent.putExtra("playerPosition", mMediaPlayer.getCurrentPosition());
                    sendBroadcast(intent);
                }
                catch(Exception e) {
                    Log.w("service", e.toString());
                }
            }
            handler.postDelayed(this, 200); // 5 seconds
        }
    };

    private BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        intent = new Intent(UPDATE_SEEK);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAY);
        filter.addAction(PAUSE);
        filter.addAction(STOP);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(PLAY)) {
                    Log.i("service","play media");
                    if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                }
                else if(action.equals(PAUSE)) {
                    Log.i("service","pause media");
                    if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                }
                else if(action.equals(STOP)) {
                    // stop player
                }
            }
        };

        registerReceiver(receiver, filter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("previewUrl");
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 200);

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Intent maxIntent = new Intent(UPDATE_MAX);
                    maxIntent.putExtra("max", mMediaPlayer.getDuration());
                    sendBroadcast(maxIntent);
                }
            });
            mMediaPlayer.prepareAsync();

        }
        catch (IOException e) { Log.w("IO Exception: ", e.toString()); }

        return START_STICKY;
    }

    public void onDestroy() {
        Log.i("service","destroying service");
        handler.removeCallbacks(sendUpdatesToUI);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        unregisterReceiver(receiver);
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        stopSelf();
    }
}
