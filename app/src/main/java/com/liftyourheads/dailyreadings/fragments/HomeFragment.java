package com.liftyourheads.dailyreadings.fragments;
import com.crashlytics.android.Crashlytics;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.activities.LearningHeaderActivity;
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
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curCalendar;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curDay;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curMonth;
import static com.liftyourheads.dailyreadings.activities.MainActivity.curReading;
import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;

public class HomeFragment extends Fragment {

    private View view;
    public ReadingSummaryRecyclerViewAdapter readingNamesAdapter;
    RecyclerView readingNamesRecyclerView;

    ConstraintLayout commandment_constraint_layout;
    ConstraintLayout learning_constraint_layout;
    ImageButton settings;
    ImageButton date_button;
    public HashMap<String,String> quote;
    Calendar readingCalendar;
    TextView date_tv;
    private static final String QUOTES_DB = "CommandmentsOfChrist";
    private static final String QUOTES_TABLE = "Commandments_Of_Christ";
    private static String TAG = "Home Fragment";

    static DatePickerDialog curDatePickerDialog;

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

        commandment_constraint_layout = view.findViewById(R.id.commandment_constraint_layout);
        learning_constraint_layout = view.findViewById(R.id.learning_constraint_layout);
        settings = view.findViewById(R.id.settings_button);
        date_tv = view.findViewById(R.id.date_textView);
        date_button = view.findViewById(R.id.date_button);

        readingNamesRecyclerView = view.findViewById(R.id.reading_title_recycler);

        updateTitlesRecyclerView();

        updateQuotesView();

        setDateText(curCalendar);

        initialiseListeners();

        return view;

    }

    public void updateQuotesView(){
        TextView title_commandment_textview = view.findViewById(R.id.title_learning_textview);
        TextView body_commandment_textview = view.findViewById(R.id.body_commandment_textview);
        TextView ref_commandment_textview = view.findViewById(R.id.ref_commandment_textview);

        getQuote();
        MainActivity.initialiseDialogFragment(getContext(), quote.get("References"));

        title_commandment_textview.setText(quote.get("Title"));
        body_commandment_textview.setText(quote.get("Quote"));
        ref_commandment_textview.setText(quote.get("References"));

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
                Crashlytics.log(Log.DEBUG,TAG,"Settings Button Pressed");
                Intent settingsIntent = new Intent(view.getContext(), SettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("curDay",curDay);
                bundle.putInt("curMonth",curMonth);

                startActivity(settingsIntent,bundle);
            }
        });

        date_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GregorianCalendar today = new GregorianCalendar(TimeZone.getDefault());

                int year = today.get(Calendar.YEAR);
                int monthOfYear = today.get(Calendar.MONTH);
                int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

                curCalendar = today;
                curDatePickerDialog.updateDate(year,monthOfYear,dayOfMonth);

                setDateText(today);

                updateEverything(monthOfYear,dayOfMonth);

            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                Crashlytics.log(Log.DEBUG, TAG, "New Date Selected: Month = " + Integer.toString(monthOfYear) + ", Day = " + Integer.toString(dayOfMonth));

                // TODO Auto-generated method stub
                curCalendar.set(Calendar.YEAR, year);
                curCalendar.set(Calendar.MONTH, monthOfYear);
                curCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                curDatePickerDialog.updateDate(year,monthOfYear,dayOfMonth);

                setDateText(curCalendar);

                updateEverything(monthOfYear,dayOfMonth);

            }
        };

        curDatePickerDialog = new DatePickerDialog(getContext(),
                date,
                curCalendar.get(Calendar.YEAR),
                curCalendar.get(Calendar.MONTH),
                curCalendar.get(Calendar.DAY_OF_MONTH));

        View.OnClickListener dateSelector = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"Date change listener activated");
                curDatePickerDialog.show();
            }
        };

        date_tv.setOnClickListener(dateSelector);

        commandment_constraint_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.showDialogFragment(quote.get("Quote"),quote.get("References"));
            }
        });

        learning_constraint_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Crashlytics.log(Log.DEBUG,TAG,"Learning Button Pressed");
                Intent settingsIntent = new Intent(view.getContext(), LearningHeaderActivity.class);
                startActivity(settingsIntent);

            }

        });

    }

    private void updateEverything(int monthOfYear, int dayOfMonth){

        MainActivity.setActivityRecreated(dayOfMonth,monthOfYear);
        MainActivity.getCurrentDate();
        MainActivity.initialiseReadings(getContext());
        MainActivity.updateTabNames(getContext());
        updateTitlesRecyclerView();
        MainActivity.mapFragment.updateMap();

        ReadingFragment readingFragment = (ReadingFragment) MainActivity.fragments.get("Reading " + Integer.toString(curReading));
        CommentsFragment commentsFragment = (CommentsFragment) MainActivity.fragments.get("Comments " + Integer.toString(curReading));

        readingFragment.setBibleListContent();
        commentsFragment.updateNotesListView();

    }

    public void setDateText(Calendar calendar){

        String dateFormat = "MMM d"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());

        date_tv.setText(sdf.format(calendar.getTime()));

    }


    public void getQuote() {

        MainActivity.checkDatabase(getContext(),QUOTES_DB);

        SQLiteDatabase commandmentsDB = getContext().openOrCreateDatabase(QUOTES_DB + ".db", MODE_PRIVATE, null);


        Cursor quotes = commandmentsDB.rawQuery("SELECT * FROM " + QUOTES_TABLE,null); //+ " WHERE Used = 'no'", null);
        //Log.i("Quotes Found", Integer.toString(quotes.getCount()));

        Random rand = new Random();
        int num = rand.nextInt(quotes.getCount());

        Crashlytics.log(Log.DEBUG,TAG,"Processing quote #" + Integer.toString(num));


        //Log.i("Random number chosen",Integer.toString(num));
        quotes.moveToPosition(num);
        quote = new HashMap<>();

        //Quote number
        quote.put("Number",quotes.getString(0));
        //Quote content
        quote.put("Quote",quotes.getString(1));
        //Quote references
        quote.put("References",quotes.getString(2));

        int position = Integer.parseInt(quote.get("Number"));

        String title = "";

        if (position < 11) title = "God";
        else if (position < 21) title = "Christ";
        else if (position < 31) title = "Believers";
        else if (position < 41) title = "Strangers";
        else if (position < 51) title = "Character";
        else if (position < 73) title = "Actions";
        else if (position < 81) title = "Thoughts/Speech";
        else if (position < 86) title = "Marriage";
        else if (position < 88) title = "Parenting";
        else if (position < 91) title = "Superiors";
        else if (position < 97) title = "Disobedient Believers";
        else if (position < 101) title = "Body of Believers";

        quote.put("Title",title);

        quotes.close();
        commandmentsDB.close();

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
