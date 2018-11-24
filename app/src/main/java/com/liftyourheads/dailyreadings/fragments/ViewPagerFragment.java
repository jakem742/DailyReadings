package com.liftyourheads.dailyreadings.fragments;

import com.liftyourheads.dailyreadings.R;

import static com.liftyourheads.dailyreadings.activities.MainActivity.reading;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class ViewPagerFragment extends Fragment {

    private static final String READING = "ReadingNumber";
    private Integer readingNum;
    private static String TAG = "Reading Viewpager Fragment";
    ReadingViewPagerAdapter adapterViewPager;
    public ViewPager readingViewPager;
    public TabLayout readingsTabs;


    public void updateReadingViewPagerTab(int position) {
        if (readingViewPager != null) readingViewPager.setCurrentItem(position);
    }

    public ViewPagerFragment() {
        // Required empty public constructor
    }


    public static ViewPagerFragment newInstance(int readingNumber) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putInt(READING, readingNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            readingNum = getArguments().getInt(READING,0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_viewpager_reading, container, false);

        //Initialise the reading tabs
        if (readingsTabs==null) {
            readingsTabs = view.findViewById(R.id.tabs);
            for (int i = 0; i < 3; i++) {
                if ( reading[i].getFullName() != null && readingsTabs.getTabAt(i) != null ) {
                    readingsTabs.getTabAt(i).setText(reading[i].getFullName());
                }
            }
        }

        readingsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                readingNum = tab.getPosition();
                //for (int i = 0; i < 3 ; i++) {

                    adapterViewPager.updateReadingNum(readingNum);

                //}
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }


        });

        readingViewPager = view.findViewById(R.id.fragment_reading_viewpager);

        adapterViewPager = new ReadingViewPagerAdapter(getFragmentManager());
        readingViewPager.setAdapter(adapterViewPager);

        initialiseReadingViewPager();

        return view;
    }


    public void initialiseReadingViewPager() {

        //Initialise the viewpager

        readingViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //navigation.getMenu().getItem(position+1).setChecked(true);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

    }


        public void updateActivePage(int position) {
            readingViewPager.setCurrentItem(position);
        }

    public static class ReadingViewPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;
        private static FragmentManager mFragmentManager;
        private static int readingNumber;
        ReadingFragment[] readingFragments;
        CommentsFragment[] commentsFragments;
        MapFragment[] mapFragments;

        private void updateReadingNum(int readingNum) {

            Log.i(TAG,"Updating reading to " + Integer.toString(readingNum));

            if (readingNumber != readingNum) {
                readingNumber = readingNum;
                FragmentTransaction trans = mFragmentManager.beginTransaction();
                trans.replace(R.id.root_frame_comments,commentsFragments[readingNumber]);
                trans.replace(R.id.root_frame_reading,readingFragments[readingNumber]);

                if(mFragmentManager.findFragmentById(R.id.map_fragment_root_id) != null) {
                    trans.remove(mFragmentManager.findFragmentById(R.id.map_fragment_root_id));
                    trans.add(R.id.map_fragment_root_id,mapFragments[readingNumber]);
                } else Log.i(TAG,"No map fragment to replace!");
                //trans.replace(R.id.map_fragment_root_id,mapFragments[readingNum]); //Only replace map fragment when it has been instantiated already!
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);
                trans.commit();
                this.notifyDataSetChanged();
            }

        }

        ReadingViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            mFragmentManager = fragmentManager;
            readingNumber = 0;


            this.readingFragments = new ReadingFragment[3];
            this.commentsFragments = new CommentsFragment[3];
            this.mapFragments = new MapFragment[3];


            for (int i = 0; i < 3; i++) {
                readingFragments[i] = ReadingFragment.newInstance(i);
                commentsFragments[i] = CommentsFragment.newInstance(i);
                //mapFragments[i] = MapFragment.newInstance(i);

                //fragmentManager.beginTransaction().add( R.id.reading_fragment_root_id,  ReadingFragment.newInstance(i), "reading"+Integer.toString(i)   ).commit();
                //fragmentManager.beginTransaction().add( R.id.comments_fragment_root_id, CommentsFragment.newInstance(i),"comments"+Integer.toString(i)  ).commit();
                //fragmentManager.beginTransaction().add( R.id.map_fragment_root_id,      ReadingFragment.newInstance(i), "map"+Integer.toString(i)       ).commit();
            }


            Log.i(TAG,"Number of Fragments Created" + Integer.toString(mFragmentManager.getFragments().size()));

        }

        // Returns total number of pages.
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for a particular page.
        @Override
        public Fragment getItem(int position) {

            //We are doing this only for checking the total number of fragments in the fragment manager.
            List<Fragment> fragmentsList = mFragmentManager.getFragments();
            int size = fragmentsList.size();

            Log.i(TAG, "********getItem position:" + position + " size:" + size);

            //Create a new instance of the fragment and return it.
            if ( position > 2 || position < 0 ) position = 0;

            switch (position) {
                case 0:
                    return ReadingRootFragment.newInstance();
                case 1:
                    return CommentsRootFragment.newInstance();
                case 2:
                    return MapFragment.newInstance();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
