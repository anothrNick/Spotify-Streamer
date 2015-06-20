package com.nicksjostrom.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class PlayerFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        Bundle args = getArguments();

        String trackName = args.getString(SearchArtistsActivity.SELECTED_TRACK_NAME);
        String albumName = args.getString(SearchArtistsActivity.SELECTED_ALBUM_NAME);
        String artistName = args.getString(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        String albumImage = args.getString(SearchArtistsActivity.SELECTED_ALBUM_IMAGE);
        previewUrl = args.getString(SearchArtistsActivity.SELECTED_PREVIEW_URL);

        coverImage = (ImageView) view.findViewById(R.id.cover_art);
        artistNameText = (TextView) view.findViewById(R.id.artist_name);
        albumNameText = (TextView) view.findViewById(R.id.album_name);
        trackNameText = (TextView) view.findViewById(R.id.song_name);

        playButton = (ImageButton) view.findViewById(R.id.playBtn);
        pauseButton = (ImageButton) view.findViewById(R.id.pauseBtn);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause();
            }
        });

        songNav = (SeekBar) view.findViewById(R.id.song_nav);
        progressView = (TextView) view.findViewById(R.id.progress);
        timeView = (TextView) view.findViewById(R.id.time);

        artistNameText.setText(artistName);
        albumNameText.setText(albumName);
        trackNameText.setText(trackName);

        Log.d("Preview URL: ", previewUrl);

        if(!albumImage.isEmpty()) {
            Picasso.with(getActivity())
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

        return view;
    }

    @Override
    public void onDestroy() {
        if(player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    public void play() {
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

    public void pause() {
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

    private class Seek extends AsyncTask<Void, Void, Void> {

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

            getActivity().runOnUiThread(new Runnable() {
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
