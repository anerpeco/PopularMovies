package com.example.android.popmovies.model;

/**
 * Created by anerp_000 on 18/04/2017.
 */

public class APIUtils {

    public static final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    public static APIService getAPIService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
