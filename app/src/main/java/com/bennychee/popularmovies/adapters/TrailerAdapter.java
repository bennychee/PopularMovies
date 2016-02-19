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
import android.widget.TextView;

import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by B on 21/01/2016.
 */
public class TrailerAdapter extends CursorAdapter  {

    public static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    private String youtubeKey;

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG, LOG_TAG);
        return LayoutInflater.from(context).inflate(R.layout.item_trailer, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView trailerNameTextView = (TextView) view.findViewById(R.id.trailer_name);
        ImageView imageView = (ImageView) view.findViewById(R.id.youtube_thumbnail);

        String trailerName = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                                .TrailerEntry
                                .COLUMN_TITLE)
        );

        trailerNameTextView.setText(trailerName);

        youtubeKey = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                            .TrailerEntry
                            .COLUMN_YOUTUBE_KEY)
        );


        String youtubeImageUrl = "http://img.youtube.com/vi/";

        Uri imageUri = Uri.parse(youtubeImageUrl).buildUpon()
                .appendPath(youtubeKey)
                .appendPath("default.jpg")
                .build();

        Picasso.with(context)
                .load(imageUri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .tag(context)
                .into(imageView);

    }
}
