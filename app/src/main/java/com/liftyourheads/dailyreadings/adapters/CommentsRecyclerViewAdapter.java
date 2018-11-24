package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liftyourheads.dailyreadings.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap<String, String>> comments;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    // data is passed into the constructor
    public CommentsRecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.comments = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_comment, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String,String> comment = comments.get(position);
        holder.posterTV.setText(comment.get("poster"));
        holder.commentTV.setText(comment.get("comment"));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return comments.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView posterTV;
        TextView commentTV;

        ViewHolder(View itemView) {
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