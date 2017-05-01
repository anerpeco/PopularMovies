package com.example.android.popmovies.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popmovies.R;
import com.example.android.popmovies.db.MovieContract;
import com.example.android.popmovies.model.MovieResult;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment{

    private MovieResult mMovie;
    ImageView imageviewFavorite;
    Context context;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(MovieResult.TAG_MOVIES)) {
                mMovie = intent.getExtras().getParcelable(MovieResult.TAG_MOVIES);
            }
            setHasOptionsMenu(true);
        }
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        TextView mMovieTitleView = (TextView)rootView.findViewById(R.id.textview_original_title);
        TextView mMovieReleaseDateView = (TextView)rootView.findViewById(R.id.textview_release_date);
        TextView mMovieVoteAverageView = (TextView)rootView.findViewById(R.id.textview_vote_average);
        TextView mMovieOverviewView = (TextView)rootView.findViewById(R.id.textview_overview);
        ImageView mMoviePosterView = (ImageView)rootView.findViewById(R.id.imageview_poster);
        ImageView mMovieBackdropView = (ImageView)rootView.findViewById(R.id.imageView_backdrop);
        imageviewFavorite = (ImageView)rootView.findViewById(R.id.imageview_favourite);

        mMovieTitleView.setText(mMovie.getOriginalTitle());
        mMovieVoteAverageView.setText(mMovie.getVoteAverage().toString());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate());
        mMovieOverviewView.setMovementMethod(new ScrollingMovementMethod());
        mMovieOverviewView.setText(mMovie.getOverview());

        Picasso.with(getContext())
                .load(mMovie.buildPosterPath())
                .into(mMoviePosterView);

        Picasso.with(getContext())
                .load(mMovie.buildBackdropPath())
                .into(mMovieBackdropView);

        setUpFavoriteButton();

        return rootView;
    }

    private void setUpFavoriteButton(){

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return isFavorite();
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                if (isFavorite) {
                    imageviewFavorite.setImageDrawable(ContextCompat.getDrawable(context.getApplicationContext(), android.R.drawable.btn_star_big_on));
                } else {
                    imageviewFavorite.setImageDrawable(ContextCompat.getDrawable(context.getApplicationContext(), android.R.drawable.btn_star_big_off));
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        imageviewFavorite.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isFavorite()) {
                            String message = getResources().getString(R.string.removed_from_favorites);
                            removeFromFavorites();
                            Toast.makeText(context.getApplicationContext(),message,Toast.LENGTH_SHORT).show();

                        }else {
                            String message = getResources().getString(R.string.added_to_favorites);
                            markAsFavorite();
                            Toast.makeText(context.getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isFavorite() {

        Cursor movieCursor = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                null,
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }

    public void markAsFavorite() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!isFavorite()) {
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            mMovie.getId());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                            mMovie.getOriginalTitle());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH,
                            mMovie.getPosterPath());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
                            mMovie.getOverview());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
                            mMovie.getVoteAverage());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                            mMovie.getReleaseDate());
                    movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH,
                            mMovie.getBackdropPath());
                    context.getContentResolver().insert(
                            MovieContract.MovieEntry.CONTENT_URI,
                            movieValues
                    );
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setUpFavoriteButton();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void removeFromFavorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    context.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(), null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setUpFavoriteButton();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
