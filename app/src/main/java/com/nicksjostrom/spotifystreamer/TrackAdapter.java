package com.nicksjostrom.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Nick on 6/13/2015.
 *
 * Adapter class for Track ListView
 */
public class TrackAdapter extends BaseAdapter {
    Context context;
    List<Track> tracks;
    Picasso mPicasso;
    LayoutInflater mInflater;

    TrackAdapter(Context context, List<Track> tracks) {
        this.context = context;
        this.tracks = tracks;

        this.mPicasso = Picasso.with(context);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        // check if we can re use the view
        if(view == null) {
            // create new view with artist_item.xml layout
            view = mInflater.inflate(R.layout.track_item, null);
            holder = new ViewHolder();

            // get views from layout
            holder.imageView = (ImageView) view.findViewById(R.id.trackImage);
            holder.textView = (TextView) view.findViewById(R.id.trackName);
            holder.albumTextView = (TextView) view.findViewById(R.id.albumName);

            // set tag so we can re use view later
            view.setTag(holder);
        }
        else {
            // get existing view
            holder = (ViewHolder) view.getTag();
        }

        // get artist at this position
        Track track = tracks.get(position);
        String image_url = "";

        // if we have any images, get URL
        if(track.album.images.size() > 0) {
            image_url = track.album.images.get(0).url;
        }

        if(!image_url.isEmpty()) {
            // use Picasso to load image into artist_item.xml image view
            mPicasso.load(image_url)
                    .into(holder.imageView);
        }

        // set textView to name of this artist
        holder.textView.setText(track.name);
        holder.albumTextView.setText(track.album.name);

        return view;
    }

    /*
    View Holder design pattern. Reduces use of findViewById()
     */
    private class ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView albumTextView;
    }
}
