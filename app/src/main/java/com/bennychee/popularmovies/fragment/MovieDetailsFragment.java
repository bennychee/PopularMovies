package com.bennychee.popularmovies.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.R;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.adapters.ReviewAdapter;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.data.MovieContract.MovieEntry;
import com.bennychee.popularmovies.data.MovieContract.ReviewEntry;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;
import com.bennychee.popularmovies.event.ReviewEvent;
import com.bennychee.popularmovies.event.RuntimeEvent;
import com.bennychee.popularmovies.event.TrailerEvent;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.squareup.picasso.Picasso;

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
public class MovieDetailsFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

/*
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

*/
    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private TextView mDescription;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mToolbar;
    private ImageView posterImageView;
    private ImageView backdropImageView;
    private TextView mMovieRuntime;
    private TextView mMovieYear;
    private TextView mMovieRating;
    private TextView mVotes;

    private static final int MOVIE_DETAIL_LOADER = 0;

    public static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private int count = 0;

    private ProgressDialog progressBar;


    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_DESCRIPTION,
            MovieEntry.COLUMN_IMAGE_URL,
            MovieEntry.COLUMN_BACKDROP_IMAGE_URL,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_FAVORITE
    };

    public MovieDetailsFragment() {
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
            mUri = arguments.getParcelable(MovieDetailsFragment.DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mToolbar        = (CollapsingToolbarLayout) rootView.findViewById(R.id.details_toolbar_name);
        mTitleContainer = (LinearLayout) rootView.findViewById(R.id.main_linearlayout_title);
        mTitle          = (TextView) rootView.findViewById(R.id.main_textview_title);

        mDescription = (TextView) rootView.findViewById(R.id.movie_desc);

        posterImageView = (ImageView) rootView.findViewById(R.id.detail_poster_image);
        backdropImageView = (ImageView) rootView.findViewById(R.id.detail_backdrop_image);

        mMovieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        mMovieRuntime = (TextView) rootView.findViewById(R.id.movie_runtime);
        mMovieYear = (TextView) rootView.findViewById(R.id.movie_year);
        mVotes = (TextView) rootView.findViewById(R.id.movie_votes);

    /*    progressBar = new ProgressDialog(rootView.getContext());
        progressBar.setCancelable(true);
        progressBar.setMessage("Retrieving Movie Details");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
*/
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return rootView;
    }

    public void onEvent(RuntimeEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Event Message - Retrofit done, load the movie detail loader!");
            getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
  //          progressBar.dismiss();
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
        LoadMovieDetails(movieId);
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            if (id == MOVIE_DETAIL_LOADER) {
                Log.d(LOG_TAG, "Movie Details Loader Created");
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        //MovieEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MOVIE_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Movie Details Adapter");
                LoadMovieDetailView(data);
                break;
        }
    }

    private void LoadMovieDetailView(Cursor data) {
        if (data != null && data.moveToFirst()) {
            String title = data.getString(data.getColumnIndex(MovieEntry.COLUMN_TITLE));
            Log.d(LOG_TAG, "Title: " + title);
            mToolbar.setTitle(title);

            String desc = data.getString(data.getColumnIndex(MovieEntry.COLUMN_DESCRIPTION));
            mDescription.setText(desc);

            String moviePoster = data.getString(data.getColumnIndex(MovieEntry.COLUMN_IMAGE_URL));
            String backdropPoster = data.getString(data.getColumnIndex(MovieEntry.COLUMN_BACKDROP_IMAGE_URL));

            Uri imageUri = Uri.parse(BuildConfig.IMAGE_BASE_URL).buildUpon()
                    .appendPath(getActivity().getString(R.string.image_size_medium))
                    .appendPath(moviePoster.substring(1))
                    .build();

            Uri backdropUri = Uri.parse(BuildConfig.IMAGE_BASE_URL).buildUpon()
                    .appendPath(getActivity().getString(R.string.image_size_large))
                    .appendPath(backdropPoster.substring(1))
                    .build();

            Picasso.with(getActivity())
                    .load(imageUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .tag(getActivity())
                    .into(posterImageView);

            Picasso.with(getActivity())
                    .load(backdropUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .tag(getActivity())
                    .into(backdropImageView);

            mMovieYear.setText(Utility.getReleaseYear(data.getString(data.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE))));
            mMovieRuntime.setText(getActivity().getString(R.string.runtime_mins, data.getString(data.getColumnIndex(MovieEntry.COLUMN_RUNTIME))));
            mMovieRating.setText(getActivity().getString(R.string.ratings_ten, data.getString(data.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE))));
            mVotes.setText(getActivity().getString(R.string.votes, data.getString(data.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT))));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case MOVIE_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoaderReset - Movie Details Adapter");
                break;
        }
    }


    private void LoadMovieDetails (final int movieId) {

        // check runtime from DB for movie ID so that if it is found in DB, no retrieval required
        if (Utility.checkRuntimeFromUri(getContext(), mUri) <= 0) {
            String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;
            String baseUrl = BuildConfig.API_BASE_URL;

            Log.d(LOG_TAG, "Base URL = " + baseUrl);
            Log.d(LOG_TAG, "API Key = " + apiKey);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            final MovieService service = retrofit.create(MovieService.class);

            MovieRuntime(movieId, apiKey, service);
        } else {
            Log.d(LOG_TAG, "Info in DB. No Retrofit callback required");
            EventBus.getDefault().post(new RuntimeEvent(true));
        }
    }

    private void MovieRuntime(final int movieId, final String apiKey, final MovieService service) {
        Call<MovieRuntime> movieRuntimeCall = service.getMovieRuntime(movieId, apiKey);
        movieRuntimeCall.enqueue(new Callback<MovieRuntime>() {
            @Override
            public void onResponse(Response<MovieRuntime> response) {
                Log.d(LOG_TAG, "Movie Runtime Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Runtime " + movieId + " Response: " + response.errorBody().toString());
                    if (count < 3) {
                        //Retry 3 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + count);
                        MovieRuntime(movieId, apiKey, service);
                        count++;
                    }
                } else {
                    int runtime = response.body().getRuntime();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Runtime: " + runtime);
                    Utility.updateMovieWithRuntime(getContext(), movieId, runtime);
                    EventBus.getDefault().post(new RuntimeEvent(true));
                    Log.d(LOG_TAG, "EventBus posted");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Runtime Error: " + t.getMessage());
                EventBus.getDefault().post(new RuntimeEvent(false));
            }
        });

    }
}

