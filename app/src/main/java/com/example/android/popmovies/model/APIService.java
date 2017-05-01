package com.example.android.popmovies.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by anerp_000 on 18/04/2017.
 */

public interface APIService {

    @GET("popular")
    Call<MovieResponse> getMovie(@Query("api_key") String apiKey);

    @GET("top_rated")
    Call<MovieResponse> getMovieTopRated(@Query("api_key") String apiKey);

    @GET("{id}/reviews")
    Call<ReviewResponse> getMovieReview(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("{id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path("id") int movieId, @Query("api_key") String apiKey);
}
