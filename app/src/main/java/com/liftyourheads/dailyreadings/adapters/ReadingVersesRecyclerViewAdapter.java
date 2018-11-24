package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liftyourheads.dailyreadings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ReadingVersesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String[] chapterTitles;
    private ArrayList<HashMap<String, String>> reading;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final static int TITLE = 0;
    private final static int VERSE = 1;

    // data is passed into the constructor
    public ReadingVersesRecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> verses) {
        this.mInflater = LayoutInflater.from(context);
        this.reading = verses;
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case VERSE:
                view = mInflater.inflate(R.layout.recyclerview_verse, parent, false);
                return new ViewHolderVerse(view);
            case TITLE:
                view = mInflater.inflate(R.layout.recyclerview_chapter_title, parent, false);
                return new ViewHolderChapterTitle(view);
        }

        return null;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case VERSE:
                HashMap<String,String> verse = this.reading.get(position);
                ViewHolderVerse verseHolder = (ViewHolderVerse) holder;
                verseHolder.numberTV.setText(verse.get("verseNum"));
                verseHolder.contentTV.setText(verse.get("verseContent"));
                break;

            case TITLE:
                ViewHolderChapterTitle titleHolder = (ViewHolderChapterTitle) holder;
                titleHolder.titleTV.setText(this.reading.get(position).get("Title").toUpperCase());
                break;
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return this.reading.size();
    }


    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous

        if (reading.get(position).get("Chapter Header") != null ) return TITLE;
        else return VERSE;

        //TODO: More accurate viewtypes!

    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolderVerse extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView numberTV;
        TextView contentTV;

        ViewHolderVerse(View itemView) {
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

    public class ViewHolderChapterTitle extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTV;

        ViewHolderChapterTitle(View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.chapterTitle_tv);
            titleTV.setOnClickListener(this);
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