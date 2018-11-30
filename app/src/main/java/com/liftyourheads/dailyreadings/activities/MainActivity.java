package com.liftyourheads.dailyreadings.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.adapters.ReadingSummaryRecyclerViewAdapter;
import com.liftyourheads.dailyreadings.fragments.CommentsRootFragment;
import com.liftyourheads.dailyreadings.fragments.HomeFragment;
import com.liftyourheads.dailyreadings.fragments.MapFragment;
import com.liftyourheads.dailyreadings.fragments.ReadingFragment;
import com.liftyourheads.dailyreadings.fragments.CommentsFragment;
import com.liftyourheads.dailyreadings.fragments.ReadingRootFragment;
import com.liftyourheads.dailyreadings.models.Readings;
import com.liftyourheads.dailyreadings.utils.CustomViewPager;
import com.liftyourheads.dailyreadings.utils.DataBaseHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        ReadingFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener {

    public static final String[] BIBLE = {"Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi", "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"};
    public static final String[] BIBLE_ABBR = {"Gen", "Ex", "Lev", "Num", "Deut", "Josh", "Judg", "Ruth", "1 Sam", "2 Sam", "1 Kgs", "2 Kgs", "1 Chr", "2 Chr", "Ezra", "Neh", "Est", "Job", "Ps", "Pro", "Eccl", "Sng", "Isa", "Jer", "Lam", "Ezek", "Dan", "Hos", "Joel", "Amos", "Obad", "Jonah", "Mic", "Nahum", "Hab", "Zeph", "Hag", "Zech", "Mal", "Matt", "Mark", "Luke", "John", "Acts", "Rom", "1 Cor", "2 Cor", "Gal", "Eph", "Phil", "Col", "1 Thes", "2 Thes", "1 Tim", "2 Tim", "Titus", "Phm", "Heb", "James", "1 Pet", "2 Pet", "1 Jn", "2 Jn", "3 Jn", "Jude", "Rev"};
    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public static final String[] TRANSLATIONS = {"KJV", "ESV", "NET"};

    public static final String TAG = "Main Activity";

    public static Readings[] reading = new Readings[3];
    public static Integer curReading = 0;
    private static Integer prevPage = 0;

    public static int curMonth;
    public static int curDay;
    public static int selectedMonth = 0;
    public static int selectedDay = 0;

    static MapFragment mapFragment;

    static MainViewPagerAdapter adapterViewPager;
    public static CustomViewPager mainViewPager;
    public BottomNavigationView navigation;
    public TabLayout readingsTabs;
    AppBarLayout appBar;

    private static Stack<Integer> backStack = new Stack<>(); // Edited

    public static HashMap<String,Fragment> fragments = new HashMap<>();

    public static FragmentManager fragmentManager;

    // The key for saving and retrieving isActivityRecreated field.
    private static final String KEY_IS_ACTIVITY_RECREATED = "KEY_IS_ACTIVITY_RECREATED";

    /** true if this activity is recreated. */
    private static boolean isActivityRecreated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewPager = findViewById(R.id.mainViewPager);
        navigation = findViewById(R.id.navigation);
        readingsTabs = findViewById(R.id.reading_tab_layout);
        readingsTabs.setVisibility(View.GONE); //Hide tabs on home page!
        appBar = findViewById(R.id.main_appbar);

        fragmentManager = getSupportFragmentManager();
        mainViewPager.setOffscreenPageLimit(3);

        try {
            isActivityRecreated = savedInstanceState.getBoolean(KEY_IS_ACTIVITY_RECREATED, false);
        } catch (NullPointerException e) {
            Log.i(TAG,"Activity is not recreated!");
        }

        if (isActivityRecreated) {
            selectedMonth = savedInstanceState.getInt("month");
            selectedDay = savedInstanceState.getInt("day");

        }

        //Determine the current date
        getCurrentDate();

        //Initialise the databases
        checkDatabasesExist();

        //Get readings info
        initialiseReadings(this);

        //Create the fragments for use later on
        generateReadingFragments();

        //Set up the viewpager listener
        initialiseMainViewPager(this);

        //Set tab values
        initialiseTabs(readingsTabs);

        if(isActivityRecreated){
            //MapFragment mapFragment = ((MapFragment)fragments.get("Map"));
            //mapFragment.getMapAsync();
            //mapFragment.refreshMap();
        }

        //Hide nav bar on home page
        navigation.setVisibility(View.GONE);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    public BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {


            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mainViewPager.setCurrentItem(0);

//                    updateBackStack(0);

                    return true;
                case R.id.navigation_reading:
                    mainViewPager.setCurrentItem(1);

//                    updateBackStack(1);

                    return true;
                case R.id.navigation_comments:
                    mainViewPager.setCurrentItem(2);

//                    updateBackStack(2);

                    return true;
                case R.id.navigation_map:
                    mainViewPager.setCurrentItem(3);

//                    updateBackStack(3);
                    return true;
            }
            return false;
        }
    };

    public static void updateBackStack(int position) {

        if (backStack.empty())
            backStack.push(0);

        if (backStack.contains(position)) {
            backStack.remove(backStack.indexOf(position));
            backStack.push(position);
        } else {
            backStack.push(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (backStack.size() > 1) {
            backStack.pop();
            mainViewPager.setCurrentItem(backStack.lastElement());
        } else {
        }
    }

    public void checkDatabasesExist() {

        //Ensure DB are in correct location

        checkDatabase(this, "DailyReadings.db");
        checkDatabase(this,"BiblePlaces.db");
        for (String translation : TRANSLATIONS) {
            checkDatabase(this,translation + ".db");
        }

    }

    public static void checkDatabase(Context context, String dbName) {
        DataBaseHelper myDbHelper;
        myDbHelper = new DataBaseHelper(context, dbName);
        //myDbHelper.setDatabaseName(dataBase);

        try {

            //myDbHelper.db_delete();
            myDbHelper.createDatabase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

    }



    public static void initialiseReadings(Context context) {

        //Initialise each reading

        for (int i = 0; i < 3; i++) {
            reading[i] = new Readings(context,i,curDay,curMonth);
        }

        //Check if leap year - return message if so
        if (reading[0].isLeapDay()) Toast.makeText(context.getApplicationContext(), "There aren't any readings set as this is a leap day!", Toast.LENGTH_LONG).show();

    }

    public static void initialiseTabs(TabLayout tabs) {

        //Initialise the reading tabs
        for (int i = 0; i < 3; i++) {
            if ( reading[i].getFullName() != null && tabs.getTabAt(i) != null ) {
                tabs.getTabAt(i).setText(reading[i].getFullName());
            }
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int readingNum = tab.getPosition();
                //adapterViewPager.updateReading(readingNum);
                updateReadingNum(readingNum);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }


        });

    }

    //public static void updateReadings(Context context,int monthOfYear, int dayOfMonth){



        //TabLayout tabs = ((MainActivity)context).findViewById(R.id.reading_tab_layout);


        //((MainActivity)context).recreate();

        //Get readings info
        //initialiseReadings(context,dayOfMonth,monthOfYear);

        //Create the fragments for use later on
        //generateReadingFragments();

        //mainViewPager.removeAllViews();

        //Set up the viewpager listener
        //initialiseMainViewPager(context);

        //Set tab values
        //initialiseTabs(tabs);

    //}

    // Call this method when you want to recreate this activity.
    public static void recreateActivity(Activity activity,int month,int day) {
        selectedMonth = month;
        selectedDay = day;
        isActivityRecreated = true;
        activity.recreate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_ACTIVITY_RECREATED, isActivityRecreated);
        outState.putInt("month", selectedMonth);
        outState.putInt("day", selectedDay);
        outState.putInt("mapFragmentID",mapFragment.getId());
    }

    public static void initialiseMainViewPager(Context context) {

        BottomNavigationView navBar = ((MainActivity) context).findViewById(R.id.navigation);
        AppBarLayout topBar = ((MainActivity) context).findViewById(R.id.main_appbar);
        TabLayout tabs = ((MainActivity)context).findViewById(R.id.reading_tab_layout);

        //Initialise the viewpager
        adapterViewPager = new MainViewPagerAdapter(fragmentManager);
        mainViewPager.storeAdapter(adapterViewPager);

        adapterViewPager.startUpdate(mainViewPager);
        HomeFragment homeFragment = (HomeFragment) adapterViewPager.instantiateItem(mainViewPager,0);
        ReadingRootFragment readingRootFragment = (ReadingRootFragment) adapterViewPager.instantiateItem(mainViewPager,1);
        CommentsRootFragment commentsRootFragment = (CommentsRootFragment) adapterViewPager.instantiateItem(mainViewPager,2);
        mapFragment = (MapFragment) adapterViewPager.instantiateItem(mainViewPager,3);
        adapterViewPager.finishUpdate(mainViewPager);

        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                navBar.getMenu().getItem(position).setChecked(true);
                updateBackStack(position);

                topBar.setExpanded(false, false);

                if (position == 0) {
                    tabs.setVisibility(View.INVISIBLE);
                    navBar.setVisibility(View.GONE);
                    mainViewPager.getChildAt(position).findViewById(R.id.home_scrollview).setTranslationX(0);
                } else if (prevPage == 0) {
                    tabs.setVisibility(View.VISIBLE);
                    navBar.setVisibility(View.VISIBLE);
                } else if ( position == 3) {
                    topBar.setExpanded(true,true);
                }

                prevPage = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) { }

        });

    }

    public void getCurrentDate() {

        //// GET CURRENT DATE INFO ////

        if (isActivityRecreated){
            //retrieve the stored date

            curMonth = selectedMonth;
            curDay = selectedDay;

        } else {
            //Get today's date

            GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());

            curMonth = cal.get(Calendar.MONTH);
            curDay = cal.get(Calendar.DAY_OF_MONTH);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {    }

    public static void onReadingClick(View view, int position) {

        Log.i("On RecyclerView Clicked","Selected reading " + Integer.toString(position));

        TabLayout tabs = ((MainActivity)view.getContext()).findViewById(R.id.reading_tab_layout);

        //updateReadingNum(position);
        mainViewPager.setCurrentItem(1);
        tabs.getTabAt(position).select();


    }


    public static class MainViewPagerAdapter extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 4;
        private static String TAG = "Main ViewPager Adapter";

        MainViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages.
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for a particular page.
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Log.i(TAG,"Getting home fragment");
                    return fragments.get("Home");
                case 1:
                    Log.i(TAG,"Getting reading root fragment");
                    return ReadingRootFragment.newInstance();
                case 2:
                    Log.i(TAG,"Getting comments root fragment");
                    return CommentsRootFragment.newInstance();
                case 3:
                    Log.i(TAG,"Getting map fragment");
                    //MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentByTag("Map");
                    return fragments.get("Map");
                    //return fragments.get("Map");
                default:
                    return null;
            }
        }


        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Tab " + position;
        }


    }

    public static void updateReadingNum(int readingNumber) {

        Log.i(TAG,"Updating reading fragments to reading " + Integer.toString(readingNumber) + ", Num Fragments = " + fragmentManager.getFragments().size());
        Log.i(TAG,fragmentManager.getFragments().toString());

        if (curReading != readingNumber) {
            curReading = readingNumber;

            FragmentTransaction trans = fragmentManager.beginTransaction();

            trans.replace(R.id.root_frame_reading, fragments.get("Reading " + Integer.toString(curReading)));
            trans.replace(R.id.root_frame_comments,fragments.get("Comments " + Integer.toString(curReading)));
            //if(getSupportFragmentManager().findFragmentById(R.id.bibleMapView) != null) {

            //MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentByTag("Map");
            //MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map_fragment_root_id);
            //Log.i(TAG,"Map fragment: " + mapFragment);
            //if (isActivityRecreated && !mapRecreated) mapFragment.refreshMap(); mapRecreated = true;
            mapFragment.setCurrentLayer(curReading);
            mapFragment.zoomExtents(curReading);
            //} else Log.i(TAG,"No map fragment to replace!");

            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            trans.addToBackStack(null);
            trans.commit();
        } else Log.i(TAG,"Clicked on current reading!");

    }


    public static void generateReadingFragments() {


        //fragmentManager.getFragments().clear();
        //fragments = new HashMap<>();
        if (isActivityRecreated) {
            backStack.empty();
            //fragments.remove("Map");
            //fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.map_fragment_root_id)).commitAllowingStateLoss();
        } Log.i(TAG,"Destroying existing map fragment");

        //if(isActivityRecreated){

            //fragmentManager.beginTransaction().replace("Map",MapFragment.newInstance());

        //} else {

        for (int i = 0; i < 3; i++) {
            fragments.put("Reading " + Integer.toString(i), ReadingFragment.newInstance(i));
            fragments.put("Comments " + Integer.toString(i), CommentsFragment.newInstance(i));
        }


        fragments.put("Home", HomeFragment.newInstance());
        fragments.put("Map", MapFragment.newInstance());

        if(!isActivityRecreated) {
            //MapFragment mapFragment = MapFragment.newInstance();

            //fragmentManager.beginTransaction().add(mapFragment, "Map").commit();
            //fragments.put("Map", fragmentManager.findFragmentByTag("Map"));
        }

        Log.i(TAG, "Fragments Created!");

        //}
    }
}
