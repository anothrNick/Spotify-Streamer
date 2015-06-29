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

    int mTrackPosition = 0;
    int mTrackIndex = 0;
    boolean startNew = true;
    boolean attemptedToPlay = false;
    boolean isLoaded = false;
    static boolean paused = false; // there can only be one
    static boolean saveState = false; // there can only be one

    String artistName;

    TextView progressView;
    TextView timeView;
    TextView artistNameText;
    TextView albumNameText;
    TextView trackNameText;
    ImageView coverImage;

    ImageButton playButton;
    ImageButton pauseButton;
    ImageButton forwardButton;
    ImageButton previousButton;

    SeekBar songNav;
    Seek seekerTask;
    MediaPlayer player;

    @Override
    public void onPause() {

        Log.d("tag","onPause");
        super.onPause();
    }

    @Override
    public void onStop() {

        Log.d("tag","onStop");
        super.onStop();
    }

    @Override
    public void onStart() {

        Log.d("tag","onStart");
        Log.d("tag","artist " + artistName);
        super.onStart();
    }

    @Override
    public void onResume() {

        Log.d("tag","onResume");
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d("tag","onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("tag","onCreateView");

        if( getDialog() != null )getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        Bundle args = getArguments();

        mTrackIndex = args.getInt(SearchArtistsActivity.TRACK_INDEX);

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

        int savedPosition = 0;

        // restore player position
        if( savedInstanceState != null ){
            saveState = false;
            if( paused ) {
                paused = false;
                attemptedToPlay = true;
                savedPosition = savedInstanceState.getInt("position");
            }
        }

        // load track at a specific position
        loadTrack(savedPosition);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Log.d("tag","onSaveInstanceState");

        saveState = true;

        // check that player has been initialized
        if( player != null ) {
            // put position of player
            savedInstanceState.putInt("position", player.getCurrentPosition());
            // if the player is currently playing, flag so we continue to play after instance is restored
            if( player.isPlaying() ) {
                paused = true;
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("tag","onDestroy");
        // make sure we clean up player resources before this fragment is destroyed
        stop();
        super.onDestroy();
    }

    /**
     * Done with this track. Pause and seek to 0 on seekbar
     */
    public void done() {
        if(player != null) {
            pause();
            player.seekTo(0);
        }
    }

    /**
     * Stop and release player resources.
     */
    public void stop() {
        if(player != null) {
            if(player.isPlaying()) {
                seekerTask.cancel(true);
                player.stop();
            }
            player.reset();
            player.release();
            player = null;
        }
    }

    /**
     * Play player if a track is loaded, if not loaded, flag to play as soon as loading is complete.
     */
    public void play() {
        if(isLoaded && player != null){
            attemptedToPlay = false;
            toggleBtnView();
            if(mTrackPosition != 0) player.seekTo(mTrackPosition);
            player.start();
            seekerTask = (Seek) new Seek().execute();
        }
        else {
            attemptedToPlay = true;
        }
    }

    /**
     * Pause player if playing. Update buttons
     */
    public void pause() {
        if(player != null && player.isPlaying()) {
            player.pause();
            toggleBtnView();
        }
    }

    /**
     * Increment track index. If we player is currently playing, stop and reset seekbar
     * load next track
     */
    public void forward(){
        mTrackIndex ++;
        if( mTrackIndex > (SearchArtistsActivity.trackList.size()-1)) mTrackIndex = 0;
        if(player != null && player.isPlaying()) {
            done();
            attemptedToPlay = true;
        }
        loadTrack(0);
    }

    /**
     * Decrement track index. If we player is currently playing, stop and reset seekbar
     * load next track
     */
    public void previous(){
        mTrackIndex --;
        if( mTrackIndex < 0 ) mTrackIndex = (SearchArtistsActivity.trackList.size()-1);
        if(player != null && player.isPlaying()) {
            done();
            attemptedToPlay = true;
        }
        loadTrack(0);
    }

    /**
     * Load track at mTrackIndex. Sets position for seekbar (primarily for orientation change / saved state).
     * @param position
     */
    public void loadTrack(int position) {
        Log.d("tag","loadTrack()");
        mTrackPosition = position;
        Track track = SearchArtistsActivity.trackList.get(mTrackIndex);
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

    /**
     * Toggle play / pause button visibility
     */
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

    /**
     * Seek AsyncTask. Updates seekbar as player is playing
     */
    private class Seek extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args){
            while(!saveState && player.isPlaying()) {

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

            if(!saveState) {
                songNav.setProgress(player.getCurrentPosition());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int prg = (int) (player.getCurrentPosition() * .001);
                        progressView.setText("0:" + (prg < 10 ? "0" : "") + prg);
                    }
                });
            }

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
        }
    }

}
