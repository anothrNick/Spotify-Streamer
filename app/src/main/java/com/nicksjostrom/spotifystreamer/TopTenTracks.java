package com.nicksjostrom.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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

public class TopTenTracks extends Activity {

    TrackAdapter adapter;
    List<Track> trackList = new ArrayList<Track>();
    ListView trackListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);

        //if (savedInstanceState != null) {
            // get existing TrackAdapter
           // adapter = (TrackAdapter)savedInstanceState.getSerializable("tracks");
       // }
        //else {
            // initialize TrackAdapter with empty list, updating this will update the listview (once notified of changes)
        //}

        // get intent and extra string parameters
        Intent intent = getIntent();
        String artist_name = intent.getStringExtra(SearchArtists.SELECTED_ARTIST_NAME);
        String artist_id = intent.getStringExtra(SearchArtists.SELECTED_ARTIST_ID);

        // ensure we have an action bar
        assert getActionBar() != null;
        // set title with artist name
        getActionBar().setTitle("Top 10 Tracks - " + artist_name);

        // initialize TrackAdapter with empty list, updating this will update the listview (once notified of changes)
        adapter = new TrackAdapter(this, trackList);
        // get listview from activity
        trackListView = (ListView) findViewById(R.id.trackList);
        // set listview adapter so we can update the UI
        trackListView.setAdapter(adapter);

        // create and execute new async task to get the top tracks
        new GetTracks().execute(artist_id);
    }

    /*
    AsyncTask to make API call. Keep process off of UI thread
     */
    private class GetTracks extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {
            // get the artist id passed to the async task
            String artist = params[0];

            // spotify wrapper(s)
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            // http parameters for request. in this case, country code for top tracks
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("country","us");

            // make api request
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

            return null;
        }
    }
}
