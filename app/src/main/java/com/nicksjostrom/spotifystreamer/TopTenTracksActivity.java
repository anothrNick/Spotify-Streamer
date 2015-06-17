package com.nicksjostrom.spotifystreamer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTenTracksActivity extends AppCompatActivity {

    public static final String SELECTED_TRACK_ID = "com.nicksjostrom.spotifystreamer.SELECTED_TRACK_ID";
    public static final String SELECTED_TRACK_NAME = "com.nicksjostrom.spotifystreamer.SELECTED_TRACK_NAME";
    public static final String SELECTED_ALBUM_NAME = "com.nicksjostrom.spotifystreamer.SELECTED_ALBUM_NAME";
    public static final String SELECTED_ALBUM_IMAGE = "com.nicksjostrom.spotifystreamer.SELECTED_ALBUM_IMAGE";
    public static final String SELECTED_PREVIEW_URL = "com.nicksjostrom.spotifystreamer.SELECTED_PREVIEW_URL";

    TrackAdapter adapter;
    List<Track> trackList = new ArrayList<>();
    ListView trackListView;

    String artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);

        if (savedInstanceState != null) {
           // get existing TrackAdapter
           adapter = savedInstanceState.getParcelable("tracks");
        }
        else {
            // initialize TrackAdapter with empty list, updating this will update the listview (once notified of changes)
            adapter = new TrackAdapter(this, trackList);
        }

        // get intent and extra string parameters
        Intent intent = getIntent();
        artistName = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        String artistId = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_ID);

        // set subtitle with artist name
        actionBarSetup(artistName);

        // get listview from activity
        trackListView = (ListView) findViewById(R.id.trackList);
        // set listview adapter so we can update the UI
        trackListView.setAdapter(adapter);

        // set on click listener for tracks
        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Track track = (Track) trackListView.getItemAtPosition(i);
                String image_url = "";
                String preview_url = "";

                if(track.album.images.size() > 0)
                    image_url = track.album.images.get(0).url;

                preview_url = track.preview_url;

                Log.d("id: ", track.id);
                Log.d("track: ", track.name);
                Log.d("album: ", track.album.name);

                Intent intent = new Intent(TopTenTracksActivity.this, PlayerActivity.class);

                intent.putExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME, artistName);
                intent.putExtra(SELECTED_TRACK_ID, track.id);
                intent.putExtra(SELECTED_TRACK_NAME, track.name);
                intent.putExtra(SELECTED_ALBUM_NAME, track.album.name);
                intent.putExtra(SELECTED_ALBUM_IMAGE, image_url);
                intent.putExtra(SELECTED_PREVIEW_URL, preview_url);

                startActivity(intent);
            }
        });

        // get tracks spotify API call, if we haven't saved the instance
        if(savedInstanceState == null) getTracks(artistId);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // save adapter
        savedInstanceState.putParcelable("tracks", adapter);
    }

    /**
     * Sets the Action Bar for new Android versions.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void actionBarSetup(String sub) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setSubtitle(sub);
        }
    }

    public void getTracks(String artist) {
        // spotify wrapper(s)
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        // http parameters for request. in this case, country code for top tracks
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country","us");

        // make api request, callback is async
        spotify.getArtistTopTrack(artist, parameters, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                // we have tracks, clear current
                trackList.clear();
                // add all
                trackList.addAll(tracks.tracks);

                // update list view adapter on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Fail ", error.getMessage());
            }
        });
    }
}
