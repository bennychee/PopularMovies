package com.bennychee.popularmovies.fragment;


import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.data.MovieContract.MovieEntry;
import com.bennychee.popularmovies.data.MovieContract.ReviewEntry;
import com.bennychee.popularmovies.event.ReviewEvent;

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
public class MovieReviewFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieReviewFragment.class.getSimpleName();

    // CooridnatorLayout sample
    // https://github.com/saulmm/CoordinatorBehaviorExample

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

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
    private GridView trailerGridView;
    private ListView reviewListView;
    private TextView reviewText;


    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int REVIEW_DETAIL_LOADER = 1;
    private static final int TRAILER_DETAIL_LOADER = 2;

    public static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private int count = 0;

    private ProgressDialog progressBar;

    private ReviewAdapter reviewAdapter;

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

    public MovieReviewFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovieReviewFragment.DETAIL_URI);
            movieId = Utility.fetchMovieIdFromUri(getContext(), mUri);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_review, container, false);

        mToolbar        = (CollapsingToolbarLayout) rootView.findViewById(R.id.details_toolbar_name);
        mTitleContainer = (LinearLayout) rootView.findViewById(R.id.main_linearlayout_title);
//        mAppBarLayout   = (AppBarLayout) rootView.findViewById(R.id.main_appbar);
        mTitle          = (TextView) rootView.findViewById(R.id.main_textview_title);

        mDescription = (TextView) rootView.findViewById(R.id.movie_desc);

        posterImageView = (ImageView) rootView.findViewById(R.id.detail_poster_image);
        backdropImageView = (ImageView) rootView.findViewById(R.id.detail_backdrop_image);

        mMovieRating = (TextView) rootView.findViewById(R.id.movie_rating);
        mMovieRuntime = (TextView) rootView.findViewById(R.id.movie_runtime);
        mMovieYear = (TextView) rootView.findViewById(R.id.movie_year);
        mVotes = (TextView) rootView.findViewById(R.id.movie_votes);

        reviewListView = (ListView) rootView.findViewById(R.id.listview_review);

        reviewText = (TextView) rootView.findViewById(R.id.review_text);

/*
        progressBar = new ProgressDialog(rootView.getContext());
        progressBar.setCancelable(true);
        progressBar.setMessage("Getting Reviews");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
*/

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

    public void onEvent(ReviewEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, event.toString() + " Retrofit done, load the review loader!");
            getLoaderManager().initLoader(REVIEW_DETAIL_LOADER, null, this);
        } else {
            Log.d(LOG_TAG, "Event Message - " + event.toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //fetch movie details on-the-fly and store in DB
//        movieId = Utility.fetchMovieIdFromUri(getActivity(), mUri);
//        LoadMovieDetails(movieId);
//        getLoaderManager().initLoader(REVIEW_DETAIL_LOADER, null, this);

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
//                    reviewText.setVisibility(TextView.INVISIBLE);
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

/*
    private void LoadMovieDetails (final int movieId) {

        // check runtime from DB for movie ID so that if it is found in DB, no retrieval required
        if (Utility.checkReviewFromUri(getContext(), mUri) <= 0) {
            String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;
            String baseUrl = BuildConfig.API_BASE_URL;

            Log.d(LOG_TAG, "Base URL = " + baseUrl);
            Log.d(LOG_TAG, "API Key = " + apiKey);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            final MovieService service = retrofit.create(MovieService.class);
            MovieReview(movieId, apiKey, service);
        } else {
            Log.d(LOG_TAG, "Info in DB. No Retrofit callback required");
            EventBus.getDefault().post(new ReviewEvent(true));
        }
    }

    private void MovieReview(final int movieId, final String apiKey, final MovieService service) {
        Call<MovieReviews> movieReviewsCall = service.getMovieReview(movieId, apiKey);
        movieReviewsCall.enqueue(new Callback<MovieReviews>() {
            @Override
            public void onResponse(Response<MovieReviews> response) {
                Log.d(LOG_TAG, "Movie Reviews Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Reviews " + movieId + " Response: " + response.errorBody().toString());
                    if (count < 5) {
                        //Retry 5 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + count);
                        MovieReview(movieId, apiKey, service);
                        count++;
                    }
                } else {
                    List<Result> reviewResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Reviews Added: " + reviewResultList.size());
                    Utility.storeCommentList(getContext(), movieId, reviewResultList);
                    EventBus.getDefault().post(new ReviewEvent(true));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, "Movie Review Error: " + t.getMessage());
                EventBus.getDefault().post(new ReviewEvent(false));
            }
        });
    }
*/
}

