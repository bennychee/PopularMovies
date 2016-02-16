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

import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;
import com.bennychee.popularmovies.event.TrailerEvent;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link Fragment} subclass.
 */
public class MovieTrailerFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MovieTrailerFragment.class.getSimpleName();
    private ListView trailerListView;
    private static final int TRAILER_DETAIL_LOADER = 2;
    public static final String DETAIL_URI = "URI";
    private Uri mUri;
    private int movieId;
    private TrailerAdapter trailerAdapter;
    private int count = 0;

    @Override
    public void onStop() {
        super.onStop();
        trailerAdapter.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        trailerAdapter.release();
    }

    public MovieTrailerFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovieTrailerFragment.DETAIL_URI);
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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().restartLoader(TRAILER_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(TRAILER_DETAIL_LOADER, null, this);
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
                trailerAdapter.release();
                trailerAdapter.swapCursor(null);
            break;
        }
    }
}