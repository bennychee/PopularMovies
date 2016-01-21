package com.bennychee.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bennychee.popularmovies.adapters.ReviewAdapter;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.data.MovieContract.MovieEntry;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;
import com.bennychee.popularmovies.data.MovieContract.ReviewEntry;

import com.bennychee.popularmovies.event.ReviewEvent;
import com.bennychee.popularmovies.event.RuntimeEvent;
import com.bennychee.popularmovies.event.TrailerEvent;
import com.commonsware.cwac.merge.MergeAdapter;

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
public class PopMovieDetailActivityFragment extends Fragment implements  AppBarLayout.OnOffsetChangedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = PopMovieDetailActivityFragment.class.getSimpleName();

    // CooridnatorLayout sample
    // https://github.com/saulmm/CoordinatorBehaviorExample

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int REVIEW_DETAIL_LOADER = 1;
    private static final int TRAILER_DETAIL_LOADER = 2;

    static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private ListView mListView;
    MergeAdapter mergeAdapter;
    TrailerAdapter trailerAdapter;
    ReviewAdapter reviewAdapter;

    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_DESCRIPTION,
            MovieEntry.COLUMN_IMAGE_URL,
            MovieEntry.COLUMN_POPULARITY,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_FAVORITE
    };

    public PopMovieDetailActivityFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mergeAdapter  = new MergeAdapter();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(PopMovieDetailActivityFragment.DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pop_movie_detail_activity, container, false);
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        Cursor trailersCursor = getActivity().getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                TrailerEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)},
                null
        );

        trailerAdapter = new TrailerAdapter(getActivity(), trailersCursor, 0);
        mergeAdapter.addAdapter(trailerAdapter);

        Cursor reviewCursor = getActivity().getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,
                ReviewEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)},
                null
        );

        reviewAdapter = new ReviewAdapter(getActivity(), reviewCursor, 0);
        mergeAdapter.addAdapter(reviewAdapter);


        mListView = (ListView) rootView.findViewById(R.id.listview_detail);
        mListView.setAdapter(mergeAdapter);

        return rootView;
    }

    public void onEvent(RuntimeEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Retrofit done, load the movie detail loader!");
            getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        } else {

        }
    }

    public void onEvent(TrailerEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Retrofit done, load the trailer loader!");
            getLoaderManager().initLoader(TRAILER_DETAIL_LOADER, null, this);
        } else {

        }
    }

    public void onEvent(ReviewEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Retrofit done, load the review loader!");
            getLoaderManager().initLoader(REVIEW_DETAIL_LOADER, null, this);
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
        LoadMovieDetails(movieId);

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
                        MOVIE_DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            } else if (id == REVIEW_DETAIL_LOADER) {
                Log.d(LOG_TAG, "Review Loader Created");
                return new CursorLoader(
                        getActivity(),
                        ReviewEntry.CONTENT_URI,
                        null,
                        ReviewEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{String.valueOf(movieId)},
                        null
                );
            } else if (id == TRAILER_DETAIL_LOADER) {
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
            case MOVIE_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Movie Details Adapter");
                break;
            case REVIEW_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Review Adapter");
                reviewAdapter.swapCursor(data);
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
            case MOVIE_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoaderReset - Movie Details Adapter");
                break;
            case REVIEW_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoaderReset - Review Adapter");
                reviewAdapter.swapCursor(null);
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
            MovieReview(movieId, apiKey, service);
            MovieTrailers(movieId, apiKey, service);
        } else {
            Log.d(LOG_TAG, "Info in DB. No Retrofit callback required");
            EventBus.getDefault().post(new TrailerEvent(true));
            EventBus.getDefault().post(new ReviewEvent(true));
            EventBus.getDefault().post(new RuntimeEvent(true));
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

    private void MovieReview(final int movieId, String apiKey, MovieService service) {
        Call<MovieReviews> movieReviewsCall = service.getMovieReview(movieId, apiKey);
        movieReviewsCall.enqueue(new Callback<MovieReviews>() {
            @Override
            public void onResponse(Response<MovieReviews> response) {
                Log.d(LOG_TAG, "Movie Reviews Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Reviews " + movieId + " Response: " + response.errorBody().toString());
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
                EventBus.getDefault().post(new ReviewEvent(false));}
        });

    }

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
}
