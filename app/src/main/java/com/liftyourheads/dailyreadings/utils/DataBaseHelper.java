package com.liftyourheads.dailyreadings.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Switch;

import com.liftyourheads.dailyreadings.activities.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

public class DataBaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase myDataBase;
    private final Context myContext;
    private String dbName;
    //private static String DATABASE_NAME = "ESV.db";
    private static String DATABASE_PATH; //= "/data/data/com.liftyourheads.dailyreadings/databases/";
    private static String TAG = "Database Helper";
    private static final int DATABASE_VERSION = 1;
    Map<String, Integer> DATABASE_VERSIONS;


    public DataBaseHelper(Context context, String dbName) {


        super(context, dbName, null, DATABASE_VERSION);
        this.myContext = context;
        this.dbName = dbName;

        DATABASE_VERSIONS = new HashMap<String, Integer>() {{
            put("ESV", 1);
            put("KJV", 1);
            put("NET", 1);
            put("BiblePlaces", 1);
            put("DailyReadings", 1);
            put("CommandmentsOfChrist", 2);
        }};

        DATABASE_PATH = myContext.getDatabasePath(dbName).getPath() + ".db";


    }


    //Create a empty database on the system
    public void createDatabase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist)
        {
            //this.onUpgrade(getWritableDatabase(),getReadableDatabase().getVersion(),DATABASE_VERSION);
            //Log.v(TAG, DATABASE_PATH + " exists. Cur version = " + this.getWritableDatabase().getVersion());
            // By calling this method here onUpgrade will be called on a
            // writeable database, but only if the version number has been
            // bumped
            //this.openDatabase();
            this.openDatabase();
            this.onUpgrade(myDataBase, myDataBase.getVersion(), DATABASE_VERSIONS.get(dbName));
        }

        boolean dbExist1 = checkDataBase();
        if(!dbExist1)
        {
            //this.getReadableDatabase();

                this.close();
                copyDataBase();

        }

    }

    public void recreateDatabase() throws IOException {
        boolean dbExist = checkDataBase();

        if(dbExist)
        {
            Log.v(TAG, DATABASE_PATH + " exists. Deleting now.");
            db_delete();
        }

        boolean dbExist1 = checkDataBase();
        if(!dbExist1)
        {
            this.getReadableDatabase();

            this.close();
            copyDataBase();

        }
    }


    //Check database already exist or not
    private boolean checkDataBase()
    {
        boolean checkDB = false;
        try
        {
            String myPath = DATABASE_PATH;
            File dbfile = new File(myPath);
            checkDB = dbfile.exists();
        }
        catch(SQLiteException e) {
            e.printStackTrace();
        }
        return checkDB;
    }
    //Copies your database from your local assets-folder to the just created empty database in the system folder
    private void copyDataBase()
    {

        try {
            InputStream mInput = myContext.getAssets().open("databases/" + dbName + ".db");
            String outFileName = DATABASE_PATH;
            OutputStream mOutput = new FileOutputStream(outFileName);
            byte[] mBuffer = new byte[2024];
            int mLength;
            while ((mLength = mInput.read(mBuffer)) > 0) {
                mOutput.write(mBuffer, 0, mLength);
            }
            mOutput.flush();
            mOutput.close();
            mInput.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error("Error copying database");
        }
        openDatabase();
        myDataBase.setVersion(DATABASE_VERSIONS.get(dbName));
        myDataBase.close();
    }
    //delete database
    private void db_delete()
    {
        File file = new File(DATABASE_PATH);
        if(file.exists())
        {
            file.delete();
            System.out.println("Delete database file: " + file);
        }
    }

    //Open database
    public void openDatabase() throws SQLException
    {
        String myPath = DATABASE_PATH;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void closeDataBase()throws SQLException
    {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
        {
            Log.v(TAG, "Database out of date! Upgrading from Version " + Integer.toString(oldVersion) + " to Version " + Integer.toString(newVersion));

            db.close();
            this.db_delete();
            this.copyDataBase();
        }

    }

}