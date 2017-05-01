package com.example.android.popmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popmovies.R;
import com.example.android.popmovies.model.ReviewResult;

import java.util.List;

/**
 * Created by anerp_000 on 21/04/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewResult> mReviewList;
    private Context context;

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  viewType
     * @return A new ReviewAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewAdapter.ViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the review
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder The ViewHolder which should be updated to represent the
     *               contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
        ReviewResult review = mReviewList.get(position);

        holder.mReviewAuthorTextView.setText(review.getAuthor());
        holder.mReviewContentTextView.setText(review.getContent());
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our reviews
     */
    @Override
    public int getItemCount() {
        if (null == mReviewList) return 0;
        return mReviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView mReviewAuthorTextView;
        final TextView mReviewContentTextView;

        ViewHolder (View view){
            super(view);
            mReviewAuthorTextView = (TextView)view.findViewById(R.id.review_author);
            mReviewContentTextView = (TextView)view.findViewById(R.id.review_content);
            context = view.getContext();
        }
    }

    public void clear() {
        mReviewList.clear();
        notifyDataSetChanged();
    }

    /**
     * This method is used to set the reviews on a ReviewAdapter.
     * This is handy when we get new data from the web but don't want to create a
     * new ReviewAdapter to display it.
     *
     * @param reviewList The new Review list to be displayed.
     */
    public void setReviewData(List<ReviewResult> reviewList) {
        if (null == mReviewList) {
            mReviewList = reviewList;
        } else {
            mReviewList.addAll(reviewList);
        }
        notifyDataSetChanged();
    }
}
