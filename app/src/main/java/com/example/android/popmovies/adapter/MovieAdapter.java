package com.example.android.popmovies.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popmovies.R;
import com.example.android.popmovies.model.MovieResult;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by anerp_000 on 18/04/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MoviesAdapterViewHolder> {

    private List<MovieResult> mMovieList;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private MovieAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(MovieResult selectedMovie);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final CardView mPopularMovieCardView;
        final ImageView mMoviePosterImageView;
        Context mContext;

        MoviesAdapterViewHolder(View view) {
            super(view);
            mPopularMovieCardView = (CardView) view.findViewById(R.id.cv_popular_movie);
            mMoviePosterImageView = (ImageView) view.findViewById(R.id.iv_movie_poster);
            view.setOnClickListener(this);
            mContext = view.getContext();
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param view The View that was clicked
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            MovieResult currentMovie = mMovieList.get(adapterPosition);
            mClickHandler.onClick(currentMovie);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent The ViewGroup that these ViewHolders are contained within.
     * @param viewType  viewType
     * @return A new MoviesAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapter.MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movies
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(MovieAdapter.MoviesAdapterViewHolder holder, int position) {
        MovieResult movie = mMovieList.get(position);
        Picasso.with(holder.mContext)
                .load(movie.buildPosterPath())
                .into(holder.mMoviePosterImageView);

    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our movies
     */
    @Override
    public int getItemCount() {
        if (null == mMovieList) return 0;
        return mMovieList.size();
    }

    public void clear() {
        mMovieList.clear();
        notifyDataSetChanged();
    }

    /**
     * This method is used to set the movies on a MovieAdapter.
     * This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param movieList The new Movie list to be displayed.
     */
    public void setMoviesData(List<MovieResult> movieList) {
        if (null == mMovieList) {
            mMovieList = movieList;
        } else {
            mMovieList.addAll(movieList);
        }
        notifyDataSetChanged();
    }
}
