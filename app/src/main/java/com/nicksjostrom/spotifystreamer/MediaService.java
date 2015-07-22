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

    /*Broadcast messages*/
    public static String PLAY = "com.nicksjostrom.spotifystreamer.PLAY";
    public static String PAUSE = "com.nicksjostrom.spotifystreamer.PAUSE";
    public static String STOP = "com.nicksjostrom.spotifystreamer.STOP";

    public static String GET_SEEK = "com.nicksjostrom.spotifystreamer.GET_SEEK";
    public static String UPDATE_SEEK = "com.nicksjostrom.spotifystreamer.UPDATE_SEEK";
    public static String CHANGE_SEEK = "com.nicksjostrom.spotifystreamer.CHANGE_SEEK";

    public static String GET_MAX = "com.nicksjostrom.spotifystreamer.GET_MAX";
    public static String UPDATE_MAX = "com.nicksjostrom.spotifystreamer.UPDATE_MAX";

    /* if the user presses start before song is loaded, play as soon as loaded*/
    private boolean startPlaying = false;

    private MediaPlayer mMediaPlayer = null;
    private final Handler handler = new Handler();
    private Intent intent;

    /* update seekbar in PlayerFragment every 200 ms*/
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if(mMediaPlayer != null) {
                sendSeek();
            }
            handler.postDelayed(this, 200); // 200 ms
        }
    };

    /* broadcast receiver to get messages*/
    private BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        intent = new Intent(UPDATE_SEEK);

        /* only listen for these messages */
        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAY);
        filter.addAction(PAUSE);
        filter.addAction(STOP);
        filter.addAction(GET_MAX);
        filter.addAction(GET_SEEK);
        filter.addAction(CHANGE_SEEK);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                /* tell service to play mediaplayer if we have loaded a song and it is not already playing*/
                if(action.equals(PLAY)) {
                    Log.i("service","play media");
                    if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                    else if(mMediaPlayer == null) {
                        startPlaying = true;
                    }
                }
                /* pause player */
                else if(action.equals(PAUSE)) {
                    Log.i("service","pause media");
                    if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }
                }
                else if(action.equals(STOP)) {
                    // stop player
                }
                /* get latest seek bar value */
                else if(action.equals(GET_SEEK)) {
                    sendSeek();
                }
                /* get max length (30s)*/
                else if(action.equals(GET_MAX)) {
                    sendMax();
                }
                /* change player location when user scrubs bar */
                else if(action.equals(CHANGE_SEEK)) {
                    int seek = intent.getIntExtra("seekto", 0);
                    if(mMediaPlayer != null) {
                        mMediaPlayer.seekTo(seek);
                    }
                }
            }
        };

        /* register our receiver */
        registerReceiver(receiver, filter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String url = intent.getStringExtra("previewUrl");
            handler.removeCallbacks(sendUpdatesToUI);
            handler.postDelayed(sendUpdatesToUI, 200);

            try {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        sendMax();
                        if(startPlaying)
                            mMediaPlayer.start();
                    }
                });
                mMediaPlayer.prepareAsync();

            } catch (IOException e) {
                Log.w("IO Exception: ", e.toString());
            }
        }

        return START_STICKY;
    }

    public void onDestroy() {
        /* clean up */
        Log.i("service","destroying service");
        handler.removeCallbacks(sendUpdatesToUI);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();

        /* remove our receiver */
        unregisterReceiver(receiver);
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        stopSelf();
    }

    private void sendMax() {
        if(mMediaPlayer != null) {
            Intent maxIntent = new Intent(UPDATE_MAX);
            maxIntent.putExtra("max", mMediaPlayer.getDuration());
            sendBroadcast(maxIntent);
        }
    }

    private void sendSeek() {
        try{
            intent.putExtra("playerPosition", mMediaPlayer.getCurrentPosition());
            sendBroadcast(intent);
        }
        catch(Exception e) {
            Log.w("service", e.toString());
        }
    }
}
