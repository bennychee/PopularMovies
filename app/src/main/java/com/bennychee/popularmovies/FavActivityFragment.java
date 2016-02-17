package com.bennychee.popularmovies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.FrameLayout;
import android.widget.GridView;

import com.bennychee.popularmovies.adapters.PopMovieAdapter;
import com.bennychee.popularmovies.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private PopMovieAdapter popMovieAdapter;
    private GridView popMoviesGridView;


    private FrameLayout frameLayout;

    public static final int FAV_MOVIE_LOADER = 4;
    private Uri firstMovieUri;
    private boolean firstEntry = true;

    private int count = 1;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private ProgressDialog progressDialog;

    final String LOG_TAG = FavActivityFragment.class.getSimpleName();

    public interface Callback {
        public void onFavItemSelected(Uri mUri);
    }

    public FavActivityFragment() {
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FAV_MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, LOG_TAG);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading Favorites....");
        progressDialog.setIndeterminate(true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (firstEntry && getResources().getBoolean(R.bool.dual_pane)) {
                    Log.d(LOG_TAG, "Progress Dialog Dismissed. Doing work!");
                    ((Callback) getActivity())
                            .onFavItemSelected(firstMovieUri);
                }
            }
        });

        Log.d(LOG_TAG, LOG_TAG + " - onCreateView");
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
                    final int MOVIE_ID_COL = currentPos.getColumnIndex(MovieContract.MovieEntry._ID);
                    Uri movieUri = MovieContract.MovieEntry.buildMovieWithId(currentPos.getInt(MOVIE_ID_COL));
                    firstEntry = false;

                    ((Callback) getActivity())
                            .onFavItemSelected(movieUri);
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to GridView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == FAV_MOVIE_LOADER) {
            return new CursorLoader(getActivity(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_IMAGE_URL},
                    MovieContract.MovieEntry.COLUMN_FAVORITE + "= ?",
                    new String[]{Integer.toString(1)},
                    null);
        } else {
            return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == FAV_MOVIE_LOADER) {
            Log.d(LOG_TAG, LOG_TAG + " onLoadFinished");
            popMovieAdapter.swapCursor(data);

            if (mPosition != GridView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                popMoviesGridView.smoothScrollToPosition(mPosition);
            }

            int numMovie = data.getCount();
            Log.d(LOG_TAG, "Number of Movies in Fav = " + numMovie);

            if (numMovie == 0) {
                firstMovieUri = Uri.EMPTY;
            }

            if (firstEntry && data.getCount() > 0 && getResources().getBoolean(R.bool.dual_pane)) {
                data.moveToFirst();
                Log.d(LOG_TAG, "1st Entry detected. Dual pane mode detected.");
                final int MOVIE_ID_COL = data.getColumnIndex(MovieContract.MovieEntry._ID);
                firstMovieUri = MovieContract.MovieEntry.buildMovieWithId(data.getInt(MOVIE_ID_COL));
            }
            progressDialog.dismiss();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        popMovieAdapter.swapCursor(null);
    }
}