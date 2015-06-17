package com.nicksjostrom.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;


public class PlayerActivity extends AppCompatActivity {

    TextView artistNameText;
    TextView albumNameText;
    TextView trackNameText;
    ImageView coverImage;

    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        String trackId = intent.getStringExtra(TopTenTracksActivity.SELECTED_TRACK_ID);
        String trackName = intent.getStringExtra(TopTenTracksActivity.SELECTED_TRACK_NAME);
        String albumName = intent.getStringExtra(TopTenTracksActivity.SELECTED_ALBUM_NAME);
        String artistName = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        String albumImage = intent.getStringExtra(TopTenTracksActivity.SELECTED_ALBUM_IMAGE);
        String previewUrl = intent.getStringExtra(TopTenTracksActivity.SELECTED_PREVIEW_URL);

        coverImage = (ImageView) findViewById(R.id.cover_art);
        artistNameText = (TextView) findViewById(R.id.artist_name);
        albumNameText = (TextView) findViewById(R.id.album_name);
        trackNameText = (TextView) findViewById(R.id.song_name);

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
            player.prepare();
            player.start();
        } catch(IOException e) {
            Log.w("IO Exception: ", e.toString());
        }

    }
}
