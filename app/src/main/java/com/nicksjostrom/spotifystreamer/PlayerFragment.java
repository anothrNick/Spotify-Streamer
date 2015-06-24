package com.nicksjostrom.spotifystreamer;

import android.app.DialogFragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerFragment extends DialogFragment {

    boolean attemptedToPlay = false;
    boolean isLoaded = false;
    boolean saveState = false;

    TextView artistNameText;
    TextView albumNameText;
    TextView trackNameText;
    ImageView coverImage;

    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton forwardButton;
    ImageButton previousButton;

    SeekBar songNav;
    TextView progressView;
    TextView timeView;

    MediaPlayer player;

    int trackIndex = 0;

    String artistName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(savedInstanceState != null){
            saveState = false;
        }

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        Bundle args = getArguments();

        trackIndex = args.getInt(SearchArtistsActivity.TRACK_INDEX);

        artistName = args.getString(SearchArtistsActivity.SELECTED_ARTIST_NAME);

        coverImage = (ImageView) view.findViewById(R.id.cover_art);
        artistNameText = (TextView) view.findViewById(R.id.artist_name);
        albumNameText = (TextView) view.findViewById(R.id.album_name);
        trackNameText = (TextView) view.findViewById(R.id.song_name);

        playButton = (ImageButton) view.findViewById(R.id.playBtn);
        pauseButton = (ImageButton) view.findViewById(R.id.pauseBtn);
        forwardButton = (ImageButton) view.findViewById(R.id.forwardBtn);
        previousButton = (ImageButton) view.findViewById(R.id.previousBtn);

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forward();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous();
            }
        });

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

        loadTrack();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveState = true;
    }

    @Override
    public void onDestroy() {
        if(player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    public void done() {
        pause();
        player.seekTo(0);
    }

    public void play() {
        if(isLoaded){
            attemptedToPlay = false;
            toggleBtnView();
            player.start();
            new Seek().execute();
        }
        else {
            attemptedToPlay = true;
        }
    }

    public void pause() {
        if(player.isPlaying()) player.pause();
        toggleBtnView();
    }

    public void forward(){
        trackIndex ++;
        if( trackIndex > (SearchArtistsActivity.trackList.size()-1)) trackIndex = 0;
        if(player != null && player.isPlaying()) {
            done();
            attemptedToPlay = true;
        }
        loadTrack();
    }

    public void previous(){
        trackIndex --;
        if( trackIndex < 0 ) trackIndex = (SearchArtistsActivity.trackList.size()-1);
        if(player != null && player.isPlaying()) {
            done();
            attemptedToPlay = true;
        }
        loadTrack();
    }

    public void loadTrack() {
        Track track = SearchArtistsActivity.trackList.get(trackIndex);
        String trackName = track.name;
        String albumName = track.album.name;
        String albumImage = "";
        String previewUrl = track.preview_url;

        if(track.album.images.size() > 0){
            albumImage = track.album.images.get(0).url;
        }

        artistNameText.setText(artistName);
        albumNameText.setText(albumName);
        trackNameText.setText(trackName);

        if(!albumImage.isEmpty()) {
            Picasso.with(getActivity())
                    .load(albumImage)
                    .into(coverImage);
        }

        if(player != null) {
            player.stop();
            player.reset();
            player.release();
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

                    if(attemptedToPlay){
                        play();
                    }
                }
            });
            player.prepareAsync();

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    toggleBtnView();
                    done();
                }
            });


        }
        catch (IOException e) { Log.w("IO Exception: ", e.toString()); }
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
