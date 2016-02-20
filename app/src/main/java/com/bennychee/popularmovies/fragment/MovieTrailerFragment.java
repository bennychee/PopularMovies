package com.bennychee.popularmovies.fragment;


import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.YoutubeLightBox;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.data.MovieContract;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;


/**
 * A simple {@link MovieTrailerFragment} subclass.
 */
public class MovieTrailerFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MovieTrailerFragment.class.getSimpleName();
    private ListView trailerListView;
    private static final int TRAILER_DETAIL_LOADER = 2;
    public static final String DETAIL_URI = "URI";
    private Uri mUri;
    private int movieId;
    private TrailerAdapter trailerAdapter;

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public MovieTrailerFragment() {
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
            mUri = arguments.getParcelable(MovieTrailerFragment.DETAIL_URI);
            if (mUri !=null ) {
                movieId = Utility.fetchMovieIdFromUri(getContext(), mUri);
            }
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_trailer, container, false);
        trailerListView = (ListView) rootView.findViewById(R.id.listview_trailer);

        // The CursorAdapter will take data from our cursor and populate the ListView.
        Cursor trailersCursor = getActivity().getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                TrailerEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)},
                null
        );

        trailerAdapter = new TrailerAdapter(getActivity(), trailersCursor, 0);
        trailerListView.setAdapter(trailerAdapter);

        Intent intent = getActivity().getIntent();
               if (intent == null) {
                        return null;
               }

        trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor currentPos = (Cursor) parent.getItemAtPosition(position);
                if (currentPos != null) {
                    String youtubeKey = currentPos.getString(
                            currentPos.getColumnIndex(
                            MovieContract
                                    .TrailerEntry
                                    .COLUMN_YOUTUBE_KEY)
                    );
                    final Intent lightboxIntent = new Intent(getContext(), YoutubeLightBox.class);
                    lightboxIntent.putExtra(YoutubeLightBox.KEY_VIDEO_ID, youtubeKey);
                    getContext().startActivity(lightboxIntent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILER_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getLoaderManager().getLoader(TRAILER_DETAIL_LOADER) !=null) {
            getLoaderManager().restartLoader(TRAILER_DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            if (id == TRAILER_DETAIL_LOADER) {
                Log.d(LOG_TAG, "Trailer Loader Created");
                return new CursorLoader(
                        getActivity(),
                        TrailerEntry.CONTENT_URI,
                        null,
                        TrailerEntry.COLUMN_MOVIE_ID + "=?",
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
            case TRAILER_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Trailer Adapter");
                trailerAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRAILER_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoaderReset - Trailer Adapter");
                trailerAdapter.swapCursor(null);
            break;
        }
    }
}