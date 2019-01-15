package com.liftyourheads.dailyreadings.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.fragments.LearningItemFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class LearningItemRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<HashMap<String,String>> items;
    LayoutInflater mInflater;
    ItemClickListener mClickListener;
    private final static int ITEM = 0;
    private static final String TAG = "LearningHeaderRecyclerViewAdapter";

    // data is passed into the constructor
    public LearningItemRecyclerViewAdapter(Context context, ArrayList<HashMap<String,String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.items = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case ITEM:
                view = mInflater.inflate(R.layout.recyclerview_learning_item, parent, false);
                return new ViewHolderItem(view);
        }

        return null;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case ITEM:
                HashMap<String,String> item = items.get(position);
                ViewHolderItem itemHolder = (ViewHolderItem) holder;
                if (item != null) {
                    itemHolder.itemTV.setText(item.get("Title"));
                    itemHolder.refsTV.setText(item.get("Refs"));
                    if (item.get("ID") != null) itemHolder.ID = Integer.parseInt(item.get("ID"));
                }
                break;

        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return this.items.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolderItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView itemTV;
        TextView refsTV;
        FrameLayout clickFL;
        Integer ID;

        ViewHolderItem(View itemView) {
            super(itemView);
            itemTV = itemView.findViewById(R.id.item_textView);
            refsTV = itemView.findViewById(R.id.refs_textView);
            clickFL = itemView.findViewById(R.id.click_FL);
            //refTV = itemView.findViewById(R.id.refTV);
            clickFL.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

            LearningItemFragment.initialiseDialogFragment(view.getContext(),refsTV.getText().toString());
            LearningItemFragment.showDialogFragment(itemTV.getText().toString(),refsTV.getText().toString());
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

        return ITEM;
        //if (comments.get(position).get("Chapter Header") != null ) return HEADER;
        //else return COMMENT;

    }

    // convenience method for getting data at click position
    public String[] getItem(int id) {
        return (
                new String[] {
                        items.get(id).get("Text"),
                        items.get(id).get("Refs"),
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