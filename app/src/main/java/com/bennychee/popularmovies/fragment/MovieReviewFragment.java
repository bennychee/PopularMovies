package com.bennychee.popularmovies.fragment;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.adapters.ReviewAdapter;
import com.bennychee.popularmovies.data.MovieContract.ReviewEntry;


/**
 * A simple {@link MovieReviewFragment} subclass.
 */
public class MovieReviewFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieReviewFragment.class.getSimpleName();

    private ListView reviewListView;
    private TextView reviewText;


    private static final int REVIEW_DETAIL_LOADER = 1;

    public static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private ReviewAdapter reviewAdapter;

    public MovieReviewFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovieReviewFragment.DETAIL_URI);
            if (mUri != null) {
                movieId = Utility.fetchMovieIdFromUri(getContext(), mUri);
            }
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_review, container, false);

        reviewListView = (ListView) rootView.findViewById(R.id.listview_review);
        reviewText = (TextView) rootView.findViewById(R.id.review_text);


        if(mUri == Uri.EMPTY) {
            reviewListView.setVisibility(View.INVISIBLE);
        } else {
            reviewListView.setVisibility(View.VISIBLE);
        }

        Cursor reviewCursor = getActivity().getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,
                ReviewEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)},
                null
        );

        reviewAdapter = new ReviewAdapter(getActivity(), reviewCursor, 0);
        reviewListView.setAdapter(reviewAdapter);

        getLoaderManager().initLoader(REVIEW_DETAIL_LOADER, null, this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            if (id == REVIEW_DETAIL_LOADER) {
                Log.d(LOG_TAG, "Review Loader Created");
                return new CursorLoader(
                        getActivity(),
                        ReviewEntry.CONTENT_URI,
                        null,
                        ReviewEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{String.valueOf(movieId)},
                        null
                );
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
        case REVIEW_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Review Adapter");
                if (data != null && data.moveToFirst()) {
                    reviewAdapter.swapCursor(data);
                } else {
                    Log.d(LOG_TAG, "No Reviews");
                    reviewText.setVisibility(TextView.VISIBLE);
                    reviewText.setText("No Reviews");
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case REVIEW_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoaderReset - Review Adapter");
                reviewAdapter.swapCursor(null);
                break;
        }
    }
}

