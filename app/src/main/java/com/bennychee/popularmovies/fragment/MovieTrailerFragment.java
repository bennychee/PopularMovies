package com.bennychee.popularmovies.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;
import com.bennychee.popularmovies.event.TrailerEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;


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
        EventBus.getDefault().register(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
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
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

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

    public void onEvent(TrailerEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Event Message - Retrofit done, load the trailer loader!");
            getLoaderManager().initLoader(TRAILER_DETAIL_LOADER, null, this);
        } else {
            Log.d(LOG_TAG, "Event Message - " + event.toString());
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //fetch movie details on-the-fly and store in DB
        movieId = Utility.fetchMovieIdFromUri(getActivity(), mUri);
//        LoadMovieDetails(movieId);

        getLoaderManager().initLoader(TRAILER_DETAIL_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
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



    private void LoadMovieDetails (final int movieId) {

        // check runtime from DB for movie ID so that if it is found in DB, no retrieval required
        if (Utility.checkTrailerFromUri(getContext(), mUri) <= 0) {
            String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;
            String baseUrl = BuildConfig.API_BASE_URL;

            Log.d(LOG_TAG, "Base URL = " + baseUrl);
            Log.d(LOG_TAG, "API Key = " + apiKey);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            final MovieService service = retrofit.create(MovieService.class);

            MovieTrailers(movieId, apiKey, service);
        } else {
            Log.d(LOG_TAG, "Info in DB. No Retrofit callback required");
            EventBus.getDefault().post(new TrailerEvent(true));

        }
    }


    private void MovieTrailers(final int movieId, final String apiKey, final MovieService service) {
        Call<MovieTrailers> movieTrailersCall = service.getMovieTrailer(movieId, apiKey);
        movieTrailersCall.enqueue(new Callback<MovieTrailers>() {
            @Override
            public void onResponse(Response<MovieTrailers> response) {
                Log.d(LOG_TAG, "Movie Trailers Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Trailer " + movieId + " Response: " + response.errorBody().toString());
                    if (count < 3) {
                        //Retry 3 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + count);
                        MovieTrailers(movieId, apiKey, service);
                        count++;
                    }
                } else {
                    List<com.bennychee.popularmovies.api.models.trailers.Result> trailersResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Trailers Added: " + trailersResultList.size());
                    Utility.storeTrailerList(getContext(), movieId, trailersResultList);
                    EventBus.getDefault().post(new TrailerEvent(true));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Trailer Error: " + t.getMessage());
                EventBus.getDefault().post(new TrailerEvent(false));
            }
        });

    }
}