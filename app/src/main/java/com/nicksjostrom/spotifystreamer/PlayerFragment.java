package com.nicksjostrom.spotifystreamer;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import kaaes.spotify.webapi.android.models.Track;

public class PlayerFragment extends DialogFragment {

    private int mTrackIndex = 0;
    private boolean playing = false; // there can only be one
    private boolean  manualSeeking = false;
    private static boolean saveState = false; // there can only be one

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
    IntentFilter filter;
    private static Intent mediaService = null;

    private static BroadcastReceiver receiver = null;

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

        Log.d("playerFragment","onCreateView");

        if( getDialog() != null ) getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

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
        playButton.setEnabled(false);
        playButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

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

        songNav.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int newProgress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    newProgress = i;
                    int prg = (int) (newProgress * .001);
                    progressView.setText("0:" + (prg < 10 ? "0" : "") + prg);
                    manualSeeking = b;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                manualSeeking = false;
                Intent seekIntent = new Intent(MediaService.CHANGE_SEEK);
                seekIntent.putExtra("seekto", newProgress);
                getActivity().sendBroadcast(seekIntent);
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(MediaService.UPDATE_MAX)) {
                    int duration = intent.getIntExtra("max", 0);

                    timeView.setText("0:" + (int) (duration * .001));
                    songNav.setMax(duration);
                    playButton.setEnabled(true);
                    playButton.getBackground().setColorFilter(null);
                } else if (action.equals(MediaService.UPDATE_SEEK)) {
                    int position = intent.getIntExtra("playerPosition", 0);
                    int prg = (int) (position * .001);

                    if(!manualSeeking) {
                        songNav.setProgress(position);
                        progressView.setText("0:" + (prg < 10 ? "0" : "") + prg);
                    }
                }
            }
        };

        filter = new IntentFilter();
        filter.addAction(MediaService.UPDATE_MAX);
        filter.addAction(MediaService.UPDATE_SEEK);

        // restore player position
        if( savedInstanceState != null ){
            //savedPosition = savedInstanceState.getInt("position");
            mTrackIndex = savedInstanceState.getInt("trackIndex");
            playing = savedInstanceState.getBoolean("playing");

            Log.d("playerFragment", "playing: " + playing);
            if(playing) {
                Log.d("playerFragment", "playing");
                toggleBtnView();
                this.getActivity().registerReceiver(receiver, filter);
            }
            getMax();
            getSeek();
        }

        // load track at a specific position
        loadTrack(savedPosition);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Log.d("playerfragment","onSaveInstanceState");

        saveState = true;
        savedInstanceState.putInt("trackIndex", mTrackIndex);
        savedInstanceState.putBoolean("playing", playing);
    }

    @Override
    public void onDestroy() {
        Log.d("playerfragment","onDestroy");
        try {
            this.getActivity().unregisterReceiver(receiver);
        } catch ( IllegalArgumentException e ) {
            Log.w("playerfragment", e.getMessage());
        }
        // make sure we clean up player resources before this fragment is destroyed
        stop();
        super.onDestroy();
    }

    /**
     * Stop and release player resources.
     */
    private void stop() {
        playing = false;
    }

    private void getSeek() {
        Intent intent = new Intent();
        intent.setAction(MediaService.GET_SEEK);
        this.getActivity().sendBroadcast(intent);
    }

    private void getMax() {
        Intent intent = new Intent();
        intent.setAction(MediaService.GET_MAX);
        this.getActivity().sendBroadcast(intent);
    }

    /**
     * Play player if a track is loaded, if not loaded, flag to play as soon as loading is complete.
     */
    private void play() {
        playing = true;
        toggleBtnView();

        Intent intent = new Intent();
        intent.setAction(MediaService.PLAY);
        this.getActivity().sendBroadcast(intent);
    }

    /**
     * Pause player if playing. Update buttons
     */
    private void pause() {
        if(playing) {
            toggleBtnView();
            playing = false;
        }

        Intent intent = new Intent();
        intent.setAction(MediaService.PAUSE);
        this.getActivity().sendBroadcast(intent);
    }

    /**
     * Increment track index. If we player is currently playing, stop and reset seekbar
     * load next track
     */
    private void forward(){
        mTrackIndex ++;

        pause();

        playButton.setEnabled(false);
        playButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

        if( mTrackIndex > (SearchArtistsActivity.trackList.size()-1)) mTrackIndex = 0;

        loadTrack(0);
    }

    /**
     * Decrement track index. If we player is currently playing, stop and reset seekbar
     * load next track
     */
    private void previous(){
        mTrackIndex --;

        pause();

        playButton.setEnabled(false);
        playButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);

        if( mTrackIndex < 0 ) mTrackIndex = (SearchArtistsActivity.trackList.size()-1);

        loadTrack(0);
    }

    /**
     * Load track at mTrackIndex. Sets position for seekbar (primarily for orientation change / saved state).
     * @param position
     */
    private void loadTrack(int position) {
        Log.d("tag","loadTrack()");
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

        if(!saveState) {

            if (mediaService != null) {
                //if(receiver != null) {
                    //this.getActivity().unregisterReceiver(receiver);
                //}
                this.getActivity().stopService(mediaService);
            }
            mediaService = new Intent(this.getActivity(), MediaService.class);
            mediaService.putExtra("previewUrl", previewUrl);
            this.getActivity().startService(mediaService);
            this.getActivity().registerReceiver(receiver, filter);
        }
        else {
            saveState = false;
        }
    }

    /**
     * Toggle play / pause button visibility
     */
    private void toggleBtnView() {
        if(playButton.getVisibility() == View.VISIBLE) {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
        else {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }
    }
}
