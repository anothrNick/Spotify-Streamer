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

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Nick on 6/12/2015.
 *
 * ArtistAdapter class is a custom adapter to display an artist's name and image in a listview
 *
 *
 */
public class ArtistAdapter extends BaseAdapter {
    Context context;
    List<Artist> artists;
    Picasso mPicasso;
    LayoutInflater mInflater;

    ArtistAdapter(Context context, List<Artist> artists) {
        this.context = context;
        this.artists = artists;

        this.mPicasso = Picasso.with(context);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int position) {
        return artists.get(position);
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
            view = mInflater.inflate(R.layout.artist_item, null);
            holder = new ViewHolder();

            // get views from layout
            holder.imageView = (ImageView) view.findViewById(R.id.artistImage);
            holder.textView = (TextView) view.findViewById(R.id.artistText);

            // set tag so we can re use view later
            view.setTag(holder);
        }
        else {
            // get existing view
            holder = (ViewHolder) view.getTag();
        }

        // get artist at this position
        Artist artist = artists.get(position);
        String image_url = "";

        // if we have any images, get URL
        if(artist.images.size() > 0) {
           image_url = artist.images.get(0).url;
        }

        if(!image_url.isEmpty()) {
            // use Picasso to load image into artist_item.xml image view
            mPicasso.load(image_url)
                    .into(holder.imageView);
        }

        // set textView to name of this artist
        holder.textView.setText(artist.name);

        return view;
    }

    /*
    View Holder design pattern. Reduces use of findViewById()
     */
    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
