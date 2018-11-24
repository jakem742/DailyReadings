package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;


public class ReadingSummaryRecyclerViewAdapter extends RecyclerView.Adapter<ReadingSummaryRecyclerViewAdapter.ViewHolder> {

    private String[] readings;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    // data is passed into the constructor
    public ReadingSummaryRecyclerViewAdapter(Context context, String[] readingNames) {
        this.mInflater = LayoutInflater.from(context);
        this.readings = readingNames;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_reading_name, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String readingName = readings[position];
        holder.readingTV.setText(readingName);
        holder.readingTV.setTag(position);
//        holder.commentTV.setText(comment.get("comment"));
    }

    // total number of rows
    @Override
    public int getItemCount() {
//        return comments.size();
        return readings.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView readingTV;
        //TextView commentTV;

        ViewHolder(View itemView) {
            super(itemView);
            readingTV = itemView.findViewById(R.id.reading_tv);
            //commentTV = itemView.findViewById(R.id.commentTV);
            readingTV.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

            MainActivity.onReadingClick(view, getAdapterPosition());

        }
    }

    // convenience method for getting data at click position
    public String getItem(int position) {
        return readings[position];
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