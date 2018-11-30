package com.liftyourheads.dailyreadings.fragments;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.liftyourheads.dailyreadings.activities.SettingsActivity;
import com.liftyourheads.dailyreadings.adapters.ReadingSummaryRecyclerViewAdapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static com.liftyourheads.dailyreadings.activities.MainActivity.fragmentManager;
import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;

public class HomeFragment extends Fragment {

    private View view;
    public ReadingSummaryRecyclerViewAdapter readingNamesAdapter;
    RecyclerView readingNamesRecyclerView;

    ImageButton settings;
    Calendar readingCalendar;
    TextView date_tv;
    private static final String QUOTES_DB = "CommandmentsOfChrist.db";
    private static final String QUOTES_TABLE = "Commandments_Of_Christ";
    private static String TAG = "Home Fragment";

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {

        return new HomeFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view == null) view = inflater.inflate(R.layout.fragment_home, container, false);

        //TextView date_textview = view.findViewById(R.id.date_textview);
        TextView title_commandment_textview = view.findViewById(R.id.title_commandment_textview);
        TextView body_commandment_textview = view.findViewById(R.id.body_commandment_textview);
        TextView ref_commandment_textview = view.findViewById(R.id.ref_commandment_textview);
        settings = view.findViewById(R.id.settings_button);
        this.readingCalendar  = Calendar.getInstance();
        date_tv = view.findViewById(R.id.date_textView);

        readingNamesRecyclerView = view.findViewById(R.id.reading_title_recycler);




        //String date = MONTHS[curMonth] + " " + Integer.toString(curDay);
        //date_textview.setText(date);
        updateTitlesRecyclerView();

        String[] quote = getQuote();
        String section;

        int position = Integer.parseInt(quote[0]);

        if (position < 11) section = "God";
        else if (position < 21) section = "Christ";
        else if (position < 31) section = "Believers";
        else if (position < 41) section = "Strangers";
        else if (position < 51) section = "Character";
        else if (position < 73) section = "Actions";
        else if (position < 81) section = "Thoughts/Speech";
        else if (position < 86) section = "Marriage";
        else if (position < 88) section = "Parenting";
        else if (position < 91) section = "Superiors";
        else if (position < 97) section = "Disobedient Believers";
        else if (position < 101) section = "Body of Believers";
        else section = "Unknown";


        title_commandment_textview.setText(section);
        body_commandment_textview.setText(quote[1]);
        ref_commandment_textview.setText(quote[2]);
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        setDateText(cal);

        initialiseListeners();

        return view;

    }


    public void updateTitlesRecyclerView() {


        String[] readings = new String[3];

        for (int i = 0; i < 3; i++) {
            readings[i] = reading[i].getFullName();
        }

        this.readingNamesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        this.readingNamesAdapter = new ReadingSummaryRecyclerViewAdapter(getActivity(), readings);
        this.readingNamesRecyclerView.setAdapter(readingNamesAdapter);

    }
    public void initialiseListeners() {

        settings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(view.getContext(), SettingsActivity.class);

                startActivity(settingsIntent);
            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                readingCalendar.set(Calendar.YEAR, year);
                readingCalendar.set(Calendar.MONTH, monthOfYear);
                readingCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                setDateText(readingCalendar);


                MainActivity.recreateActivity(getActivity(),monthOfYear,dayOfMonth);
                //MainActivity.initialiseReadings(view.getContext(),dayOfMonth,monthOfYear);
                //MainActivity.initialiseTabs();
                //MainActivity.generateReadingFragments();

            }
        };

        View.OnClickListener dateSelector = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"Date change listener activated");
                new DatePickerDialog(getContext(),
                        date,
                        readingCalendar.get(Calendar.YEAR),
                        readingCalendar.get(Calendar.MONTH),
                        readingCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        };

        //date_tv.setOnClickListener(dateSelector);

    }

    public void setDateText(Calendar calendar){

        String dateFormat = "MMM d"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());

        date_tv.setText(sdf.format(calendar.getTime()));

    }


    public String[] getQuote() {

        MainActivity.checkDatabase(getContext(),QUOTES_DB);

        SQLiteDatabase commandmentsDB = getContext().openOrCreateDatabase(QUOTES_DB, MODE_PRIVATE, null);


        Cursor quotes = commandmentsDB.rawQuery("SELECT * FROM " + QUOTES_TABLE + " WHERE Used = 'no'", null);
        //Log.i("Quotes Found", Integer.toString(quotes.getCount()));

        Random rand = new Random();
        int num = rand.nextInt(quotes.getCount());

        //Log.i("Random number chosen",Integer.toString(num));

        quotes.moveToPosition(num);
        String[] quote = new String[3];

        //Quote number
        quote[0] = quotes.getString(0);
        //Quote content
        quote[1] = quotes.getString(1);
        //Quote references
        quote[2] = quotes.getString(2);

        //Update comment timestampe
        //ContentValues timeStamp = new ContentValues();
        //timeStamp.put("Used",System.nanoTime());
        //commandmentsDB.update(QUOTES_TABLE,timeStamp,"LIMIT ?,1", new String[] {Integer.toString(num-1)});

        //Log.i("Quote",quote[0] + " " + quote[1]);

        quotes.close();
        commandmentsDB.close();

        return quote;

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
