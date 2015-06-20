package com.nicksjostrom.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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


public class SearchArtistsFragment extends Fragment {

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
                Toast.makeText(getActivity(), "No artists found. Please refine your search.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_artists, container, false);

        // init spotify API
        api = new SpotifyApi();
        spotify = api.getService();

        if (savedInstanceState != null) {
            // get existing ArtistAdapter from saved instance
            adapter = savedInstanceState.getParcelable("artists");
        }
        else {
            // initialize ArtistAdapter with empty list, updating this will update the listview (once notified of changes)
            adapter = new ArtistAdapter(getActivity(), artistList);
        }

        // get listview from activity
        artistListView = (ListView) view.findViewById(R.id.artistList);
        // set listview adapter so we can update the UI
        artistListView.setAdapter(adapter);

        // set onclick listener so we can click on an artist and view their top 10
        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get artist from listview
                Artist artist = (Artist) artistListView.getItemAtPosition(position);

                //Bundle bundle = new Bundle();
                //bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_NAME, artist.name);
                //bundle.putString(SearchArtistsActivity.SELECTED_ARTIST_ID, artist.id);

                //TopTenTracksFragment frag = new TopTenTracksFragment();
                //frag.setArguments(bundle);

                //FragmentManager fm = getFragmentManager();
                //FragmentTransaction ft = fm.beginTransaction();
                //ft.replace(R.id.search_artist_container, frag).addToBackStack("topten").commit();

                // create new intent to TopTenTracks activity for this artist
                Intent intent = new Intent(getActivity(), TopTenTracksActivity.class);
                // place extra data so we know what artist to display
                intent.putExtra(SearchArtistsActivity.SELECTED_ARTIST_NAME, artist.name);
                intent.putExtra(SearchArtistsActivity.SELECTED_ARTIST_ID, artist.id);
                // start activity
                startActivity(intent);
            }
        });

        // initialize search field
        searchView = (SearchView) view.findViewById(R.id.search_artist);

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

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable("artists", adapter);
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

                getActivity().runOnUiThread(postExecute);
            }

            @Override
            public void failure(RetrofitError error) {
                // toast on failure
                artistList.clear();

                Log.d("No artists found...","");

                getActivity().runOnUiThread(postExecute);
            }
        });
    }
}
