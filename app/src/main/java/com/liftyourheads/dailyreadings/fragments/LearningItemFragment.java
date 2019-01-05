package com.liftyourheads.dailyreadings.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.adapters.LearningItemRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class LearningItemFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String HEADER_ID = "ID";
    private static final String HEADER_TITLE = "Title";
    private static final String LEARNING_DB = "LearningContent";
    private static final String ITEMS_TABLE = "Content";
    private static final String TAG = "LearningItemFragment";

    private View view;
    private String headerID;
    private String title;
    private ArrayList<HashMap<String,String>> items;
    private RecyclerView itemRecyclerView;
    private LearningItemRecyclerViewAdapter learningItemAdapter;

    private OnFragmentInteractionListener mListener;

    public LearningItemFragment() {
        // Required empty public constructor
    }

    public static LearningItemFragment newInstance(String headerID, String title) {
        LearningItemFragment fragment = new LearningItemFragment();
        Bundle args = new Bundle();
        args.putString(HEADER_ID, headerID);
        args.putString(HEADER_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            headerID = getArguments().getString(HEADER_ID);
            title = getArguments().getString(HEADER_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_learning_list, container, false);

        TextView headerTV = view.findViewById(R.id.subject_header_textView);
        headerTV.setText(title);

        getLearningContent();
        updateItemListView();

        // Inflate the layout for this fragment
        return view;
    }


    public void updateItemListView() {

        this.itemRecyclerView = view.findViewById(R.id.itemRecyclerView);
        this.itemRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.learningItemAdapter = new LearningItemRecyclerViewAdapter(getContext(), items);
        this.itemRecyclerView.setAdapter(learningItemAdapter);
        //this.headerRecyclerView.setHasFixedSize(true);

    }


    private void getLearningContent() {

        MainActivity.checkDatabase(getContext(), LEARNING_DB);
        Crashlytics.log(Log.DEBUG, TAG, "Processing items");

        SQLiteDatabase learningDB = getContext().openOrCreateDatabase(LEARNING_DB + ".db", MODE_PRIVATE, null);
        Cursor headersCursor = learningDB.rawQuery("SELECT * FROM " + ITEMS_TABLE + " WHERE HeadingID = " + headerID, null);

        items = new ArrayList<>();

        if (headersCursor.moveToFirst()) {

            int titleColumn = headersCursor.getColumnIndex("Text");
            int idColumn = headersCursor.getColumnIndex("Subheading");
            int refColumn = headersCursor.getColumnIndex("References");

            do {

                HashMap<String,String> item = new HashMap<>();

                item.put("Title",headersCursor.getString(titleColumn));
                item.put("Refs",headersCursor.getString(refColumn));
                item.put("ID",headersCursor.getString(idColumn));

                items.add(item);

            } while (headersCursor.moveToNext());

        } else {

            Log.i(TAG, "Couldn't find subject headers in the database!");
        }

        headersCursor.close();
        learningDB.close();

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
