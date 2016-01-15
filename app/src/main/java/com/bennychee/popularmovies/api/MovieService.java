package com.bennychee.popularmovies.api;

import com.bennychee.popularmovies.api.models.popmovies.PopMovieModel;
import com.bennychee.popularmovies.api.models.runtime.MovieRuntime;
import com.bennychee.popularmovies.api.models.review.MovieReviews;
import com.bennychee.popularmovies.api.models.trailers.MovieTrailers;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by B on 13/01/2016.
 */
public interface MovieService {

    @GET("discover/movie?api_key={apikey}")
    Call<PopMovieModel> getPopMovies(@Path("apikey") String apikey, @Query("sort_by") String sort);

    @GET("movie/{id}?api_key={apikey}")
    Call<MovieRuntime> getMovieRuntime(@Path("apikey") String apikey, @Path("id") int id);

    @GET("movie/{id}/reviews?api_key={apikey}")
    Call<MovieReviews> getMovieReview(@Path("apikey") String apikey, @Path("id") int id);

    @GET("movie/{id}/videos?api_key={apikey}")
    Call<MovieTrailers> getMovieTrailer(@Path("apikey") String apikey, @Path("id") int id);

}

