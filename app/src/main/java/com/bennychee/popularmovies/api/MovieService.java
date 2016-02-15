package com.bennychee.popularmovies.api;

import com.bennychee.popularmovies.api.models.popmovies.PopMovieModel;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by B on 13/01/2016.
 */
public interface MovieService {

    @GET("discover/movie")
    Call<PopMovieModel> getPopMovies(@Query("api_key") String apikey, @Query("sort_by") String sort);

    @GET("movie/{id}")
    Call<MovieRuntime> getMovieRuntime(@Path("id") int id, @Query("api_key") String apikey);

    @GET("movie/{id}/reviews")
    Call<MovieReviews> getMovieReview(@Path("id") int id, @Query("api_key") String apikey);

    @GET("movie/{id}/videos")
    Call<MovieTrailers> getMovieTrailer(@Path("id") int id, @Query("api_key") String apikey);

}

