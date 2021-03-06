package com.nicksjostrom.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    Fragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if( savedInstanceState == null ) {
            Intent intent = getIntent();

            Bundle bundle = new Bundle();
            bundle.putInt(SearchArtistsActivity.TRACK_INDEX, intent.getIntExtra(SearchArtistsActivity.TRACK_INDEX, 0));
            bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_NAME, intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME));

            PlayerFragment frag = new PlayerFragment();
            frag.setArguments(bundle);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.player_container, frag).addToBackStack("player").commit();
        }
    }

}
