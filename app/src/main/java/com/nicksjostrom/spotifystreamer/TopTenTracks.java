package com.nicksjostrom.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TopTenTracks extends Activity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten_tracks);

        textView = (TextView) findViewById(R.id.textView);

        Intent intent = getIntent();
        String name = intent.getStringExtra(SearchArtists.SELECTED_ARTIST_NAME);

        textView.setText(name);
    }
}
