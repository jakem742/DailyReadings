package com.liftyourheads.dailyreadings.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        ReadingFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener {

    public static final String[] BIBLE = {"Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi", "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"};
    public static final String[] BIBLE_ABBR = {"Gen", "Ex", "Lev", "Num", "Deut", "Josh", "Judg", "Ruth", "1 Sam", "2 Sam", "1 Kgs", "2 Kgs", "1 Chr", "2 Chr", "Ezra", "Neh", "Est", "Job", "Ps", "Pro", "Eccl", "Sng", "Isa", "Jer", "Lam", "Ezek", "Dan", "Hos", "Joel", "Amos", "Obad", "Jonah", "Mic", "Nahum", "Hab", "Zeph", "Hag", "Zech", "Mal", "Matt", "Mark", "Luke", "John", "Acts", "Rom", "1 Cor", "2 Cor", "Gal", "Eph", "Phil", "Col", "1 Thes", "2 Thes", "1 Tim", "2 Tim", "Titus", "Phm", "Heb", "James", "1 Pet", "2 Pet", "1 Jn", "2 Jn", "3 Jn", "Jude", "Rev"};
    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public static final String[] TRANSLATIONS = {"KJV", "ESV", "NET"};

    private final String TAG = "Main Activity";

    public static Readings[] reading = new Readings[3];
    public static Integer curReading = 0;
    private Integer prevPage = 0;
    public static int curMonth;
    public static int curDay;

    MainViewPagerAdapter adapterViewPager;

    public static CustomViewPager mainViewPager;
    BottomNavigationView navigation;

    static TabLayout readingsTabs;

    public static HashMap<String,Fragment> fragments = new HashMap<>();

    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewPager = findViewById(R.id.mainViewPager);
        navigation = findViewById(R.id.navigation);
        readingsTabs = findViewById(R.id.reading_tab_layout);
        readingsTabs.setVisibility(View.GONE); //Hide tabs on home page!

        fragmentManager = getSupportFragmentManager();
        mainViewPager.setOffscreenPageLimit(3);


        //Determine the current date
        getCurrentDate();

        //Initialise the databases
        checkDatabasesExist();

        //Get readings info
        initialiseReadings(curDay,curMonth);

        //Create the fragments for use later on
        generateReadingFragments();

        //Set up the viewpager listener
        initialiseMainViewPager();

        //Set tab values
        initialiseTabs();

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
                    return true;
                case R.id.navigation_reading:
                    mainViewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_comments:
                    mainViewPager.setCurrentItem(2);
                    return true;
                case R.id.navigation_map:
                    mainViewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        }
    };

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

    public void initialiseReadings(int day, int month) {

        //Initialise each reading

        for (int i = 0; i < 3; i++) {
            reading[i] = new Readings(this,i,day,month);
        }

        //Check if leap year - return message if so
        if (reading[0].isLeapDay()) Toast.makeText(getApplicationContext(), "There aren't any readings set as this is a leap day!", Toast.LENGTH_LONG).show();

    }

    public void initialiseTabs() {

        //Initialise the reading tabs
        for (int i = 0; i < 3; i++) {
            if ( reading[i].getFullName() != null && readingsTabs.getTabAt(i) != null ) {
                readingsTabs.getTabAt(i).setText(reading[i].getFullName());
            }
        }

        readingsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

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

    public void initialiseMainViewPager() {

        //Initialise the viewpager
        adapterViewPager = new MainViewPagerAdapter(getSupportFragmentManager());
        mainViewPager.storeAdapter(adapterViewPager);

        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                navigation.getMenu().getItem(position).setChecked(true);

                if (position == 0) {
                    readingsTabs.setVisibility(View.GONE);
                    navigation.setVisibility(View.GONE);
                    mainViewPager.getChildAt(position).findViewById(R.id.home_scrollview).setTranslationX(0);
                } else if (prevPage == 0) {
                    readingsTabs.setVisibility(View.VISIBLE);
                    navigation.setVisibility(View.VISIBLE);
                } else if ( position == 3) {
                    AppBarLayout appBarLayout = findViewById(R.id.main_appbar);
                    appBarLayout.setExpanded(true,true);
                }

                prevPage = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) { }

        });

    }

    public void getCurrentDate() {

        //// GET CURRENT DATE INFO ////

        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());

        curMonth = cal.get(Calendar.MONTH);
        curDay = cal.get(Calendar.DAY_OF_MONTH);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {    }

    public static void onReadingClick(View view, int position) {

        Log.i("On RecyclerView Clicked","Selected reading " + Integer.toString(position));

        //updateReadingNum(position);
        mainViewPager.setCurrentItem(1);
        readingsTabs.getTabAt(position).select();


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
                    return fragments.get("Map");
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

        String TAG = "Main Activity";

        if (curReading != readingNumber) {
            curReading = readingNumber;

            Log.i(TAG,"Updating reading to " + Integer.toString(curReading));
            FragmentTransaction trans = fragmentManager.beginTransaction();

            trans.replace(R.id.root_frame_reading, fragments.get("Reading " + Integer.toString(curReading)));
            trans.replace(R.id.root_frame_comments,fragments.get("Comments " + Integer.toString(curReading)));

            //if(getSupportFragmentManager().findFragmentById(R.id.bibleMapView) != null) {
                MapFragment mapFragment = (MapFragment) fragments.get("Map");
                mapFragment.setMarkers(curReading);
                mapFragment.zoomCamera();
            //} else Log.i(TAG,"No map fragment to replace!");

            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            trans.addToBackStack(null);
            trans.commit();
        } else Log.i(TAG,"Clicked on current reading!");

    }

    public void generateReadingFragments() {

        Log.i(TAG,"Storing Fragments");

        for (int i = 0; i < 3; i++) {
            Log.i(TAG,"Processing number " + Integer.toString(i));

            fragments.put("Reading " + Integer.toString(i), ReadingFragment.newInstance(i));
            fragments.put("Comments " + Integer.toString(i), CommentsFragment.newInstance(i));
        }

        fragments.put("Home",HomeFragment.newInstance());
        fragments.put("Map",MapFragment.newInstance());

        Log.i(TAG, "Fragments Created!");
    }

}
