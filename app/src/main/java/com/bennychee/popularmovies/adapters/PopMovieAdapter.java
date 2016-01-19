package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * {@link PopMovieAdapter} exposes a list of popular movies with posters
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */

public class PopMovieAdapter extends CursorAdapter {

    public static final String LOG_TAG = PopMovieAdapter.class.getSimpleName();

    public PopMovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        return LayoutInflater.from(context).inflate(R.layout.poster_movie, parent, false);
        return LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, "binding view: " + view.toString());
        ImageView posterImageView = (ImageView) view.findViewById(R.id.poster_image);

        int posterColumn = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE_URL);
        String moviePoster = cursor.getString(posterColumn);

        Uri imageUri = Uri.parse(BuildConfig.IMAGE_BASE_URL).buildUpon()
                .appendPath(context.getString(R.string.image_size_medium))
                .appendPath(moviePoster.substring(1))
                .build();

        Picasso.with(context)
                .load(imageUri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .tag(context)
                .into(posterImageView);
    }
}
