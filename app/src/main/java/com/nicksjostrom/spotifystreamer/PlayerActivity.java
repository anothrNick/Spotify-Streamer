package com.nicksjostrom.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;


public class PlayerActivity extends AppCompatActivity {

    boolean isCompleted = false;
    boolean isLoaded = false;

    TextView artistNameText;
    TextView albumNameText;
    TextView trackNameText;
    ImageView coverImage;

    ImageButton playButton;
    ImageButton pauseButton;
    SeekBar songNav;
    TextView progressView;
    TextView timeView;

    MediaPlayer player;

    String previewUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        //String trackId = intent.getStringExtra(TopTenTracksActivity.SELECTED_TRACK_ID);
        String trackName = intent.getStringExtra(TopTenTracksActivity.SELECTED_TRACK_NAME);
        String albumName = intent.getStringExtra(TopTenTracksActivity.SELECTED_ALBUM_NAME);
        String artistName = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        String albumImage = intent.getStringExtra(TopTenTracksActivity.SELECTED_ALBUM_IMAGE);
        previewUrl = intent.getStringExtra(TopTenTracksActivity.SELECTED_PREVIEW_URL);

        coverImage = (ImageView) findViewById(R.id.cover_art);
        artistNameText = (TextView) findViewById(R.id.artist_name);
        albumNameText = (TextView) findViewById(R.id.album_name);
        trackNameText = (TextView) findViewById(R.id.song_name);
        playButton = (ImageButton) findViewById(R.id.playBtn);
        pauseButton = (ImageButton) findViewById(R.id.pauseBtn);
        songNav = (SeekBar) findViewById(R.id.song_nav);
        progressView = (TextView) findViewById(R.id.progress);
        timeView = (TextView) findViewById(R.id.time);

        artistNameText.setText(artistName);
        albumNameText.setText(albumName);
        trackNameText.setText(trackName);

        Log.d("Preview URL: ", previewUrl);

        if(!albumImage.isEmpty()) {
            Picasso.with(this)
                    .load(albumImage)
                    .into(coverImage);
        }

        player = new MediaPlayer();

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(previewUrl);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //mediaPlayer.start();
                    isLoaded = true;
                    Log.d("", "loaded");

                    songNav.setMax(player.getDuration());
                    timeView.setText("0:" + (int) (player.getDuration() * .001));
                }
            });
            player.prepareAsync();

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    toggleBtnView();
                    isCompleted = true;
                }
            });

            //new PlayTrack().execute();
        }
        catch (IOException e) { Log.w("IO Exception: ", e.toString()); }
    }

    public void play(View view) {
        if(isCompleted){
            isCompleted = false;
            player.stop();
            player.reset();
        }
        if(isLoaded){
            toggleBtnView();
            player.start();
            new Seek().execute();
        }
    }

    public void pause(View view) {
        if(player.isPlaying()) player.pause();
        toggleBtnView();
    }

    public void toggleBtnView() {
        if(playButton.getVisibility() == View.VISIBLE) {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
        else {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
    }

    private class Seek extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... args){
            while(player.isPlaying()) {

                try{
                    Thread.sleep(100);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                onProgressUpdate();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            songNav.setProgress(player.getCurrentPosition());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prg = (int) (player.getCurrentPosition() * .001);
                    progressView.setText("0:" + (prg < 10 ? "0" : "") + prg);
                }
            });

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
        }
    }
}
