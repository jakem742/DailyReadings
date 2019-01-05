package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.liftyourheads.dailyreadings.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LearningHeaderRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<HashMap<String,String>> headers;
    LayoutInflater mInflater;
    ItemClickListener mClickListener;
    private final static int HEADER = 0;
    private static final String TAG = "LearningHeaderRecyclerViewAdapter";

    // data is passed into the constructor
    public LearningHeaderRecyclerViewAdapter(Context context, ArrayList<HashMap<String,String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.headers = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case HEADER:
                view = mInflater.inflate(R.layout.recyclerview_learning_header, parent, false);
                return new ViewHolderHeader(view);
        }

        return null;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case HEADER:
                HashMap<String,String> header = headers.get(position);
                ViewHolderHeader headerHolder = (ViewHolderHeader) holder;
                headerHolder.headerTV.setText(header.get("Title"));
                //headerHolder.refTV.setText(header.get("Ref"));
                if (header.get("ID") != null) headerHolder.ID = Integer.parseInt(header.get("ID"));
                break;

        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return this.headers.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolderHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView headerTV;
        //TextView refTV;
        Integer ID;
        ConstraintLayout learning_header_constraint_layout;

        ViewHolderHeader(View itemView) {
            super(itemView);
            headerTV = itemView.findViewById(R.id.headerTV);
            //refTV = itemView.findViewById(R.id.refTV);
            headerTV.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            Toast.makeText(view.getContext(), headers.get(getAdapterPosition()).get("Title"), Toast.LENGTH_SHORT).show();

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

        return HEADER;
        //if (comments.get(position).get("Chapter Header") != null ) return HEADER;
        //else return COMMENT;

    }

    // convenience method for getting data at click position
    public String[] getItem(int id) {
        return (
                new String[] {
                        headers.get(id).get("Title"),
                        headers.get(id).get("Ref"),
                        headers.get(id).get("ID")
                } );

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