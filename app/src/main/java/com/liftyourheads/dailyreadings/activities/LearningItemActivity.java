package com.liftyourheads.dailyreadings.activities;

import android.app.Activity;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.fragments.LearningItemFragment;

public class LearningItemActivity extends FragmentActivity implements LearningItemFragment.OnFragmentInteractionListener {

    FragmentManager fragmentManager;
    private static final String fragmentTagPrefix = "Learning fragment ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_item);

        String ID = getIntent().getStringExtra("ID");
        String title = getIntent().getStringExtra("Title");

        fragmentManager = getSupportFragmentManager();

        updateFragment(ID,title);

    }

    public void updateFragment(String ID, String title) {

        LearningItemFragment learningItemFragment = (LearningItemFragment) fragmentManager.findFragmentByTag(fragmentTagPrefix + ID);

        if (learningItemFragment == null) {
            learningItemFragment = LearningItemFragment.newInstance(ID, title);
            fragmentManager.beginTransaction().add(learningItemFragment,fragmentTagPrefix + ID).commit();
        }


        fragmentManager.beginTransaction().replace(R.id.item_fragment_container,learningItemFragment).commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
