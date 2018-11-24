package com.liftyourheads.dailyreadings.fragments;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.liftyourheads.dailyreadings.activities.MainActivity.curReading;


public class CommentsRootFragment extends Fragment {

    public CommentsRootFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CommentsRootFragment newInstance() { return new CommentsRootFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.root_frame_comments, container, false);

        MainActivity.fragmentManager.beginTransaction()
                .replace(R.id.root_frame_comments, MainActivity.fragments.get("Comments " + curReading))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        return view;
    }
}
