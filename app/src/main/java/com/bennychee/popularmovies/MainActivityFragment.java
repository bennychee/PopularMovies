package com.bennychee.popularmovies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bennychee.popularmovies.adapters.PopMovieAdapter;
import com.bennychee.popularmovies.data.MovieContract;
import com.bennychee.popularmovies.sync.MovieSyncAdapter;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private PopMovieAdapter popMovieAdapter;
    private GridView popMoviesGridView;
    public static final int MOVIE_LOADER = 0;
    private Uri firstMovieUri;

    private int count = 1;

    private ProgressDialog progressDialog;

    final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public interface Callback {
        public void onItemSelected(Uri mUri);
    }


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //TODO: inflater.inflate(R.menu.menu_pop_movies, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        MovieSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading Movies....");
        progressDialog.setIndeterminate(true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getResources().getBoolean(R.bool.dual_pane)) {
                    Log.d(LOG_TAG, "Progress Dialog Dimissed. Doing work!");
                    ((Callback) getActivity())
                            .onItemSelected(firstMovieUri);
                }
            }
        });


        Log.d(LOG_TAG, "MainActivityFragment - onCreateView");
        popMoviesGridView = (GridView) rootView.findViewById(R.id.movie_posters_gridview);
        if(getResources().getBoolean(R.bool.dual_pane)) {
            popMoviesGridView.setNumColumns(3);
        }
        popMovieAdapter = new PopMovieAdapter(getActivity(), null, 0);
        popMoviesGridView.setAdapter(popMovieAdapter);

        progressDialog.show();

        Log.d(LOG_TAG, "popMoviesGridView: " + popMoviesGridView.getAdapter().toString());
        popMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor currentPos = (Cursor) parent.getItemAtPosition(position);
                if (currentPos != null) {
//                    Intent movieDetailIntent = new Intent(getActivity(), PopMovieDetailActivity.class);
                    final int MOVIE_ID_COL = currentPos.getColumnIndex(MovieContract.MovieEntry._ID);
                    Uri movieUri = MovieContract.MovieEntry.buildMovieWithId(currentPos.getInt(MOVIE_ID_COL));

                    ((Callback) getActivity())
                            .onItemSelected(movieUri);
/*
                    movieDetailIntent.setData(movieUri);
                    startActivity(movieDetailIntent);
*/
                }
            }
        });

/*
        if((getResources().getBoolean(R.bool.dual_pane)) && count == 1) {
            Intent movieDetailIntent = new Intent(getActivity(), PopMovieDetailActivity.class);
            Uri movieUri = MovieContract.MovieEntry.buildMovieWithId();
            count = 0;
        }
*/
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrderSetting = Utility.getPreferredSortOrder(getActivity());
        String sortOrder;
        final int NUMBER_OF_MOVIES = 20;

        if (sortOrderSetting.equals(getString(R.string.prefs_sort_default_value))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else {
            //sort by rating
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }

        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_IMAGE_URL},
                null,
                null,
                sortOrder + " LIMIT " + NUMBER_OF_MOVIES);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        popMovieAdapter.swapCursor(data);

        if (data.getCount() > 0 && getResources().getBoolean(R.bool.dual_pane)) {
            data.moveToFirst();
            Log.d(LOG_TAG, "Dual Pane Detected.");
            final int MOVIE_ID_COL = data.getColumnIndex(MovieContract.MovieEntry._ID);
            firstMovieUri = MovieContract.MovieEntry.buildMovieWithId(data.getInt(MOVIE_ID_COL));
        }

        progressDialog.dismiss();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        popMovieAdapter.swapCursor(null);
    }
}
