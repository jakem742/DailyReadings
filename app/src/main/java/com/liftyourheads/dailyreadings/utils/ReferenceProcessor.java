package com.liftyourheads.dailyreadings.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static com.liftyourheads.dailyreadings.activities.MainActivity.BIBLE;
import static com.liftyourheads.dailyreadings.activities.MainActivity.BIBLE_ABBR;

public class ReferenceProcessor {

    private static String TAG = "Reference Processor";
    final static String TYPE_CONCURRENT = "Concurrent Verses";
    final static String TYPE_SINGLE = "Single Verse";
    static String REFERENCE_TYPE = "Type";
    static String REFERENCE_START = "Start Verse";
    static String REFERENCE_END = "End Verse";
    static String REFERENCE_VERSE = "Verse";
    static String REFERENCE_CHAPTER = "Chapter";
    static String REFERENCE_BOOK = "Book";
    static String REFERENCE_FULL = "Long Name";

    public static final String[][] BIBLE_REF = {{"Genesis","Gen"}, {"Exodus","Ex"}, {"Leviticus","Lev"}, {"Numbers","Num"}, {"Deuteronomy","Deut"}, {"Joshua","Josh"}, {"Judges","Judg"}, {"Ruth"}, {"1 Samuel", "1 Sam"}, {"2 Samuel", "2 Sam"}, {"1 Kings", "1 Kgs"}, {"2 Kings", "2 Kgs"}, {"1 Chronicles", "1 Chr"}, {"2 Chronicles", "2 Chr"}, {"Ezra"}, {"Nehemiah", "Neh"}, {"Esther", "Est"}, {"Job"}, {"Psalms", "Psa", "Ps"}, {"Proverbs", "Pro"}, {"Ecclesiastes", "Ecc"}, {"Song of Solomon", "Song"}, {"Isaiah", "Isa"}, {"Jeremiah", "Jer"}, {"Lamentations", "Lam"}, {"Ezekiel", "Eze", "Ezek"}, {"Daniel", "Dan"}, {"Hosea", "Hos"}, {"Joel"}, {"Amos"}, {"Obadiah", "Oba"}, {"Jonah", "Jon"}, {"Micah", "Mic"}, {"Nahum", "Nah"}, {"Habakkuk", "Hab"}, {"Zephaniah", "Zeph"}, {"Haggai", "Hag"}, {"Zechariah", "Zech"}, {"Malachi", "Mal"}, {"Matthew", "Matt", "Mat"}, {"Mark", "Mar","Mk"}, {"Luke", "Luk", "Lk"}, {"John", "Jn"}, {"Acts"}, {"Romans", "Rom"}, {"1 Corinthians", "1 Co", "1 Cor"}, {"2 Corinthians", "2 Co", "2 Cor"}, {"Galatians", "Gal"}, {"Ephesians", "Eph"}, {"Philippians", "Phil"}, {"Colossians", "Col"}, {"1 Thessalonians", "1 Thess", "1 Thes"}, {"2 Thessalonians", "2 Thess", "2 Thes"}, {"1 Timothy", "1 Tim"}, {"2 Timothy", "2 Tim"}, {"Titus", "Tit"}, {"Philemon", "Phm"}, {"Hebrews", "Heb"}, {"James", "Jas", "Jam"}, {"1 Peter", "1 Pet"}, {"2 Peter", "2 Pet"}, {"1 John", "1 Jn"}, {"2 John", "2 Jn"}, {"3 John", "3 Jn"}, {"Jude"}, {"Revelation", "Rev"}};


    public ReferenceProcessor(){
    }

    public static ArrayList<HashMap<String,String>> processReferenceString(Context context, String data) {

        if (data.equals("")) {
            return null;
        }

        String[] references = data.split(";");
        String[] verseGroups = null;
        ArrayList<HashMap<String,String>> verseGroupList = new ArrayList<>();

        String book = "";

        for (String reference : references) {
            String bookChapter,
                    chapter = "",
                    verses = "";

            String[] split = reference.trim().split(":");
            bookChapter = split[0];
            String[] bookChapterProcess = bookChapter.split("(?<=[A-Za-z]) (?=[\\d])"); //Split when character behind and number after (ie between book & chapter)

            if (split.length == 2) { //If a split was made

                verses = split[1];

                //If distinction between book & chapter found
                if (bookChapterProcess.length > 1) {
                    book = bookChapterProcess[0].trim();
                    chapter = bookChapterProcess[1].trim();
                } else { //Otherwise re-use book from previous ref
                    chapter = bookChapterProcess[0].trim();
                }

            } else if (split.length == 1) { //If no ':' was found (ie Book + verse given)

                Log.i(TAG,reference);

                chapter = "1";

                if (bookChapterProcess.length > 1) {
                    book = bookChapterProcess[0].trim();
                    verses = bookChapterProcess[1].trim();
                } else {
                    verses =  bookChapterProcess[0].trim();
                }
            }

            ///////// Process chapter segment /////////
            int numVerseGroups = 1;

            if (verses.contains(",")) { //If there are multiple verses not in a row

                verseGroups = verses.split(",");
                numVerseGroups = verseGroups.length;

            }


            Log.i(TAG,book);
            int bookIndex = getBookIndex(book);
            if (bookIndex == -1) {
                Log.e(TAG,"Unable to find \"" + book + "\" in array!");
                continue; // Means that the book is invalid!
            }
            String bookFull = BIBLE[bookIndex];
            //String bookFull = BIBLE[Arrays.asList(BIBLE_ABBR).indexOf(book)];

            Log.i(TAG,"Book = " + bookFull + ", Chapter = " + chapter + ", Verses = " + verses + ", Verse Groups = " + Integer.toString(numVerseGroups));

            if (verses.contains("-")) { //If there are multiple verses in a row

                if (numVerseGroups > 1) {

                    for (int i = 0; i < numVerseGroups; i++) {

                        HashMap<String, String> verseGroup = new HashMap<>();

                        if (verseGroups[i].contains("-")) {
                            String[] versesSplit = verseGroups[i].split("-");
                            verseGroup.put(REFERENCE_START, versesSplit[0].trim());
                            verseGroup.put(REFERENCE_END, versesSplit[1].trim());
                            verseGroup.put(REFERENCE_TYPE, TYPE_CONCURRENT);
                        } else {
                            verseGroup.put(REFERENCE_VERSE, verseGroups[i].trim());
                            verseGroup.put(REFERENCE_TYPE, TYPE_SINGLE);
                        }

                        verseGroup.put(REFERENCE_FULL, book + " " + chapter + ":" + verseGroups[i].trim());
                        verseGroup.put(REFERENCE_BOOK, book);
                        verseGroup.put(REFERENCE_CHAPTER, chapter);
                        verseGroupList.add(verseGroup);

                    }
                } else {

                    HashMap<String, String> verseGroup = new HashMap<>();

                    String[] versesSplit = verses.split("-");

                    verseGroup.put(REFERENCE_START, versesSplit[0].trim());
                    verseGroup.put(REFERENCE_END, versesSplit[1].trim());
                    verseGroup.put(REFERENCE_FULL, book + " " + chapter + ":" + verses.trim());
                    verseGroup.put(REFERENCE_TYPE, TYPE_CONCURRENT);
                    verseGroup.put(REFERENCE_BOOK, book);
                    verseGroup.put(REFERENCE_CHAPTER, chapter);

                    verseGroupList.add(verseGroup);

                }
            } else {

                if (numVerseGroups == 1) {
                    HashMap<String,String> verseGroup  = new HashMap<>();
                    verseGroup.put(REFERENCE_BOOK,book);
                    verseGroup.put(REFERENCE_CHAPTER,chapter);
                    verseGroup.put(REFERENCE_VERSE,verses.trim());
                    verseGroup.put(REFERENCE_FULL,book + " " + chapter + ":" + verses.trim());
                    verseGroup.put(REFERENCE_TYPE,TYPE_SINGLE);

                    verseGroupList.add(verseGroup);

                } else if (numVerseGroups > 1 ) {
                    for (int i = 0; i < numVerseGroups; i++) {

                        HashMap<String,String> verseGroup  = new HashMap<>();

                        if (verseGroups[i].contains("-")) {
                            String[] versesSplit = verseGroups[i].split("-");
                            verseGroup.put(REFERENCE_START,versesSplit[0].trim());
                            verseGroup.put(REFERENCE_END,versesSplit[1].trim());
                            verseGroup.put(REFERENCE_TYPE,TYPE_CONCURRENT);
                        } else {
                            verseGroup.put(REFERENCE_VERSE,verseGroups[i].trim());
                            verseGroup.put(REFERENCE_TYPE,TYPE_SINGLE);
                        }

                        verseGroup.put(REFERENCE_FULL, book + " " + chapter + ":" + verseGroups[i].trim());
                        verseGroup.put(REFERENCE_BOOK,book);
                        verseGroup.put(REFERENCE_CHAPTER,chapter);

                        verseGroupList.add(verseGroup);

                    }
                } else {
                    Log.e(TAG,"Unable to process verses!");
                }

            }


        }

        verseGroupList = returnVerses(context,verseGroupList);


        return verseGroupList;
    }

    public static int getBookIndex(String value) {
        for (int book = 0; book < BIBLE_REF.length; book++)    {
            List<String> rowvalues = Arrays.asList(BIBLE_REF[book]);
            if(rowvalues.contains(value)) return book;
        }
        return -1;
    }

    private static ArrayList<HashMap<String,String>> returnVerses(Context context, ArrayList<HashMap<String,String>> referenceList){

        ArrayList<HashMap<String,String>> references = referenceList;

        String translation = PreferenceManager.getDefaultSharedPreferences(context).getString("translation","ESV");
        SQLiteDatabase bibleDB = context.openOrCreateDatabase(translation + ".db", MODE_PRIVATE, null);

        Cursor reading = null;

        for (HashMap<String,String> reference : references) {

            try {

                String book = reference.get(REFERENCE_BOOK);
                String chapter = reference.get(REFERENCE_CHAPTER);
                Log.i(TAG,"Book = " + book + ", Chapter = " + chapter + " Type = " + reference.get(REFERENCE_TYPE));

                String fullBook = BIBLE[getBookIndex(book)];

                //Get the current book number for use in verses db
                Cursor bookCur = bibleDB.rawQuery("SELECT * FROM books WHERE long_name = \'" + fullBook + "\'", null);

                bookCur.moveToFirst();
                String bookNumber = null;

                try {

                    bookNumber = bookCur.getString(bookCur.getColumnIndex("book_number"));
                    bookCur.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    Timber.i("Getting Book Number: " + "Invalid book name: " + book);
                    bookCur.close();
                }


                StringBuilder readingText = new StringBuilder();

                String verseType = reference.get(REFERENCE_TYPE);

                switch (verseType) {

                    case TYPE_SINGLE :
                        reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = " + chapter + " AND verse = " + reference.get(REFERENCE_VERSE).trim(), null);

                        break;
                    case TYPE_CONCURRENT :
                        reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = " + chapter + " AND verse BETWEEN " + reference.get(REFERENCE_START) + " AND " + reference.get(REFERENCE_END), null);
                        break;

                }

                if(reading.getCount() > 0) {
                    reading.moveToFirst();

                    int contentIndex = reading.getColumnIndex("text");

                    do {

                        readingText.append(reading.getString(contentIndex).replaceAll("’", "'").replaceAll("<f>.*?</f>", "").replaceAll("<[^>]*>","")).append(" ");//

                    } while (reading.moveToNext());

                    reference.put("Text", readingText.toString().trim());
                } else {
                    Log.e(TAG,"Unable to find verse in DB: " + book + " " + chapter);
                }

                reading.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        bibleDB.close();

        return references;

    }

    public static String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

}
