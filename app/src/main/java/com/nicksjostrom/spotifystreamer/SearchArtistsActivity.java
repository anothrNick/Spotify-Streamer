package com.nicksjostrom.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchArtistsActivity extends AppCompatActivity  {

    public static final String SELECTED_ARTIST_NAME = "com.nicksjostrom.spotifystreamer.SELECTED_ARTIST_NAME";
    public static final String SELECTED_ARTIST_ID = "com.nicksjostrom.spotifystreamer.SELECTED_ARTIST_ID";

    ArtistAdapter adapter;
    List<Artist> artistList = new ArrayList<>();
    ListView artistListView;
    SearchView searchView;

    SpotifyApi api;
    SpotifyService spotify;

    /*
    Runnable object to call on UI thread when Spotify API callbacks are made
     */
    final Runnable postExecute = new Runnable() {
        @Override
        public void run() {
            // This code will always run on the UI thread, therefore is safe to modify UI elements.
            // notify change of artists, this will update the listview
            adapter.notifyDataSetChanged();
            if (artistList.size() == 0) {
                // if artistList contains 0 artists, toast
                Toast.makeText(SearchArtistsActivity.this, "No artists found. Please refine your search.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artists);

        // init spotify API
        api = new SpotifyApi();
        spotify = api.getService();

        if (savedInstanceState != null) {
            // get existing ArtistAdapter from saved instance
            adapter = savedInstanceState.getParcelable("artists");
        }
        else {
            // initialize ArtistAdapter with empty list, updating this will update the listview (once notified of changes)
            adapter = new ArtistAdapter(this, artistList);
        }

        // get listview from activity
        artistListView = (ListView) findViewById(R.id.artistList);
        // set listview adapter so we can update the UI
        artistListView.setAdapter(adapter);

        // set onclick listener so we can click on an artist and view their top 10
        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get artist from listview
                Artist artist = (Artist) artistListView.getItemAtPosition(position);

                Log.d("id: ", ""+artist.id);
                Log.d("name: ", ""+artist.name);

                // create new intent to TopTenTracks activity for this artist
                Intent intent = new Intent(SearchArtistsActivity.this, TopTenTracksActivity.class);
                // place extra data so we know what artist to display
                intent.putExtra(SELECTED_ARTIST_NAME, artist.name);
                intent.putExtra(SELECTED_ARTIST_ID, artist.id);
                // start activity
                startActivity(intent);
            }
        });

        // initialize search field
        searchView = (SearchView) findViewById(R.id.search_artist);

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        // Start searching on submit
                        // new search, clear artist list
                        artistList.clear();

                        // notify list change
                        adapter.notifyDataSetChanged();

                        // getArtists method with search string. calls spotify api
                        getArtists(query);
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
            });
/*
        searchText = (EditText) findViewById(R.id.search);

        // create event listener for 'Done' button to search
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    // new search, clear artist list
                    artistList.clear();

                    // notify list change
                    adapter.notifyDataSetChanged();

                    // getArtists method with search string. calls spotify api
                    getArtists(searchText.getText().toString());

                    return true;
                }
                return false;
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_artists, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable("artists", adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // get artist spotify api call
    public void getArtists(String artist) {

        // callback is async
        spotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                // get list of found artists
                List<Artist> artists = artistsPager.artists.items;
                // clear adapter's list
                artistList.clear();
                // add all artists to adapter list
                artistList.addAll(artists);

                Log.d("success. List: ", artistList.toString());

                runOnUiThread(postExecute);
            }

            @Override
            public void failure(RetrofitError error) {
                // toast on failure
                artistList.clear();

                Log.d("No artists found...","");

                runOnUiThread(postExecute);
            }
        });
    }
}
