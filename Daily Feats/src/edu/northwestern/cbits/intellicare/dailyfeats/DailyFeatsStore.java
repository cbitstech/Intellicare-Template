/**
 * Created by Gabe on 9/23/13.
 */
package edu.northwestern.cbits.intellicare.dailyfeats;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class DailyFeatsStore extends android.content.ContentProvider
{
//    static final String LAST_UPDATE = "last_update";


    private static final int CHECKLISTS =       1;
    private static final int CHECKLIST =        2;
    private static final int FEATRESPONSES =    3;
    private static final int FEATRESPONSE =     4;

    private static final String CHECKLISTS_TABLE    = AppConstants.checklistsTableName;
    private static final String FEATRESPONSES_TABLE = AppConstants.featResponsesTableName;
    private static final String AUTHORITY           = "edu.northwestern.cbits.intellicare.dailyfeats";

    public static final Uri CHECKLISTS_URI = Uri.parse("content://" + AUTHORITY + "/" + CHECKLISTS_TABLE);
    public static final Uri FEATRESPONSES_URI = Uri.parse("content://" + AUTHORITY + "/" + FEATRESPONSES_TABLE);

    private static final UriMatcher _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        _uriMatcher.addURI(AUTHORITY, "checklists",        CHECKLISTS);
        _uriMatcher.addURI(AUTHORITY, "checklists/#",      CHECKLIST);
        _uriMatcher.addURI(AUTHORITY, "feat_responses",    FEATRESPONSES);
        _uriMatcher.addURI(AUTHORITY, "feat_responses/#",  FEATRESPONSE);
    }

    private SQLiteDatabase _db;

    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        return 0;
    }

    public boolean onCreate()
    {
        //CHRIS: NOT SURE WHAT TO DO ABOUT DOCUMENTATION:
        // "Database upgrade may take a long time, you should not call this method from the application main thread,
        // including from ContentProvider.onCreate()."
        // http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html#getWritableDatabase()
        // Found this:
        // http://stackoverflow.com/questions/4640039/where-should-android-applications-call-sqlite-getwritabledatabase
        // thinking i should refactor to AsyncTask as suggested. Not sure if that belongs here, or in StartupActivity?
        // impact on rest of code?
        DbHelper dbHelper = new DbHelper(getContext());
        _db = dbHelper.getWritableDatabase();
        Log.d("DB value", "DB => " + _db);

        return true;

    }

    public String getType(Uri uri)
    {
        switch(this._uriMatcher.match(uri))
        {
            case DailyFeatsStore.CHECKLISTS:
                return "vnd.android.cursor.dir/"+AUTHORITY+".checklist";
            case DailyFeatsStore.CHECKLIST:
                return "vnd.android.cursor.item/"+AUTHORITY+".checklist";
            case DailyFeatsStore.FEATRESPONSES:
                return "vnd.android.cursor.dir/"+AUTHORITY+".feat_response";
            case DailyFeatsStore.FEATRESPONSE:
                return "vnd.android.cursor.item/"+AUTHORITY+".feat_response";
        }

        return null;
    }

    public Uri insert(Uri uri, ContentValues values)
    {
        long insertedId;
        Uri inserted = Uri.EMPTY;

        Log.d("DF-Store", "about to match uri: " + uri.toString());
        int match = _uriMatcher.match(uri);
        Log.d("DF-Store", "Matched as: " + String.valueOf(match));
        switch(match)
        {
            case DailyFeatsStore.CHECKLISTS:
                Log.d("DF-Store", "matched checklists; values:"+values.toString());

                insertedId = _db.insert(CHECKLISTS_TABLE, null, values);
                Log.d("DF-Store", "After Insert");
                inserted = Uri.fromParts("content",
                                         "//"+AUTHORITY+"/"+CHECKLISTS_TABLE+"/",
                                         String.valueOf(insertedId));
                break;
            case DailyFeatsStore.FEATRESPONSES:
                Log.d("DF-Store", "matched feat_responses; values:"+values.toString());
                insertedId = _db.insert(FEATRESPONSES_TABLE, null, values);
                Log.d("DF-Store", "After Insert");
                inserted = Uri.fromParts("content",
                                         "//"+AUTHORITY+"/"+FEATRESPONSES_TABLE+"/",
                                         String.valueOf(insertedId));
                break;
            default:
                break;
        }

        return inserted;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        switch(this._uriMatcher.match(uri))
        {
            case DailyFeatsStore.CHECKLIST:
                return this._db.query(DailyFeatsStore.CHECKLISTS_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
            case DailyFeatsStore.CHECKLISTS:
                return this._db.query(DailyFeatsStore.CHECKLISTS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case DailyFeatsStore.FEATRESPONSE:
                return this._db.query(DailyFeatsStore.FEATRESPONSES_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
            case DailyFeatsStore.FEATRESPONSES:
                return this._db.query(DailyFeatsStore.FEATRESPONSES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
         }

        return null;
    }


    private String[] buildSingleSelectionArgs(Uri uri, String[] selectionArgs)
    {
        if (selectionArgs == null)
        {
            selectionArgs = new String[1];
            selectionArgs[0] = uri.getLastPathSegment();
        }
        else
        {
            String[] newSelectionArgs = new String[selectionArgs.length + 1];

            for (int i = 0; i < selectionArgs.length; i++)
            {
                newSelectionArgs[i] = selectionArgs[i];
            }

            newSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();

            selectionArgs = newSelectionArgs;
        }

        return selectionArgs;
    }

    private String buildSingleSelection(String selection)
    {
        if (selection == null)
            selection = "_id = ?";
        else
            selection = " AND _id = ?";

        return selection;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {

//          Do not need?
//        switch(this._uriMatcher.match(uri))
//        {
//            case ContentProvider.LESSONS_LIST:
//                return this._db.update(ContentProvider.LESSONS_TABLE, values, selection, selectionArgs);
//        }
//
        return 0;
    }
}
