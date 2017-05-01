package com.example.android.popmovies.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
import com.example.android.popmovies.adapter.ReviewAdapter;
import com.example.android.popmovies.model.APIService;
import com.example.android.popmovies.model.APIUtils;
import com.example.android.popmovies.model.MovieResult;
import com.example.android.popmovies.model.ReviewResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment implements Callback<ReviewResponse>{

    private APIService mService;
    private MovieResult mMovie;
    private ReviewAdapter mReviewAdapter;
    RecyclerView recyclerViewReview;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    int id;

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReviewAdapter = new ReviewAdapter();
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
        View rootView = inflater.inflate(R.layout.fragment_review, container, false);

        recyclerViewReview = (RecyclerView) rootView.findViewById(R.id.review_RV);

        Context context = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerViewReview.setLayoutManager(linearLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        recyclerViewReview.setHasFixedSize(true);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        recyclerViewReview.setAdapter(mReviewAdapter);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView)rootView.findViewById(R.id.tv_error_message_display_review);
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar)rootView.findViewById(R.id.pb_loading_indicator_review);

        if (checkNetworkAvailable()) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            showReviewDataView();
            mService.getMovieReview(id, BuildConfig.API_KEY).enqueue(ReviewFragment.this);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }else{
            showErrorMessage();
        }

        return rootView;
    }

    private boolean checkNetworkAvailable() {
        /* Make sure the error is invisible and the review data is visible */
        showReviewDataView();

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
    private void showReviewDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the review data is visible */
        recyclerViewReview.setVisibility(View.VISIBLE);
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
        recyclerViewReview.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
             if (response.isSuccessful()) {
                 if (mReviewAdapter.getItemCount() != 0) {
                     mReviewAdapter.clear();
                 }
                     mReviewAdapter.setReviewData(response.body().getResults());
            } else {
                 int statusCode = response.code();
                 String message = getResources().getString(R.string.response_error_message) + statusCode;
                 Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onFailure(Call<ReviewResponse> call, Throwable t) {
        String message = getResources().getString(R.string.response_error_message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
