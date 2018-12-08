package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;


public class ReferencesRecyclerViewAdapter extends RecyclerView.Adapter<ReferencesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap<String, String>> references;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    // data is passed into the constructor
    public ReferencesRecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.references = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_reference, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String referenceName = references.get(position).get("Long Name");
        String referenceText = references.get(position).get("Text");
        holder.quoteTV.setText(referenceText);
        holder.referenceTV.setText(referenceName);
        holder.referenceTV.setTag(position);
//        holder.commentTV.setText(comment.get("comment"));
    }

    // total number of rows
    @Override
    public int getItemCount() {
//        return comments.size();
        if (references != null) {
            return references.size();
        } else return 0;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView quoteTV;
        TextView referenceTV;

        ViewHolder(View itemView) {
            super(itemView);
            quoteTV = itemView.findViewById(R.id.quoteTV);
            referenceTV = itemView.findViewById(R.id.referenceTV);
            quoteTV.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

            MainActivity.onReadingClick(view, getAdapterPosition());

        }
    }

    // convenience method for getting data at click position
    public HashMap<String, String> getItem(int position) {
        return references.get(position);
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