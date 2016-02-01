package com.bennychee.popularmovies.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RecoverySystem;
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
import com.bennychee.popularmovies.data.MovieContract;
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

import org.w3c.dom.Text;

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
        EventBus.getDefault().register(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovieReviewFragment.DETAIL_URI);
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

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
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

        return rootView;
    }

    public void onEvent(ReviewEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Retrofit done, load the review loader!");
            getLoaderManager().initLoader(REVIEW_DETAIL_LOADER, null, this);
            progressBar.dismiss();
        } else {

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
        getLoaderManager().initLoader(REVIEW_DETAIL_LOADER, null, this);

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

/*
    private void MovieRuntime(final int movieId, String apiKey, MovieService service) {
        Call<MovieRuntime> movieRuntimeCall = service.getMovieRuntime(movieId, apiKey);
        movieRuntimeCall.enqueue(new Callback<MovieRuntime>() {
            @Override
            public void onResponse(Response<MovieRuntime> response) {
                Log.d(LOG_TAG, "Movie Runtime Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Runtime " + movieId + " Response: " + response.errorBody().toString());
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
*/

/*
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        Log.d(LOG_TAG, "Youtube play initialized.");
        youTubePlayer.cueVideo(youtubeKey);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(getActivity(), 1).show();
        } else {
//            String errorMessage = String.format(getString(R.string.youtube_error), youTubeInitializationResult.toString());
            //Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
*/

/*
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
*/
}

