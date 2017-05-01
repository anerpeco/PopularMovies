package com.example.android.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popmovies.adapter.MovieAdapter;
import com.example.android.popmovies.db.MovieContract;
import com.example.android.popmovies.model.APIService;
import com.example.android.popmovies.model.APIUtils;
import com.example.android.popmovies.model.MovieResponse;
import com.example.android.popmovies.model.MovieResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MovieAdapter.MovieAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private static final String POPULAR = "popular";
    private static final String TOP_RATED = "top_rated";
    private static final String FAVORITES = "favorites";
    private String mSorting = POPULAR;
    private String mSubtitle;
    String favSub;
    String popSub;
    String topSub;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    private ImageView mMovieImageView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    int columns;

    private MovieAdapter mMoviesAdapter;
    RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    Context context;
    private APIService mService;
    private static final String API_KEY = BuildConfig.API_KEY;
    private int mNavItemId;
    private static final String NAV_ITEM_ID = "navItemId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = APIUtils.getAPIService();

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        recyclerView = (RecyclerView) findViewById(R.id.rv_poster);
        context = getBaseContext();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            columns = 3;
        }else{
            columns = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, columns, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        recyclerView.setHasFixedSize(true);

        /*
         * The MoviesAdapter is responsible for linking our movies data with the Views that
         * will end up displaying our movies data.
         */
        mMoviesAdapter = new MovieAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        recyclerView.setAdapter(mMoviesAdapter);

        favSub = getResources().getString(R.string.favorites_subtitle);
        popSub = getResources().getString(R.string.most_popular_subtitle);
        topSub = getResources().getString(R.string.top_rated_subtitle);

        SharedPreferences sort = context.getSharedPreferences("sortorder", MODE_PRIVATE);
        String sorting = sort.getString("order", POPULAR);
        String subtitle = sort.getString("subtitle", popSub);
        mSorting = sorting;
        mSubtitle = subtitle;
        mNavItemId = sort.getInt(NAV_ITEM_ID, R.id.nav_popular);

        mToolbar.setSubtitle(mSubtitle);

        mMovieImageView = (ImageView)findViewById(R.id.iv_movie_poster);
        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView)findViewById(R.id.tv_error_message_display);
        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar)findViewById(R.id.pb_loading_indicator);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(mNavItemId);


        if(checkNetworkAvailable() || mSorting.contains(FAVORITES)) {
            loadMovies(mSorting);
        }else{
            showErrorMessage();
        }
    }

    private boolean checkNetworkAvailable() {
        /* Make sure the error is invisible and the movies data is visible */
        showMoviesDataView();

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * This method will make the View for the movies data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movies data is visible */
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movies
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        recyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void loadMovies(String sorting) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        if(checkNetworkAvailable()) {
            showMoviesDataView();
        }else{
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showErrorMessage();
        }
        String sort = sorting;
        if(sort.contains(POPULAR)) {
            mService.getMovie(API_KEY).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful()) {
                        if (mMoviesAdapter.getItemCount() != 0) {
                            mMoviesAdapter.clear();
                        }
                        mMoviesAdapter.setMoviesData(response.body().getResults());
                    } else {
                        int statusCode = response.code();
                        String message = getResources().getString(R.string.response_error_message) + statusCode;
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    String message = getResources().getString(R.string.response_error_message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                }
            });
        }else if(sort.contains(TOP_RATED)){
            mService.getMovieTopRated(API_KEY).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    if (response.isSuccessful()) {
                        if (mMoviesAdapter.getItemCount() != 0) {
                            mMoviesAdapter.clear();
                        }
                        mMoviesAdapter.setMoviesData(response.body().getResults());
                    } else {
                        int statusCode = response.code();
                        String message = getResources().getString(R.string.response_error_message) + statusCode;
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    String message = getResources().getString(R.string.response_error_message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }else if(sort.contains(FAVORITES)){
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            showMoviesDataView();
            getSupportLoaderManager().initLoader(1, null, this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_popular:
                mSorting = POPULAR;
                mSubtitle = popSub;
                mToolbar.setSubtitle(mSubtitle);
                saveSorting();
                loadMovies(mSorting);
                break;
            case R.id.nav_top_rated:
                mSorting = TOP_RATED;
                mSubtitle = topSub;
                mToolbar.setSubtitle(mSubtitle);
                saveSorting();
                loadMovies(mSorting);
                break;
            case R.id.nav_favorite:
                mSorting = FAVORITES;
                mSubtitle = favSub;
                mToolbar.setSubtitle(mSubtitle);
                saveSorting();
                loadMovies(mSorting);
                break;
            default:
                Log.w(TAG, "Unknown drawer item selected");
                break;
        }

        mNavItemId = item.getItemId();
        item.setChecked(true);
        saveSorting();
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(MovieResult selectedMovie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MovieResult.TAG_MOVIES, selectedMovie);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mMoviesAdapter.getItemCount() != 0) {
            mMoviesAdapter.clear();
        }
        if(data.getCount()>0) {
            mMoviesAdapter.setMoviesData(getMoviesFromCursor(data));
        }else{
            Toast.makeText(this, getString(R.string.no_favorites_data), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private List<MovieResult> getMoviesFromCursor(Cursor cursor) {

        List<MovieResult> movies = new ArrayList<>();

        if (cursor != null) {
            if (cursor.moveToFirst()){
                do{
                    MovieResult movie = new MovieResult(
                            cursor.getInt(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_ID)),
                            cursor.getString(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_TITLE)),
                            cursor.getString(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE)),
                            cursor.getString(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH)),
                            cursor.getDouble(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE)),
                            cursor.getString(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW)),
                            cursor.getString(cursor.getColumnIndex(
                                    MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH))
                    );
                    movies.add(movie);
                }while(cursor.moveToNext());
            }
        }
        return movies;
    }

    public void saveSorting(){
        SharedPreferences sort = context.getSharedPreferences("sortorder", MODE_PRIVATE);
        SharedPreferences.Editor edit = sort.edit();
        edit.clear();
        edit.putString("order", mSorting);
        edit.putString("subtitle", mSubtitle);
        edit.putInt(NAV_ITEM_ID, mNavItemId);
        edit.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }
}
