package com.nicksjostrom.spotifystreamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();

        Bundle bundle = new Bundle();
        bundle.putString(SearchArtistsActivity.SELECTED_TRACK_NAME, intent.getStringExtra(SearchArtistsActivity.SELECTED_TRACK_NAME));
        bundle.putString(SearchArtistsActivity.SELECTED_ALBUM_NAME, intent.getStringExtra(SearchArtistsActivity.SELECTED_ALBUM_NAME));
        bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_NAME, intent.getStringExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME));
        bundle.putString(SearchArtistsActivity.SELECTED_ALBUM_IMAGE, intent.getStringExtra(SearchArtistsActivity.SELECTED_ALBUM_IMAGE));
        bundle.putString(SearchArtistsActivity.SELECTED_PREVIEW_URL, intent.getStringExtra(SearchArtistsActivity.SELECTED_PREVIEW_URL));

        PlayerFragment frag = new PlayerFragment();
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.player_container, frag).addToBackStack("player").commit();
    }
}
