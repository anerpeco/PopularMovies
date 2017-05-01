package com.example.android.popmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popmovies.R;
import com.example.android.popmovies.model.TrailerResult;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by anerp_000 on 22/04/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {

    private List<TrailerResult> mTrailerList;

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  viewType
     * @return A new ReviewAdapterViewHolder that holds the View for each list item
     */
    @Override
    public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailer_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailerAdapter.ViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the trailer
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder The ViewHolder which should be updated to represent the
     *               contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(TrailerAdapter.ViewHolder holder, int position) {
        final TrailerResult trailer = mTrailerList.get(position);
        final Context context = holder.mView.getContext();

        holder.trailer = trailer;

        String thumbnailUrl = "http://img.youtube.com/vi/" + trailer.getKey() + "/0.jpg";

        Picasso.with(context)
                .load(thumbnailUrl)
                .placeholder((R.drawable.movie_placeholder))
                .error(R.drawable.movie_placeholder)
                .into(holder.mTrailerThumbnail);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
            }
        });
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our trailers
     */
    @Override
    public int getItemCount() {
        if (null == mTrailerList) return 0;
        return mTrailerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        final ImageView mTrailerThumbnail;
        public TrailerResult trailer;

        ViewHolder (View view){
            super(view);
            mTrailerThumbnail = (ImageView) view.findViewById(R.id.trailer_thumbnail);
            mView = view;
        }
    }

    public void clear() {
        mTrailerList.clear();
        notifyDataSetChanged();
    }

    /**
     * This method is used to set the trailers on a TrailerAdapter.
     * This is handy when we get new data from the web but don't want to create a
     * new TrailerAdapter to display it.
     *
     * @param trailerList The new Trailer list to be displayed.
     */
    public void setTrailerData(List<TrailerResult> trailerList) {
        if (null == mTrailerList) {
            mTrailerList = trailerList;
        } else {
            mTrailerList.addAll(trailerList);
        }
        notifyDataSetChanged();
    }
}
