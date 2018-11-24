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

public class ReadingVersesRecyclerViewAdapter extends RecyclerView.Adapter<ReadingVersesRecyclerViewAdapter.ViewHolder> {

    private ArrayList<HashMap<String, String>> reading;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    // data is passed into the constructor
    public ReadingVersesRecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.reading = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_verse, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String,String> verse = this.reading.get(position);
        holder.numberTV.setText(verse.get("verseNum"));
        holder.contentTV.setText(verse.get("verseContent"));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return this.reading.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView numberTV;
        TextView contentTV;

        ViewHolder(View itemView) {
            super(itemView);
            numberTV = itemView.findViewById(R.id.verseNum);
            contentTV = itemView.findViewById(R.id.verseContent);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String[] getItem(int id) {
        return ( new String[] { reading.get(id).get("verseNum") , reading.get(id).get("verseContent") } );

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