package com.bennychee.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
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
        final TextView reviewTextView = (TextView) view.findViewById(R.id.review_content);
        final TextView showAll = (TextView) view.findViewById(R.id.detail_read_all);

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

        authorTextView.setText("Reviewer : " + author);
        authorTextView.setTypeface(null, Typeface.BOLD);

        reviewTextView.setText(review);
        Log.d(LOG_TAG, "Line Count = " + reviewTextView.getLineCount() + "/" + reviewTextView.getMaxLines());
        if (reviewTextView.getLineCount() > reviewTextView.getMaxLines()) {
            showAll.setTypeface(null, Typeface.BOLD_ITALIC);
            showAll.setVisibility(View.VISIBLE);
        } else {
            showAll.setVisibility(View.INVISIBLE);
        }

        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAll.setVisibility(View.INVISIBLE);
                reviewTextView.setMaxLines(Integer.MAX_VALUE);
            }
        });

    }
}
