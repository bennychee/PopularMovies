package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.data.MovieContract;

/**
 * Created by B on 21/01/2016.
 */
public class ReviewAdapter extends CursorAdapter {

    public static final String LOG_TAG = ReviewAdapter.class.getSimpleName();

    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, "binding view: " + view.toString());

        TextView authorTextView = (TextView) view.findViewById(R.id.review_author);
        TextView reviewTextView = (TextView) view.findViewById(R.id.review_content);

        String author = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                                .ReviewEntry
                                .COLUMN_AUTHOR)
        );

        String review = cursor.getString(
                cursor.getColumnIndex(
                        MovieContract
                                .ReviewEntry
                                .COLUMN_CONTENT)
        );

        authorTextView.setText(author);
        reviewTextView.setText(review);
    }
}
