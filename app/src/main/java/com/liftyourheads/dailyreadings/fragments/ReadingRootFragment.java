package com.liftyourheads.dailyreadings.fragments;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ReadingRootFragment extends Fragment {

    static String TAG = "Reading Root Fragment";

    public ReadingRootFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ReadingRootFragment newInstance() {
        ReadingRootFragment fragment = new ReadingRootFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.root_frame_reading, container, false);


        MainActivity.fragmentManager.beginTransaction()
                .replace(R.id.root_frame_reading, MainActivity.fragments.get("Reading " + MainActivity.curReading))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        Log.i(TAG,"Number of Fragments Found: " + Integer.toString(MainActivity.fragmentManager.getFragments().size()));

        return view;
    }
}
