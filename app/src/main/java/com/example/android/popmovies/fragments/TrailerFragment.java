package com.example.android.popmovies.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popmovies.BuildConfig;
import com.example.android.popmovies.R;
import com.example.android.popmovies.adapter.TrailerAdapter;
import com.example.android.popmovies.model.APIService;
import com.example.android.popmovies.model.APIUtils;
import com.example.android.popmovies.model.MovieResult;
import com.example.android.popmovies.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrailerFragment extends Fragment implements Callback<TrailerResponse> {

    private APIService mService;
    private MovieResult mMovie;
    private TrailerAdapter mTrailerAdapter;
    RecyclerView recyclerViewTrailer;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    int id;
    int columns;

    public TrailerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrailerAdapter = new TrailerAdapter();
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(MovieResult.TAG_MOVIES)) {
                mMovie = intent.getExtras().getParcelable(MovieResult.TAG_MOVIES);
            }
        }
        id = mMovie.getId();
        mService = APIUtils.getAPIService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trailer, container, false);

        recyclerViewTrailer = (RecyclerView) rootView.findViewById(R.id.trailer_RV);

        Context context = getActivity();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            columns = 3;
        }else{
            columns = 2;
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, columns, GridLayoutManager.VERTICAL, false);
        recyclerViewTrailer.setLayoutManager(gridLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        recyclerViewTrailer.setHasFixedSize(true);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        recyclerViewTrailer.setAdapter(mTrailerAdapter);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView)rootView.findViewById(R.id.tv_error_message_display_trailer);
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar)rootView.findViewById(R.id.pb_loading_indicator_trailer);

        if (checkNetworkAvailable()) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            showTrailerDataView();
            mService.getMovieTrailer(id, BuildConfig.API_KEY).enqueue(TrailerFragment.this);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }else{
            showErrorMessage();
        }

        return rootView;
    }

    private boolean checkNetworkAvailable() {
        /* Make sure the error is invisible and the review data is visible */
        showTrailerDataView();

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * This method will make the View for the review data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showTrailerDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the review data is visible */
        recyclerViewTrailer.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the review
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        recyclerViewTrailer.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
        if (response.isSuccessful()) {
            if (mTrailerAdapter.getItemCount() != 0) {
                mTrailerAdapter.clear();
            }
            mTrailerAdapter.setTrailerData(response.body().getResults());
        } else {
            int statusCode = response.code();
            String message = getResources().getString(R.string.response_error_message) + statusCode;
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailure(Call<TrailerResponse> call, Throwable t) {
        String message = getResources().getString(R.string.response_error_message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
