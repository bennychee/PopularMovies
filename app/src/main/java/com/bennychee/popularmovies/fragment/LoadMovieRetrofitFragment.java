package com.bennychee.popularmovies.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.Utility;
import com.bennychee.popularmovies.api.MovieService;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.review.Result;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;
import com.bennychee.popularmovies.event.ReviewEvent;
import com.bennychee.popularmovies.event.RuntimeEvent;
import com.bennychee.popularmovies.event.TrailerEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by B on 1/02/2016.
 */
public class LoadMovieRetrofitFragment extends Fragment {

    private static final String LOG_TAG = LoadMovieRetrofitFragment.class.getSimpleName();
    private Uri mUri;
    public static final String DETAIL_URI = "URI";

    private int reviewCount = 0;
    private int runtimeCount = 0;
    private int trailerCount = 0;

    private final int RETRY_COUNT = 5;

    public  LoadMovieRetrofitFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);EventBus.getDefault().register(this);

    }


    public void LoadMovieRetrofit (Context context, int movieId) {

        String apiKey = BuildConfig.MOVIE_DB_API_TOKEN;
        String baseUrl = BuildConfig.API_BASE_URL;

        Log.d(LOG_TAG, "Base URL = " + baseUrl);
        Log.d(LOG_TAG, "API Key = " + apiKey);
        Log.d(LOG_TAG, "Movie ID = " + movieId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MovieService service = retrofit.create(MovieService.class);

        MovieRuntime(context, movieId, apiKey, service);
        MovieTrailers(context, movieId, apiKey, service);
        MovieReview(context, movieId, apiKey, service);
    }


    private void MovieRuntime(final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieRuntime> movieRuntimeCall = service.getMovieRuntime(movieId, apiKey);
        movieRuntimeCall.enqueue(new Callback<MovieRuntime>() {
            @Override
            public void onResponse(Response<MovieRuntime> response) {
                Log.d(LOG_TAG, "Movie Runtime Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Runtime " + movieId + " Response: " + response.errorBody().toString());
                    if (runtimeCount< RETRY_COUNT) {
                        //Retry 3 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + runtimeCount);
                        MovieRuntime(context, movieId, apiKey, service);
                        runtimeCount++;
                    }
                } else {
                    int runtime = response.body().getRuntime();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Runtime: " + runtime);
                    Utility.updateMovieWithRuntime(context, movieId, runtime);
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

    private void MovieTrailers(final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieTrailers> movieTrailersCall = service.getMovieTrailer(movieId, apiKey);
        movieTrailersCall.enqueue(new Callback<MovieTrailers>() {
            @Override
            public void onResponse(Response<MovieTrailers> response) {
                Log.d(LOG_TAG, "Movie Trailers Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Trailer " + movieId + " Response: " + response.errorBody().toString());
                    if (trailerCount < RETRY_COUNT) {
                        //Retry 3 times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + trailerCount);
                        MovieTrailers(context, movieId, apiKey, service);
                        trailerCount++;
                    }
                } else {
                    List<com.bennychee.popularmovies.api.models.trailers.Result> trailersResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Trailers Added: " + trailersResultList.size());
                    Utility.storeTrailerList(context, movieId, trailersResultList);
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

    private void MovieReview (final Context context, final int movieId, final String apiKey, final MovieService service) {
        Call<MovieReviews> movieReviewsCall = service.getMovieReview(movieId, apiKey);
        movieReviewsCall.enqueue(new Callback<MovieReviews>() {
            @Override
            public void onResponse(Response<MovieReviews> response) {
                Log.d(LOG_TAG, "Movie Reviews Response Status: " + response.code());
                if (!response.isSuccess()) {
                    Log.e(LOG_TAG, "Unsuccessful Call for Reviews " + movieId + " Response: " + response.errorBody().toString());
                    if (reviewCount < RETRY_COUNT) {
                        //Retry RETRY_COUNT times
                        Log.d(LOG_TAG, "Retry Retrofit service #" + reviewCount);
                        MovieReview(context, movieId, apiKey, service);
                        reviewCount++;
                    }
                } else {
                    List<Result> reviewResultList = response.body().getResults();
                    Log.d(LOG_TAG, "Movie ID: " + movieId + " Reviews Added: " + reviewResultList.size());
                    Utility.storeCommentList(context, movieId, reviewResultList);
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

    public void onEvent(ReviewEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Retrofit done, load the review loader!");
//            progressBar.dismiss();
        } else {
            Log.d(LOG_TAG, "Event Message - " + event.toString());
        }
    }

    public void onEvent(TrailerEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Event Message - Retrofit done, load the trailer loader!");
        } else {
            Log.d(LOG_TAG, "Event Message - " + event.toString());
        }
    }

    public void onEvent(RuntimeEvent event) {
        if (event.isRetrofitCompleted) {
            Log.d(LOG_TAG, "Retrofit done, load the movie runtime loader!");
        } else {
            Log.d(LOG_TAG, "Event Message - " + event.toString());
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
