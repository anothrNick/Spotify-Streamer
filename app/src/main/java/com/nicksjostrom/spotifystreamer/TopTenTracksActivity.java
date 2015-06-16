package com.nicksjostrom.spotifystreamer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

public class TopTenTracksActivity extends Activity {

    TrackAdapter adapter;
    List<Track> trackList = new ArrayList<Track>();
    ListView trackListView;

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
        String artist_name = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        String artist_id = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_ID);

        // ensure we have an action bar
        assert getActionBar() != null;

        // set subtitle with artist name
        actionBarSetup(artist_name);

        // get listview from activity
        trackListView = (ListView) findViewById(R.id.trackList);
        // set listview adapter so we can update the UI
        trackListView.setAdapter(adapter);

        // get tracks spotify API call, if we haven't saved the instance
        if(savedInstanceState == null) getTracks(artist_id);
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
            ActionBar ab = getActionBar();
            ab.setSubtitle(sub);
        }
    }

    public void getTracks(String artist) {
        // spotify wrapper(s)
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        // http parameters for request. in this case, country code for top tracks
        Map<String, Object> parameters = new HashMap<String, Object>();
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
