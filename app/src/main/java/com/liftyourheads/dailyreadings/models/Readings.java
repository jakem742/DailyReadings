package com.liftyourheads.dailyreadings.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.liftyourheads.dailyreadings.activities.MainActivity;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.liftyourheads.dailyreadings.activities.MainActivity.BIBLE;
import static com.liftyourheads.dailyreadings.activities.MainActivity.BIBLE_ABBR;
import static com.liftyourheads.dailyreadings.activities.MainActivity.MONTHS;


public class Readings {

    private String[][][] verses;
    private String comments;
    private int notesSize = 0;
    private int maxNotes = 0;
    private String[] allComments;
    private String[][] commentPost;
    private ArrayList<HashMap<String, String>> commentList;
    private ArrayList<HashMap<String, String>> verseList;
    private ArrayList<Map<String, String>> places;
    private Integer day;
    private Integer month;
    private Context context;
    private Integer readingNum;
    private StringBuilder fullName = new StringBuilder();
    private String[] bookName;
    private Integer[] bookPosition;
    private Integer[] chapters = null;
    private Boolean chapterPartial = false;
    private Boolean singleChapterBook = false;
    private Boolean multipleBooks = false;
    private Boolean leapDay;
    private Boolean isCommentsInDatabase;
    private int numPlaces;
    private Integer numChapters;
    private String[] chapterTitles;
    private static final String TAG = "Reading";
    private Integer[] chapterVerses;
    private StringBuilder placeJson;



    private SpannableStringBuilder verseParagraphs = new SpannableStringBuilder();
    //private String[] audioURL;
    private Integer[] versesPartialChapter = new Integer[2];

    public Readings(Context context, Integer reading, int day,int month) {
        this.context = context;
        this.readingNum = reading;
        this.day = day;
        this.month = month;
        if (day == 29  && month == 1) {
            //TODO: Fix leap day implementation
            leapDay = true;
        } else {
            leapDay = false;
            initialise();
        }
    }

    /*
    private void setNumber(int value) {
        this.number = value;
    }
    */

    private Boolean isChapterPartial() {
        return this.chapterPartial;
    }

    /*
    public Boolean isMultipleBooks() {
        return this.multipleBooks;
    }
    */

    private void initialise() {
        // Retrieve Reading Details
        getReadingDetails();
        setFullName();
        //generateAudioURL();

        // Retrieve Verses
        updateVerses();

        // Retrieve Comments
        getCommentsFromDB();           /// ACCESS COMMENTS DATABASE AND RETRIEVE RELEVANT SECTION ///
        separateComments();            /// SEPARATE COMMENTS ///
        separateNoteFromPoster();      /// SPLIT COMMENTS INTO CONTENT AND POSTER ///
        createCommentArrayList();      /// CREATE ARRAYLIST OF RESULTING COMMENTS ///

        //Retrieve Places for map
        processPlaces();

        //Convert places into usable format
        createPlacesJson();

    }

    private void getCommentsFromDB() {
        if ( day == null ) {

            Timber.e( "Day is a null object!");

        } else if (day < 1 || day > 31) {

            Timber.e("Invalid day chosen! Day = " + day.toString());

        } else if ( month == null ) {

            Timber.e("Invalid month chosen!");

        }

        //Todo: Check database for comments

        SQLiteDatabase commentsDB = context.openOrCreateDatabase("DailyReadings.db", MODE_PRIVATE, null);

        Cursor reading = commentsDB.rawQuery("SELECT * FROM comments WHERE month = '" + MONTHS[month] + "\' AND date = " + day.toString(), null);

        Timber.i( Integer.toString(reading.getCount()) + " set of comments entry found for reading " + readingNum.toString());

        reading.moveToFirst();

        isCommentsInDatabase = Boolean.valueOf(reading.getString(reading.getColumnIndex("downloaded")));

        if (isCommentsInDatabase) {

            Timber.i( "Comments found in DB");

            int curColumn = 0;

            switch (readingNum) {
                case 0:
                    curColumn = reading.getColumnIndex("first");
                    break;
                case 1:
                    curColumn = reading.getColumnIndex("second");

                    break;
                case 2:
                    curColumn = reading.getColumnIndex("third");
                    break;
            }

                comments = reading.getString(curColumn);

        }

        reading.close();
        commentsDB.close();

    }

    private void separateComments() {

        try {

            /// DIVIDE READINGS INTO INDIVIDUAL NOTES ///

                allComments = comments.split("<div class=\"note\">");

                /// DETERMINE ARRAY SIZE FOR COMMENT ARRAY BELOW ///
                //if (allComments[i].length > maxNotes) {

                notesSize = allComments.length;

                if (notesSize > maxNotes) {

                    maxNotes = notesSize;

                }

            Timber.i(Integer.toString(notesSize));


        } catch (NullPointerException e) {

            e.printStackTrace();

        } catch (Exception e) {

            e.printStackTrace();

        }



    }

    private void separateNoteFromPoster() {

        //Todo: Make more efficient array size
        commentPost = new String[maxNotes][];

        for (int j = 0; j < allComments.length; j++) {

            //Divide allComments into comment and poster info
            commentPost[j] = allComments[j].split("<p class=\"user\">");

        }

    }

    private void createCommentArrayList() {

        commentList = new ArrayList<>();

        HashMap<String, String> item;


            if (commentPost != null) {

                //Add comments header to top of list
                item = new HashMap<>();
                item.put("Chapter Header","True");
                item.put("Title","Comments");
                commentList.add(item);


                for (int i = 1; i < commentPost.length; i++) {

                    // Check if current position is less than total number of comments for the specified reading
                    if (i < notesSize) {
                        try {

                            item = new HashMap<>();
                            String post;
                            String[] poster;

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                //Code for Nougat+

                                //// STRIP UNNECESSARY TEXT ////
                                post = Html.fromHtml(commentPost[i][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                        "\t&nbsp;</div>", ""), Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n");
                                poster = Html.fromHtml(commentPost[i][1], Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("Reply to ([a-zA-Z]+)", "").split("Comment added in");


                            } else {
                                //Code for older OS

                                //// REMOVE UNNECESSARY TEXT ////
                                post = Html.fromHtml(commentPost[i][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                        "\t&nbsp;</div>", "")).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n");
                                poster = Html.fromHtml(commentPost[i][1]).toString().replaceAll("Reply to ([a-zA-Z]+)", "").split("Comment added in");

                            }

                            item.put("comment", post);
                            item.put("poster", poster[0].replaceFirst("\\s++$", "") + ", " + poster[1]); // Remove trailing whitespace
                            commentList.add(item);

                        } catch (Exception e) {

                            e.printStackTrace();
                            Timber.i("Current iteration: " + Integer.toString(i));
                            break;

                        }
                    }

                }

        }

    }




    private void setBookName(String name) {

        if (!name.contains(",")) {

            this.bookName = new String[1];
            this.bookName[0] = name;

        } else { // Multiple books included! In this case, 2 + 3 Jn

            this.bookName = new String[2];
            this.bookName = name.split(", ");
            this.multipleBooks = true;

        }

        if (!this.multipleBooks) {

            this.bookPosition = new Integer[1];
            this.bookPosition[0] = Arrays.asList(MainActivity.BIBLE).indexOf(this.bookName[0]);

        } else {

            this.bookPosition = new Integer[2];

            for (int i = 0; i < this.bookName.length; i++) {

                this.bookPosition[i] = Arrays.asList(MainActivity.BIBLE).indexOf(this.bookName[i]);

            }

        }
    }

    private void processPlaces() {

        // Get list of places by chapter from DB and
        SQLiteDatabase readingsDB = context.openOrCreateDatabase("BiblePlaces.db", 0, null);

        //Establishing variables
        Integer[] bookPosition = this.getBookIndex();
        Integer[] bookChapters = this.getChapters();
        Cursor readingCursor,placesCursor;
        ArrayList<String> placeNamesDone = new ArrayList<>();
        places = new ArrayList<>();

        //Iterating through each chapter in reading
        for (Integer readingChapter : bookChapters ) {

            String readingBook = MainActivity.BIBLE_ABBR[bookPosition[0]];

            // Find places in DB
            readingCursor = readingsDB.rawQuery("SELECT * FROM bible_places_CHAPTER WHERE book = '" + readingBook + "\' AND chapter = " + readingChapter.toString(), null);

            Timber.i("Map data found: " + "Found map data for " + readingBook + " " + readingChapter.toString());



            if (readingCursor.moveToFirst()) {

                int placesColumn = readingCursor.getColumnIndex("places");

                do {

                    String placesList = readingCursor.getString(placesColumn);
                    String[] placesArray = placesList.split(", ");

                    for (String placeName : placesArray) {

                        if(!placeNamesDone.contains(placeName)) { //Check that the place isn't already on the list!

                            placeNamesDone.add(placeName);

                            placesCursor = readingsDB.rawQuery("SELECT * FROM bible_places_NAME WHERE Name = '" + placeName.replaceAll("\'", "\'\'") + "\'", null);
                            Integer latColumn = placesCursor.getColumnIndex("Lat");
                            Integer longColumn = placesCursor.getColumnIndex("Long");
                            Integer typeColumn = placesCursor.getColumnIndex("Type");
                            Integer versesColumn = placesCursor.getColumnIndex("Verses");

                            if (placesCursor.moveToFirst()) {
                                do {
                                    String placeLat = placesCursor.getString(latColumn).replaceAll("[^\\d.]", "");
                                    String placeLong = placesCursor.getString(longColumn).replaceAll("[^\\d.]", "");
                                    String placeVerses = placesCursor.getString(versesColumn);
                                    String type = placesCursor.getString(typeColumn);

                                    String[] verses = placeVerses.trim().split(",");

                                    StringBuilder versesList = new StringBuilder();

                                    HashMap<String,String> placeData = new HashMap<>();
                                    placeData.put("name",placeName);
                                    placeData.put("latitude",placeLat);
                                    placeData.put("longitude",placeLong);
                                    if (type == null) placeData.put("type","Unknown");
                                    else placeData.put("type",type);

                                    if (verses.length == 1) {
                                        placeData.put("verses",placeVerses);
                                        Log.i(TAG,"Only one place found!");
                                    } else {

                                        for(String book : bookName) {
                                            String book_abbr = BIBLE_ABBR[Arrays.asList(BIBLE).indexOf(book)];
                                            for (Integer chapter : chapters){
                                                for (String verse : verses) {

                                                    if (verse.contains(book_abbr + " " + chapter.toString())) {
                                                        versesList.append(verse).append(",");
                                                    }
                                                }
                                            }
                                        }


                                        placeData.put("verses",versesList.substring(0, versesList.length() - 1).trim());

                                    }
                                            //{placeName, placeLat, placeLong};
                                    //Log.i(TAG, "Place Info: " + Arrays.toString(placeData));
                                    places.add(placeData);

                                } while (placesCursor.moveToNext());


                            } else {
                                Log.i(TAG, "Couldn't find '" + placeName + "' in the database!");
                            }

                            placesCursor.close();

                        }
                    }

                } while (readingCursor.moveToNext());
            } else {
                Timber.i("Place Info: "+ readingBook + " " + readingChapter + " has no recorded places");
            }

            readingCursor.close();

        }

        readingsDB.close();

    }

    public ArrayList<Map<String,String>> getPlaces() {
        return places;
    }


    private void setChapters(String chapters) {
        String[] chaptersSplit;

        if (chapters.contains(":")) {

            //If chapter is partial
            chapterPartial = true;
            chaptersSplit = chapters.split(":");

            //Only one chapter
            numChapters = 1;
            this.chapters = new Integer[numChapters];
            this.chapters[0] = Integer.parseInt(chaptersSplit[0]);
            this.chapterTitles = new String[] {this.bookName[0] + " " + this.chapters[0]};

            //Split start & end verses
            String[] versesPartial = chaptersSplit[1].split("-");

            this.versesPartialChapter[0] = Integer.parseInt(versesPartial[0]);
            this.versesPartialChapter[1] = Integer.parseInt(versesPartial[1]);

        } else {

            //If chapter is not partial

            if (chapters.contains("-")) {

                // More than two chapters
                chaptersSplit = chapters.split("-");
                numChapters = Integer.parseInt(chaptersSplit[1]) - Integer.parseInt(chaptersSplit[0]) + 1; // +1 accounts for initial chapter ie. 45-43 = 2 ( +1 for starting chapter = 3)
                this.chapters = new Integer[numChapters];
                this.chapters[0] = Integer.parseInt(chaptersSplit[0]);
                this.chapterTitles = new String[numChapters];
                this.chapterTitles[0] = this.bookName[0] + " " + this.chapters[0];

                for (int i = 1; i < numChapters; i++) {

                    this.chapters[i] = this.chapters[(i-1)] + 1;
                    this.chapterTitles[i] = this.bookName[0] + " " + this.chapters[i];

                }


            } else if (chapters.contains(",")) {

                //Two chapters
                chaptersSplit = chapters.split(",");
                numChapters = chaptersSplit.length;
                this.chapters = new Integer[numChapters];
                this.chapterTitles = new String[numChapters];


                for (int i = 0; i < numChapters; i++) {

                    this.chapters[i] = Integer.parseInt(chaptersSplit[i]);

                }

                this.chapterTitles[0] = this.bookName[0] + " " + this.chapters[0];
                this.chapterTitles[1] = this.bookName[0] + " " + this.chapters[1];


            } else {


                if (!chapters.equals("")) {
                    //Only one chapter out of several
                    numChapters = 1;
                    this.chapters = new Integer[numChapters];
                    this.chapters[0] = Integer.parseInt(chapters);
                    this.chapterTitles = new String[]{this.bookName[0] + " " + this.chapters[0]};

                } else if (!multipleBooks) {

                    //Only one chapter in book
                    numChapters = 1;
                    this.chapters = new Integer[numChapters];
                    this.chapterTitles = new String[]{this.bookName[0]};
                    this.singleChapterBook = true;
                    this.chapters[0] = 1;
                } else { //Only true for 2 + 3 Jn

                    this.chapterTitles = new String[2];
                    //Two chapters
                    numChapters = 2;
                    this.chapters = new Integer[numChapters];

                    //Always first chapter of book
                    this.chapters[0] = 1;
                    this.chapters[1] = 1;
                    this.chapterTitles[0] = this.bookName[0];
                    this.chapterTitles[1] = this.bookName[1];

                }

            }

        }

    }

    public String[] getChapterTitles() {
        return chapterTitles;
    }

    private Integer[] getBookIndex() {

        return this.bookPosition;

    }

    private void createPlacesJson(){

        //if(hasPlaces) {
            placeJson = new StringBuilder();
            placeJson.append("{\"type\": \"FeatureCollection\",\"features\": [");

            for (Map place : places) {

                placeJson.append("{\"type\": \"Feature\",\"properties\": {\"name\": \"")
                        .append(place.get("name"))
                        .append("\",\"selected\": false, \"verses\": \"")
                        .append(place.get("verses"))
                        .append("\", \"type\":")
                        .append(place.get("type"))
                        .append("},\"geometry\": {\"type\": \"Point\",\"coordinates\": [")
                        .append(place.get("longitude"))
                        .append(",")
                        .append(place.get("latitude"))
                        .append("]}}");

                if (places.get(places.size()-1) != place ) placeJson.append(",");
            }

            placeJson.append("]}");
        //}

    }

    public String getPlacesAsString(){
        Log.i(TAG,placeJson.toString());
        //if (hasPlaces)
        return placeJson.toString();
        //else return null;

    }

    public Boolean placesExist() {

        return (numPlaces > 0);
    }

    public int getNumPlaces() {
        return numPlaces;
    }

    private void setFullName() {

        if (!this.singleChapterBook && !this.multipleBooks) { //Include chapter number if book contains more than one!

            this.fullName.append(this.bookName[0]).append(" ").append(chapters[0]);
            Timber.i("Reading " + readingNum + " contains only one book!");

        } else if (this.multipleBooks) {

            this.fullName.append(this.bookName[0]).append(", ").append(this.bookName[1]);

            Timber.i("Reading " + readingNum + " contains multiple books!");

        } else {

            this.fullName.append(this.bookName[0]);
            Timber.i("Reading " + readingNum + " contains only one chapter in the whole book!");


        }


        //Append extra chapters
        if (chapters.length > 2) {

            // Add final chapter
            this.fullName.append("-").append(chapters[(chapters.length-1)]); //Add last chapter

        } else if (chapters.length == 2 && !multipleBooks) {

            //Add second chapter
            this.fullName.append(", ").append(chapters[1]);

        } else if (this.isChapterPartial()) {

            //Add verses range
            this.fullName.append(" : ").append(this.versesPartialChapter[0]).append(" - ").append(this.versesPartialChapter[1]);

        }

    }

    /*
    private void generateAudioURL() {

        this.audioURL = new String[numChapters];

        //TODO: Integrate audio for different translations
        String curTranslation = "ESV";

        for (int i = 0; i < chapters.length; i++) {

//            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            curTranslation = sharedPreferences.getString("pref_translation", "ESV"); //Default translation = ESV

            //Set audio URL
            switch (curTranslation) {

                case "NET":
                    //TODO: Get correct NET audio url!

                    //TODO: Fix audio urls for multiple books

                    this.audioURL[i] = "http://feeds.bible.org/netaudio/" + (bookPosition[0] + 1) + "-" + bookName[0].replace(" ", "") + "-" + String.format(Locale.getDefault(),"%02d", chapters[i]) + ".mp3";
                    break;

                //Use KJV for default readings
                default:
                    this.audioURL[i] = "http://server.firefighters.org/kjv/projects/firefighters/kjv_web/" + String.format(Locale.getDefault(),"%02d", (bookPosition[0] + 1)) + "_" + bookName[0].replace(" ", "").substring(0, 3) + "/" + String.format(Locale.getDefault(),"%02d", (bookPosition[0] + 1)) + bookName[0].replace(" ", "").substring(0, 3) + String.format(Locale.getDefault(),"%03d", chapters[i]) + ".mp3";
                    break;

            }


        }

    }
    */


    public void updateVerses() {

        //Update reading verses
        verseList = new ArrayList<>();

        String verseNumber = null;
        String verseContent;
        //JSONObject readingChapterCurrent = null;
        int[] chapterStartCounters = new int[chapters.length];

        Timber.i( "Getting Verses: " + Arrays.asList(this.bookName) + " " + Arrays.asList(this.chapters));

        getVerses(this.bookName, this.chapters, this.isChapterPartial(), this.versesPartialChapter);

        HashMap<String, String> verse;
        HashMap<String, String> chapter;


        Integer curVerse;

        if (this.chapterPartial) {

            //Change starting verse number
            curVerse = versesPartialChapter[0];

        } else {

            curVerse = 1;

        }

        for (int book = 0; book < this.bookName.length; book++) {
            for (Integer i = 0; i < verses[book].length; i++) {

                if (!multipleBooks) chapterStartCounters[i] = verses[book][i].length;
                else chapterStartCounters[i] = verses[book][0].length;

                //Set starting verse to 0
                if (i != 0) curVerse = 1;

                // Add chapter title to start
                //if (this.chapters.length > 1) {

                    if (i != 0) verseParagraphs.append("<br/><br/>");
                    verseParagraphs.append("<big><span style=\"color:#9ccc65\">").append(this.bookName[book]).append(" ").append(Integer.toString(this.chapters[i])
                    ).append("</span></big><br/>");
                //}

                try {

                    for (int j = 0; j < verses[book][i].length; j++) {

                        //Skip verse if it is blank (in case of modern translations)
                        if (verses[book][i][j].equals("")) continue;

                        //Add chapter header
                        if (j == 0) {
                            chapter = new HashMap<>();
                            chapter.put("Chapter Header","True");

                            if (multipleBooks) chapter.put("Title",this.chapterTitles[book]);
                            else chapter.put("Title",this.chapterTitles[i]);

                            verseList.add(chapter);
                        }


                        //For list view
                        verse = new HashMap<>();

                        if (j != 0) {

                            if (verses[book][i][j].contains("<pb/>")) {

                                verseParagraphs.append("<br/>");

                            } else {

                                verseParagraphs.append("&nbsp;");

                            }
                        }

                        verseNumber = Integer.toString((j+1));
                        verseContent = verses[book][i][j].replaceAll("([A-Z][\\w'-]*(?:\\s+[A-Z][\\w'-]*|\\s+(?:a|an|for|the|and|but|or|on|in|with))+(?: <br'/>)+(?:\\s*))", "").replaceAll("<[^>]*>", "").trim();//.replaceAll("(\\{[a-zA-Z0-9.,: ;]+\\})+$", "").replaceAll("&#x27;", "'").replaceAll("\\{(.*?)\\}", "$1");

                        verse.put("verseNum", curVerse.toString());
                        verse.put("verseContent", verseContent);
                        verseList.add(verse);

                        //For paragraph view
                        verseParagraphs.append("<span style=\"color:#9ccc65\"><small><sup><b><i>").append(curVerse.toString()).append("</i></b></sup></small></span>&nbsp;").append(verses[book][i][j].replaceAll("<pb/>", ""));//

                        curVerse++;
                    }
                    //chapterStartCounters[i]++;

                } catch (Exception e) {

                    e.printStackTrace();
                    Timber.i( "Error reading verse " + verseNumber);

                }
            }
        }


        for (int i = 1; i<(chapters.length); i++) {

            chapterStartCounters[i] += chapterStartCounters[(i-1)];

        }

        Timber.i( "Chapter Counters = " + Arrays.toString(chapterStartCounters));


    }

    private void getVerses(String[] bookName, Integer[] chapters, Boolean isPartialChapter, Integer[] versesPartial) {

        //Open the db for current translation
        //TODO: Allow for different translations
        String translation = PreferenceManager.getDefaultSharedPreferences(context).getString("translation","ESV");

        SQLiteDatabase bibleDB = context.openOrCreateDatabase(translation + ".db", MODE_PRIVATE, null);
        Cursor reading = null;

        //Initialise verses array based on number of chapters
        verses = new String[bookName.length][chapters.length][];

        int counter = 0;

        try {
            for (String book : bookName) {

                //Get the current book number for use in verses db
                Cursor bookCur = bibleDB.rawQuery("SELECT * FROM books WHERE long_name = \'" + book + "\'", null);

                bookCur.moveToFirst();
                String bookNumber = null;

                try {

                    bookNumber = bookCur.getString(bookCur.getColumnIndex("book_number"));
                    bookCur.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    Timber.i("Getting Book Number: " + "Invalid book name: " + book);
                }


                chapterVerses = new Integer[chapters.length];

                //Iterate through each chapter and add to array
                if (!multipleBooks) {

                    for (int i = 0; i < chapters.length; i++) {

                        //Customise query to match current chapter
                        if (!isPartialChapter) {

                            reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = " + chapters[i].toString(), null);

                        } else {

                            reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = " + chapters[i].toString() + " AND verse BETWEEN " + versesPartial[0] + " AND " + versesPartial[1], null);

                        }

                        //Define size of array for this chapter
                        verses[counter][i] = new String[reading.getCount()];
                        chapterVerses[i] = reading.getCount();

                        int contentIndex = reading.getColumnIndex("text");

                        if (reading.getCount() > 0) {
                            reading.moveToFirst();
                            int j = 0;

                            do {

                                verses[counter][i][j] = reading.getString(contentIndex).replaceAll("’", "'").replaceAll("<f>.*?</f>", "");//.replaceAll("<pb/>", "")
                                j++;

                            } while (reading.moveToNext());
                        } else {

                            Log.e(TAG,"Unable to find verses in db for reading " + fullName);
                        }
                    }

                } else {

                    reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = 1", null); //Chapter is always 1 for multiple books

                    //Define size of array for this chapter
                    verses[counter][0] = new String[reading.getCount()];
                    chapterVerses[counter] = reading.getCount();

                    int contentIndex = reading.getColumnIndex("text");


                    reading.moveToFirst();
                    int j = 0;

                    do {

                        verses[counter][0][j] = reading.getString(contentIndex).replaceAll("’", "'").replaceAll("<f>.*?</f>", "");//.replaceAll("<pb/>", "")
                        j++;

                    } while (reading.moveToNext());
                }

                counter++;

            }

        } finally {

            if(reading != null){

                reading.close();

            }

            bibleDB.close();

        }

    }


    private void getReadingDetails() {

        SQLiteDatabase readingsDB = context.openOrCreateDatabase("DailyReadings.db", MODE_PRIVATE, null);

        Cursor reading = readingsDB.rawQuery("SELECT * FROM daily_readings WHERE month = '" + MONTHS[this.month] + "\' AND date = " + Integer.toString(this.day), null);

        Timber.i( "Found " + Integer.toString(reading.getCount()) + " DB entry for reading " + readingNum);

        reading.moveToFirst();

        int portionColumn = reading.getColumnIndex("first_book");

        switch(readingNum) {
            case 0:
                portionColumn = reading.getColumnIndex("first_book");
                break;
            case 1:
                portionColumn = reading.getColumnIndex("second_book");
                break;
            case 2:
                portionColumn = reading.getColumnIndex("third_book");
                break;
        }

        do {
                setBookName(reading.getString(portionColumn++));
                setChapters(reading.getString(portionColumn++));

        } while (reading.moveToNext());


        reading.close();
        readingsDB.close();

    }

    /*
    public String getAudioURL(int i) {
        return audioURL[i];
    }

    public String[] getAudioURLS() {
        return audioURL;
    }
*/


    public String getFullName() {
        return fullName.toString();
    }

    /*
    public Integer getNumChapters() {
        return numChapters;
    }

    public int getChapterStartCounters(int i) {
        return chapterStartCounters[i];
    }


    Deprecated (used for dynamic header title)
    public String getBookName(String bookNumber) {

        StringBuilder name = new StringBuilder();

        if (bookNumber == null) {

            for (String book : bookName) {
                name.append(book);
            }

        } else {

            name.append(bookName[Integer.parseInt(bookNumber)]);

        }

        return name.toString();
    }
    */

    private Integer[] getChapters() {
        return chapters;
    }

    /*
    public Integer[] getBookPosition() {
        return bookPosition;
    }

    public Integer getChapter(int i) {
        return chapters[i];
    }
    */


    public ArrayList<HashMap<String, String>> getReadingVerses() {

        return verseList;

    }


    public ArrayList<HashMap<String, String>> getReadingComments() {

        return commentList;

    }

    public Boolean isLeapDay() {
        return leapDay;
    }


    public String getVerseParagraphs() {

        return verseParagraphs.toString();

    }

    public Integer[] getNumVerses() {
        return chapterVerses;
    }

}