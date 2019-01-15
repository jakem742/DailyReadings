package com.liftyourheads.dailyreadings.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.LearningHeaderActivity;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.adapters.LearningItemRecyclerViewAdapter;
import com.liftyourheads.dailyreadings.adapters.ReferencesRecyclerViewAdapter;
import com.liftyourheads.dailyreadings.utils.ReferenceProcessor;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class LearningItemFragment extends Fragment {
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
    private static Dialog refs_dialog;
    private static RelativeLayout quotes_dialog_background_rl;


    private OnFragmentInteractionListener mListener;

    public LearningItemFragment() {
        // Required empty public constructor
    }

    public static LearningItemFragment newInstance(String headingID, String headingText) {
        LearningItemFragment fragment = new LearningItemFragment();
        Bundle args = new Bundle();
        args.putString(HEADER_ID, headingID);
        args.putString(HEADER_TITLE, headingText);
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


        Log.i(TAG,"ID = " + headerID + ", Title = " + title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_learning_list, container, false);

        TextView headerTV = view.findViewById(R.id.subject_header_textView);

        Button back_button = view.findViewById(R.id.learning_item_back_button);
        back_button.setOnClickListener(view -> {

            Crashlytics.log(Log.DEBUG,TAG,"Learning Item Back Button Selected");
            Intent itemsIntent = new Intent(view.getContext(), LearningHeaderActivity.class);
            view.getContext().startActivity(itemsIntent);
        });

        headerTV.setText(title);

        getLearningContent();
        updateItemListView();

        checkReferences();

        // Inflate the layout for this fragment
        return view;
    }


    private void getLearningContent() {

        MainActivity.checkDatabase(getContext(), LEARNING_DB);
        Crashlytics.log(Log.DEBUG, TAG, "Processing items");

        SQLiteDatabase learningDB = getContext().openOrCreateDatabase(LEARNING_DB + ".db", MODE_PRIVATE, null);
        Cursor headersCursor = learningDB.rawQuery("SELECT * FROM " + ITEMS_TABLE + " WHERE HeadingID = " + headerID, null);

        items = new ArrayList<>();

        if (headersCursor.moveToFirst()) {

            int titleColumn = headersCursor.getColumnIndex("Text");
            int idColumn = headersCursor.getColumnIndex("SubHeadingID");
            int refColumn = headersCursor.getColumnIndex("Refs");

            do {

                HashMap<String,String> item = new HashMap<>();

                item.put("Title",headersCursor.getString(titleColumn));
                item.put("Refs",headersCursor.getString(refColumn));
                item.put("ID",headersCursor.getString(idColumn));

                items.add(item);

            } while (headersCursor.moveToNext());

            //Log.i(TAG,Arrays.toString(items.toArray()));

        } else {

            Log.i(TAG, "Couldn't find subject items in the database!");
        }

        headersCursor.close();
        learningDB.close();

    }

    public void updateItemListView() {

        this.itemRecyclerView = view.findViewById(R.id.itemRecyclerView);
        this.itemRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.learningItemAdapter = new LearningItemRecyclerViewAdapter(getContext(), items);
        this.itemRecyclerView.setAdapter(learningItemAdapter);
        //this.headerRecyclerView.setHasFixedSize(true);

    }

     public void checkReferences() {

         MainActivity.checkDatabase(getContext(), LEARNING_DB);
         Crashlytics.log(Log.DEBUG, TAG, "Processing items");

         SQLiteDatabase learningDB = getContext().openOrCreateDatabase(LEARNING_DB + ".db", MODE_PRIVATE, null);
         Cursor headersCursor = learningDB.rawQuery("SELECT * FROM " + ITEMS_TABLE, null);

         int refColumn = headersCursor.getColumnIndex("Refs");

         headersCursor.moveToFirst();

         do {

             ReferenceProcessor.processReferenceString(getContext(),headersCursor.getString(refColumn));

         } while (headersCursor.moveToNext());

    }


    public static void initialiseDialogFragment(Context context,String referenceString){
        refs_dialog = new Dialog(context,android.R.style.Theme_Translucent_NoTitleBar);
        refs_dialog.setContentView(R.layout.layout_commandment_popup_window);//your custom dialog layout.

        quotes_dialog_background_rl = refs_dialog.findViewById(R.id.custom_dialog_background_rl);
        quotes_dialog_background_rl.getBackground().setAlpha(170);

        ArrayList<HashMap<String,String>> references = ReferenceProcessor.processReferenceString(context,referenceString);


        RecyclerView referencesRecyclerView = refs_dialog.findViewById(R.id.references_recyclerview);
        referencesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        ReferencesRecyclerViewAdapter referencesAdapter = new ReferencesRecyclerViewAdapter(context, references);
        referencesRecyclerView.setAdapter(referencesAdapter);

    }


    public static void showDialogFragment(String quote,String verses) {
        try {
            Log.i(TAG,"Creating dialog");

            TextView quote_tv = refs_dialog.findViewById(R.id.quote_tv);
            quote_tv.setText(quote);


            refs_dialog.show();

            quotes_dialog_background_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refs_dialog.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
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
