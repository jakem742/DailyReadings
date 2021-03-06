package com.liftyourheads.dailyreadings.fragments;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.adapters.ReadingVersesRecyclerViewAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ReadingFragment extends Fragment {

    private static final String READING = "ReadingNumber";
    private Integer readingNum;
    private static String TAG = "Reading Fragment";
    public ReadingVersesRecyclerViewAdapter readingAdapter;
    private View view;

    private OnFragmentInteractionListener mListener;

    public ReadingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ReadingFragment newInstance(int readingNumber) {
        ReadingFragment fragment = new ReadingFragment();
        Bundle args = new Bundle();
        args.putInt(READING, readingNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG,"Creating reading fragment");
        if (getArguments() != null) {
            this.readingNum = getArguments().getInt(READING, 0);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (this.view == null) {
            this.view = inflater.inflate(R.layout.fragment_reading, container, false);
        }

        Log.i(TAG,"Processing bible verses: " + this.readingNum.toString());


        setBibleListContent();

        return view;

    }

    public void notifyUpdateRecyclerView(){
        readingAdapter.notifyDataSetChanged();
    }


    public void setBibleListContent() {

        ArrayList<HashMap<String,String>> reading = MainActivity.reading[this.readingNum].getReadingVerses();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean paragraphs = sharedPreferences.getBoolean("paragraphs",false);

        NestedScrollView readingScrollView = view.findViewById(R.id.biblePassageScrollView);
        TextView readingTextView = view.findViewById(R.id.biblePassageTextView);
        RecyclerView readingRecyclerView = view.findViewById(R.id.readingRecyclerView);

        if (paragraphs) {

            readingScrollView.setVisibility(View.VISIBLE);
            readingTextView.setVisibility(View.VISIBLE);
            readingRecyclerView.setVisibility(View.GONE);

            readingTextView.setText(Html.fromHtml(MainActivity.reading[readingNum].getVerseParagraphs()));


        } else {

            readingScrollView.setVisibility(View.INVISIBLE);
            readingTextView.setVisibility(View.INVISIBLE);
            readingRecyclerView.setVisibility(View.VISIBLE);

            readingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            readingAdapter = new ReadingVersesRecyclerViewAdapter(getActivity(), reading);
            //readingAdapter.setClickListener(this);
            readingRecyclerView.setAdapter(readingAdapter);
            readingRecyclerView.setHasFixedSize(true);
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
