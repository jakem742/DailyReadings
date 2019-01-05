package com.liftyourheads.dailyreadings.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.liftyourheads.dailyreadings.R;
import com.liftyourheads.dailyreadings.adapters.LearningHeaderRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class LearningHeaderActivity extends AppCompatActivity {

    public LearningHeaderRecyclerViewAdapter learningAdapter;
    RecyclerView headerRecyclerView;
    private static final String LEARNING_DB = "LearningContent";
    private static final String HEADERS_TABLE = "Headings";
    private static final String TAG = "LearningHeaderActivity";

    ArrayList<HashMap<String,String>> headers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_header);

        getLearningContent();
        updateHeaderListView();

    }

    public void updateHeaderListView() {

        this.headerRecyclerView = findViewById(R.id.headerRecyclerView);
        this.headerRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        this.learningAdapter = new LearningHeaderRecyclerViewAdapter(this, headers);
        this.headerRecyclerView.setAdapter(learningAdapter);
        //this.headerRecyclerView.setHasFixedSize(true);

    }

    private void getLearningContent() {

        MainActivity.checkDatabase(this, LEARNING_DB);
        Crashlytics.log(Log.DEBUG, TAG, "Processing headers");

        SQLiteDatabase learningDB = openOrCreateDatabase(LEARNING_DB + ".db", MODE_PRIVATE, null);
        Cursor headersCursor = learningDB.rawQuery("SELECT * FROM " + HEADERS_TABLE, null);

        headers = new ArrayList<>();

        if (headersCursor.moveToFirst()) {

            int titleColumn = headersCursor.getColumnIndex("Header");
            int refColumn = headersCursor.getColumnIndex("Subheading");
            int idColumn = headersCursor.getColumnIndex("ID");

            do {

                HashMap<String,String> heading = new HashMap<>();

                heading.put("Title",headersCursor.getString(titleColumn));
                heading.put("Ref",headersCursor.getString(refColumn));
                heading.put("ID",headersCursor.getString(idColumn));

                headers.add(heading);

            } while (headersCursor.moveToNext());

        } else {

            Log.i(TAG, "Couldn't find subject headers in the database!");
        }

        headersCursor.close();
        learningDB.close();

    }
}
