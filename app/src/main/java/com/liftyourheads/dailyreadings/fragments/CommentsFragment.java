package com.liftyourheads.dailyreadings.fragments;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.adapters.CommentsRecyclerViewAdapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;

public class CommentsFragment extends Fragment {

    private static final String READING = "ReadingNumber";
    private Integer readingNum;
    private static String TAG = "Comments Fragment";
    public CommentsRecyclerViewAdapter commentsAdapter;
    RecyclerView readingCommentsRecyclerView;
    View view;

    public CommentsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CommentsFragment newInstance(int readingNumber) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putInt(READING, readingNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            readingNum = getArguments().getInt(READING, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_comments, container, false);
        }

        Log.i(TAG,"Processing reading comments: " + readingNum.toString());

        updateNotesListView();

        return view;
    }

    public void updateNotesListView() {

        ArrayList<HashMap<String,String>> comments = reading[readingNum].getReadingComments();


        this.readingCommentsRecyclerView = view.findViewById(R.id.readingCommentsRecyclerView);
        this.readingCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.commentsAdapter = new CommentsRecyclerViewAdapter(getActivity(), comments);
        this.readingCommentsRecyclerView.setAdapter(commentsAdapter);
        this.readingCommentsRecyclerView.setHasFixedSize(true);

    }

}
