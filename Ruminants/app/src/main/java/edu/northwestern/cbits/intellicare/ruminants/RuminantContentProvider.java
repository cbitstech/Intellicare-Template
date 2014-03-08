package edu.northwestern.cbits.intellicare.ruminants;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.StatFs;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLException;

public class RuminantContentProvider extends ContentProvider
{
    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
    /*
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.ruminants.survey";
    private static final String FILENAME = "survey.sqlite3";
    private static final String SURVEY_TABLE = "survey_logs";

    public static final Uri SURVEY_URI = Uri.parse("content://" + AUTHORITY + "/" + SURVEY_TABLE);

    public static final String SURVEY_RUMINATION_ISOVER = "rumination_isOver";
    public static final String SURVEY_EMOTION = "emotion";
    public static final String SURVEY_TRIGGER = "trigger";
    public static final String SURVEY_DURATION = "rumination_duration";
    public static final String SURVEY_RUMINATION_STRATEGY = "rumination_strategy";
    public static final String SURVEY_RUMINATION_TERMINATION_CAUSE = "termination_cause";
    public static final String SURVEY_TIMESTAMP = "timestamp";
    public static final String SURVEY_URI = "survey_uri";

    private static final int DATABASE_VERSION = 0;

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    private static HashMap<String, String> _cachedHashes = new HashMap<String, String>();

    public RuminantContentProvider()
    {
    	super();
        this._matcher.addURI(AUTHORITY, SURVEY_TABLE, SURVEY_URI);

    }

    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();
        
        SQLiteOpenHelper helper = new SQLiteOpenHelper(path.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)
        {

            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_survey_table));

	            this.onUpgrade(db, 0, RuminantContentProvider.DATABASE_VERSION);
            }
        
        this._db  = helper.getWritableDatabase();

        return true;
	}

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        return this._db.query(RuminantContentProvider.SURVEY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        return 0;
    }

    public int delete(Uri uri, String where, String[] args)
    {
        return 0;
    }

    public String getType(Uri uri)
    {
        return "vnd.android.cursor.dir/" + AUTHORITY + ".entry";
    }

    public Uri insert(Uri uri, ContentValues values)
    {
        return null;
    }

*/
}

