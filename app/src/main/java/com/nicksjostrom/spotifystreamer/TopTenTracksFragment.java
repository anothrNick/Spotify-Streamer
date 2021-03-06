package com.nicksjostrom.spotifystreamer;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTenTracksFragment extends Fragment {

    TrackAdapter adapter;
    ListView trackListView;

    String artistName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        if (savedInstanceState != null) {
            // get existing TrackAdapter
            adapter = savedInstanceState.getParcelable("tracks");
        }
        else {
            SearchArtistsActivity.trackList.clear();
            // initialize TrackAdapter with empty list, updating this will update the listview (once notified of changes)
            adapter = new TrackAdapter(getActivity(), SearchArtistsActivity.trackList);
        }

        // get intent and extra string parameters
        //Intent intent = getIntent();
        Bundle args = getArguments();
        artistName = args.getString(SearchArtistsActivity.SELECTED_ARTIST_NAME);
        String artistId = args.getString(SearchArtistsActivity.SELECTED_ARTIST_ID);

        // get listview from activity
        trackListView = (ListView) view.findViewById(R.id.trackList);
        // set listview adapter so we can update the UI
        trackListView.setAdapter(adapter);

        // set on click listener for tracks
        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /* if user is on a tablet, start new fragment in container*/
                if(SearchArtistsActivity.mDualPane) {
                    Bundle bundle = new Bundle();

                    bundle.putInt(SearchArtistsActivity.TRACK_INDEX, i);

                    bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_NAME, artistName);

                    PlayerFragment player = new PlayerFragment();
                    player.setArguments(bundle);

                    FragmentManager fm = getFragmentManager();
                    player.show(fm, "player");
                }
                /* user is on mobile phone, start new activity to create fragment*/
                else {
                    Intent intent = new Intent(getActivity(), PlayerActivity.class);

                    intent.putExtra(SearchArtistsActivity.TRACK_INDEX, i);

                    intent.putExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME, artistName);

                    startActivity(intent);
                }
            }
        });

        // get tracks spotify API call, if we haven't saved the instance
        if(savedInstanceState == null) getTracks(artistId);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // save adapter
        savedInstanceState.putParcelable("tracks", adapter);
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
                SearchArtistsActivity.trackList.clear();
                // add all
                SearchArtistsActivity.trackList.addAll(tracks.tracks);

                // update list view adapter on UI thread
                getActivity().runOnUiThread(new Runnable() {
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
