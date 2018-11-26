package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liftyourheads.dailyreadings.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<HashMap<String, String>> comments;
    LayoutInflater mInflater;
    ItemClickListener mClickListener;
    private final static int TITLE = 0;
    private final static int COMMENT = 1;

    // data is passed into the constructor
    public CommentsRecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.comments = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case COMMENT:
                view = mInflater.inflate(R.layout.recyclerview_comment, parent, false);
                return new ViewHolderComment(view);
            case TITLE:
                view = mInflater.inflate(R.layout.recyclerview_title, parent, false);
                return new ViewHolderTitle(view);
        }

        return null;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case COMMENT:
                HashMap<String, String> comment = comments.get(position);
                ViewHolderComment commentHolder = (ViewHolderComment) holder;
                commentHolder.posterTV.setText(comment.get("poster"));
                commentHolder.commentTV.setText(comment.get("comment"));
                break;

            case TITLE:

                ViewHolderTitle titleHolder = (ViewHolderTitle) holder;
                titleHolder.titleTV.setText(this.comments.get(position).get("Title").toUpperCase());
                break;
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return this.comments.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolderComment extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView posterTV;
        TextView commentTV;

        ViewHolderComment(View itemView) {
            super(itemView);
            posterTV = itemView.findViewById(R.id.posterTV);
            commentTV = itemView.findViewById(R.id.commentTV);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public class ViewHolderTitle extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTV;

        ViewHolderTitle(View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.chapterTitle_tv);
            titleTV.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous

        if (comments.get(position).get("Chapter Header") != null ) return TITLE;
        else return COMMENT;

    }

    // convenience method for getting data at click position
    public String[] getItem(int id) {
        return ( new String[] { comments.get(id).get("comment") , comments.get(id).get("poster") } );

    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}