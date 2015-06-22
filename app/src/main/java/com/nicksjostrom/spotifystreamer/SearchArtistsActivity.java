package com.nicksjostrom.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SearchArtistsActivity extends AppCompatActivity  {

    public static final String SELECTED_ARTIST_NAME = "com.nicksjostrom.spotifystreamer.SELECTED_ARTIST_NAME";
    public static final String SELECTED_ARTIST_ID = "com.nicksjostrom.spotifystreamer.SELECTED_ARTIST_ID";

    public static final String SELECTED_TRACK_ID = "com.nicksjostrom.spotifystreamer.SELECTED_TRACK_ID";
    public static final String SELECTED_TRACK_NAME = "com.nicksjostrom.spotifystreamer.SELECTED_TRACK_NAME";
    public static final String SELECTED_ALBUM_NAME = "com.nicksjostrom.spotifystreamer.SELECTED_ALBUM_NAME";
    public static final String SELECTED_ALBUM_IMAGE = "com.nicksjostrom.spotifystreamer.SELECTED_ALBUM_IMAGE";
    public static final String SELECTED_PREVIEW_URL = "com.nicksjostrom.spotifystreamer.SELECTED_PREVIEW_URL";

    public static boolean mDualPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artists);

        if(findViewById(R.id.track_list_container) != null) {
            mDualPane = true;
        }
        //FragmentManager fm = getFragmentManager();

        //FragmentTransaction ft = fm.beginTransaction();
        //ft.add(R.id.search_artist_container, new SearchArtistsFragment()).addToBackStack("search").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_artists, menu);
        return true;
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


}
