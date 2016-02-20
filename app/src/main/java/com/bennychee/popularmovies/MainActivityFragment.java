package com.bennychee.popularmovies;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.GridView;

import com.bennychee.popularmovies.adapters.PopMovieAdapter;
import com.bennychee.popularmovies.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private PopMovieAdapter popMovieAdapter;
    private GridView popMoviesGridView;
    public static final int MOVIE_LOADER = 3;
    private Uri firstMovieUri;
    private boolean firstEntry = true;
    private SyncReceiver myReceiver;

    private int mPosition = GridView.INVALID_POSITION;
    private Uri movieUri;
    private boolean isOnResume = false;
    private int scrollPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private ProgressDialog progressDialog;
    private ProgressDialog firstDialog;
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

        myReceiver = new SyncReceiver();
        IntentFilter intentFilter = new IntentFilter("com.bennychee.syncstatus");
        getActivity().registerReceiver(myReceiver, intentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateMovies();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firstDialog.isShowing()){
            firstDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Inside onResume");
        isOnResume = true;
        if (getResources().getBoolean(R.bool.dual_pane)) {
            firstDialog.show();
            firstDialog.hide();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);

        if (savedInstanceState != null && savedInstanceState.containsKey("ScrollPosition")) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            scrollPosition = savedInstanceState.getInt("ScrollPosition");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firstDialog.isShowing()){
            firstDialog.dismiss();
        }
        getActivity().unregisterReceiver(myReceiver);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, LOG_TAG);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if ((isOnResume || firstEntry) && getResources().getBoolean(R.bool.dual_pane)) {
                    Log.d(LOG_TAG, "Progress Dialog Dismissed. Doing work!");
                    movieUri  = firstMovieUri;
                    ((Callback) getActivity())
                            .onItemSelected(movieUri);
                }
            }
        });

        firstDialog = new ProgressDialog(getActivity());
        firstDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isOnResume) {
                    Log.d(LOG_TAG, "onResume Loading URI = " + firstMovieUri);
                    if(firstMovieUri != null) {
                        ((Callback) getActivity())
                                .onItemSelected(firstMovieUri);
                    }
                    isOnResume = false;
                }
            }
        });

        popMoviesGridView = (GridView) rootView.findViewById(R.id.movie_posters_gridview);
        if(getResources().getBoolean(R.bool.dual_pane)) {
            popMoviesGridView.setNumColumns(3);
        }
        popMovieAdapter = new PopMovieAdapter(getActivity(), null, 0);
        popMoviesGridView.setAdapter(popMovieAdapter);

        popMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor currentPos = (Cursor) parent.getItemAtPosition(position);
                if (currentPos != null) {
                    final int MOVIE_ID_COL = currentPos.getColumnIndex(MovieContract.MovieEntry._ID);
                    movieUri = MovieContract.MovieEntry.buildMovieWithId(currentPos.getInt(MOVIE_ID_COL));
                    firstEntry = false;

                    ((Callback) getActivity())
                            .onItemSelected(movieUri);
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

        scrollPosition = popMoviesGridView.getFirstVisiblePosition();
        outState.putInt("ScrollPosition", scrollPosition);

        super.onSaveInstanceState(outState);
    }

    public class SyncReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Receiving Broadcast");
            Bundle extras = intent.getExtras();
            String syncStatus;
            String getUri;
            if (intent.getAction().equals("com.bennychee.syncstatus")) {
                if (extras != null) {
                    syncStatus = extras.getString("SYNCING_STATUS");
                    Log.d(LOG_TAG, "SyncStatus = " + syncStatus);
                    //if (syncStatus == "RUNNING") {
                    if (syncStatus.equals("RUNNING")) {
                        Log.d(LOG_TAG, "Intent Running");
                        progressDialog.setTitle("Please wait....");
                        progressDialog.setMessage("Loading Movies....");
                        progressDialog.setIndeterminate(true);
                        progressDialog.show();
                    } else if (syncStatus.equals("STOPPING")) {
                        Log.d(LOG_TAG, "Intent Stopping");
                        progressDialog.dismiss();
                    }
                }
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MOVIE_LOADER) {
            final int NUMBER_OF_MOVIES = 20;
            String sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";

            return new CursorLoader(getActivity(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_IMAGE_URL},
                    null,
                    null,
                    sortOrder + " LIMIT " + NUMBER_OF_MOVIES);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == MOVIE_LOADER) {
            popMovieAdapter.swapCursor(data);

            if (mPosition != GridView.INVALID_POSITION) {
                // If we don't need to restart the loader, and there's a desired position to restore
                // to, do so now.
                popMoviesGridView.smoothScrollToPosition(mPosition);
            }


            if (scrollPosition != GridView.INVALID_POSITION) {
                popMoviesGridView.smoothScrollToPosition(scrollPosition);
            }

            if (firstEntry && data.getCount() > 0 && getResources().getBoolean(R.bool.dual_pane)) {
                data.moveToFirst();
                Log.d(LOG_TAG, "1st Entry detected. Dual pane mode detected.");
                final int MOVIE_ID_COL = data.getColumnIndex(MovieContract.MovieEntry._ID);
                Log.d(LOG_TAG, "Movie ID in onLoadfinished = " + MOVIE_ID_COL);
                firstMovieUri = MovieContract.MovieEntry.buildMovieWithId(data.getInt(MOVIE_ID_COL));
                Log.d(LOG_TAG, "First Movie URI = " + firstMovieUri.toString());
                if (isOnResume) {
                    firstDialog.dismiss();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        popMovieAdapter.swapCursor(null);
    }
}
