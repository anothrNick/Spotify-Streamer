package com.nicksjostrom.spotifystreamer;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TopTenTracksActivity extends AppCompatActivity {

    String artistName;
    String artistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);

        // get intent and extra string parameters
        Intent intent = getIntent();
        artistName = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        artistId = intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_ID);

        actionBarSetup(artistName);

        Bundle bundle = new Bundle();
        bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_NAME, artistName);
        bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_ID, artistId);

        TopTenTracksFragment frag = new TopTenTracksFragment();
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.track_list_container, frag).addToBackStack("topten").commit();
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
}
