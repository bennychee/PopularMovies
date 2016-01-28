package com.bennychee.popularmovies;


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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bennychee.popularmovies.adapters.ReviewAdapter;
import com.bennychee.popularmovies.adapters.TrailerAdapter;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.data.MovieContract;
import com.bennychee.popularmovies.data.MovieContract.MovieEntry;
import com.bennychee.popularmovies.data.MovieContract.TrailerEntry;
import com.bennychee.popularmovies.data.MovieContract.ReviewEntry;

import com.bennychee.popularmovies.event.ReviewEvent;
import com.bennychee.popularmovies.event.RuntimeEvent;
import com.bennychee.popularmovies.event.TrailerEvent;
import com.commonsware.cwac.merge.MergeAdapter;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PopMovieDetailActivityFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor>, YouTubePlayer.OnInitializedListener {

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


    private String youtubeKey;

    public TabLayout tabLayout;

    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int REVIEW_DETAIL_LOADER = 1;
    private static final int TRAILER_DETAIL_LOADER = 2;

    static final String DETAIL_URI = "URI";

    private Uri mUri;
    private int movieId;

    private ListView mListView;
    MergeAdapter mergeAdapter;
    private TrailerAdapter trailerAdapter;
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

    public PopMovieDetailActivityFragment() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mergeAdapter  = new MergeAdapter();
        EventBus.getDefault().register(this);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
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

        trailerGridView = (GridView) rootView.findViewById(R.id.gridview_detail_trailer);
        reviewListView = (ListView) rootView.findViewById(R.id.listview_detail_review);


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
//        mergeAdapter.addAdapter(trailerAdapter);
        trailerGridView.setAdapter(trailerAdapter);


        Cursor reviewCursor = getActivity().getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,
                ReviewEntry.COLUMN_MOVIE_ID + "=?",
                new String[]{String.valueOf(movieId)},
                null
        );

        reviewAdapter = new ReviewAdapter(getActivity(), reviewCursor, 0);
        //mergeAdapter.addAdapter(reviewAdapter);
        reviewListView.setAdapter(reviewAdapter);


/*
        mListView = (ListView) rootView.findViewById(R.id.listview_detail);
        mListView.setAdapter(mergeAdapter);
*/

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
                        //MovieEntry.CONTENT_URI,
                        null,
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
                trailerAdapter.notifyDataSetChanged();
                LoadTrailer(data);
                break;
            case MOVIE_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Movie Details Adapter");
                LoadMovieDetailView(data);
                break;
            case REVIEW_DETAIL_LOADER:
                Log.d(LOG_TAG, "Inside onLoadFinished - Review Adapter");
                reviewAdapter.swapCursor(data);
                reviewAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void LoadTrailer(Cursor data) {
        data.moveToFirst();
        FragmentManager fm = getActivity().getSupportFragmentManager();

        YouTubePlayerSupportFragment youtubeFragment = (YouTubePlayerSupportFragment) fm.findFragmentById(R.id.youtube_player);

        youtubeKey = data.getString(
                data.getColumnIndex(
                        MovieContract
                                .TrailerEntry
                                .COLUMN_YOUTUBE_KEY)
        );

/*
                Uri youtubeUri = Uri.parse(BuildConfig.YOUTUBE_TRAILER_URL + youtubeKey);
                Log.d(LOG_TAG, "Youtube URL: " + youtubeUri.toString());
*/

        youtubeFragment.initialize(BuildConfig.YOUTUBE_API_TOKEN, this);

/*
        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) view.findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(BuildConfig.YOUTUBE_API_TOKEN, this);
*/


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

