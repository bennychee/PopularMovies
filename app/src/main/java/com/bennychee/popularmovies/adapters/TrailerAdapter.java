package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.data.MovieContract;

/**
 * Created by B on 21/01/2016.
 */
public class TrailerAdapter extends CursorAdapter {

    public static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_trailer, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        Log.d(LOG_TAG, "binding view: " + view.toString());



        TextView trailerNameTextView = (TextView) view.findViewById(R.id.trailer_name);

        String trailerName = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                                .TrailerEntry
                                .COLUMN_TITLE)
        );

        trailerNameTextView.setText(trailerName);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String youtubeKey = cursor.getString(
                        cursor.getColumnIndex(
                                MovieContract
                                        .TrailerEntry
                                        .COLUMN_YOUTUBE_KEY)
                );

                Uri youtubeUri = Uri.parse(BuildConfig.YOUTUBE_TRAILER_URL + youtubeKey);
                Log.d(LOG_TAG, "Youtube URL: " + youtubeUri.toString());
                //TODO Play youtube Video via intent
            }
        });
    }
}
