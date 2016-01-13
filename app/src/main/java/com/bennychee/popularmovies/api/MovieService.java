package com.bennychee.popularmovies.api;

import com.bennychee.popularmovies.BuildConfig;
import com.bennychee.popularmovies.api.models.PopMovieModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by B on 13/01/2016.
 */
public interface MovieService {

    @GET("/discover/movie?api_key={apikey}")
    Call<PopMovieModel> groupList(@Path("apikey") String apikey, @Query("sort_by") String sort);
}

