/**
 * Created by Gabe on 9/23/13.
 */
package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class FeatsProvider extends android.content.ContentProvider
{
    private static final int RESPONSES = 1;
    private static final int RESPONSE = 2;
    private static final int FEATS = 3;
    private static final int FEAT = 4;

    private static final String RESPONSES_TABLE = "responses";
    private static final String FEATS_TABLE = "feats";
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.dailyfeats";

    public static final Uri RESPONSES_URI = Uri.parse("content://" + AUTHORITY + "/" + RESPONSES_TABLE);
    public static final Uri FEATS_URI = Uri.parse("content://" + AUTHORITY + "/" + FEATS_TABLE);

    public static final String DEPRESSION_LEVEL = "depression_level";
	protected static final String SUPPORTERS = "supporters";

    private UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase mDb = null;

    private static final int DATABASE_VERSION = 2;
    
    public FeatsProvider()
    {
    	super();
    	
    	this.mUriMatcher.addURI(AUTHORITY, RESPONSES_TABLE, RESPONSES);
    	this.mUriMatcher.addURI(AUTHORITY, RESPONSES_TABLE +  "/#",  RESPONSE);
    	this.mUriMatcher.addURI(AUTHORITY, FEATS_TABLE, FEATS);
    	this.mUriMatcher.addURI(AUTHORITY, FEATS_TABLE +  "/#",  FEAT);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        switch(this.mUriMatcher.match(uri))
        {
	        case FeatsProvider.FEATS:
	            return this.mDb.delete(FeatsProvider.FEATS_TABLE, selection, selectionArgs);
	        case FeatsProvider.RESPONSES:
	            return this.mDb.delete(FeatsProvider.RESPONSES_TABLE, selection, selectionArgs);
        }

		return 0;
    }

    public boolean onCreate()
    {
    	final Context context = this.getContext().getApplicationContext();
    	
    	SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "feats.db", null, FeatsProvider.DATABASE_VERSION)
    	{
			public void onCreate(SQLiteDatabase db) 
			{
		        db.execSQL(context.getString(R.string.db_create_responses_table));
		        db.execSQL(context.getString(R.string.db_create_feats_table));
		        
		        ContentValues feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_0));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_1));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_2));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_1_3));
		        feat.put("feat_level", 1);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_2_0));
		        feat.put("feat_level", 2);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_2_1));
		        feat.put("feat_level", 2);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_2_2));
		        feat.put("feat_level", 2);
		        db.insert(FEATS_TABLE, null, feat);

		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_0));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_1));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_2));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_3_3));
		        feat.put("feat_level", 3);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_0));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_1));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_2));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);
		        
		        feat = new ContentValues();
		        feat.put("feat_name", context.getString(R.string.feat_4_3));
		        feat.put("feat_level", 4);
		        db.insert(FEATS_TABLE, null, feat);

		        this.onUpgrade(db, 0, FeatsProvider.DATABASE_VERSION);
			}

			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
			{
                switch (oldVersion)
                {
                    case 0:
                    	
                    case 1: 
	                	db.execSQL(context.getString(R.string.db_alter_feats_table_add_enabled));
                    default:
                    	break;
                }
			}
    	};
    	
        this.mDb = helper.getWritableDatabase();

        return true;
    }

    public String getType(Uri uri)
    {
        switch(this.mUriMatcher.match(uri))
        {
            case FeatsProvider.RESPONSES:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".response";
            case FeatsProvider.RESPONSE:
                return "vnd.android.cursor.item/" + AUTHORITY + ".response";
            case FeatsProvider.FEATS:
                return "vnd.android.cursor.dir/"+AUTHORITY+".feat";
            case FeatsProvider.FEAT:
                return "vnd.android.cursor.item/"+AUTHORITY+".feat";
        }

        return null;
    }

    public Uri insert(Uri uri, ContentValues values)
    {
        long insertedId = -1;

        Uri inserted = Uri.EMPTY;

        switch(this.mUriMatcher.match(uri))
        {
            case FeatsProvider.RESPONSES:
                insertedId = this.mDb.insert(RESPONSES_TABLE, null, values);
                
                return Uri.withAppendedPath(RESPONSES_URI, "" + insertedId);

            case FeatsProvider.FEATS:
                insertedId = this.mDb.insert(FEATS_TABLE, null, values);

                return Uri.withAppendedPath(FEATS_URI, "" + insertedId);
        }

        return inserted;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        switch(this.mUriMatcher.match(uri))
        {
            case FeatsProvider.RESPONSE:
                return this.mDb.query(FeatsProvider.RESPONSES_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
            case FeatsProvider.RESPONSES:
                return this.mDb.query(FeatsProvider.RESPONSES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case FeatsProvider.FEAT:
                return this.mDb.query(FeatsProvider.FEATS_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
            case FeatsProvider.FEATS:
                return this.mDb.query(FeatsProvider.FEATS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
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
        switch(this.mUriMatcher.match(uri))
        {
	        case FeatsProvider.FEATS:
	            return this.mDb.update(FeatsProvider.FEATS_TABLE, values, selection, selectionArgs);
        }

		return 0;
	}

	public static boolean hasFeatForDate(Context context, String featName, Date date) 
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		long start = calendar.getTimeInMillis();
		
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		long end = calendar.getTimeInMillis();

		String selection = "recorded >= ? AND recorded <= ? AND feat = ?";
		String[] args = { "" + start, "" + end, featName };
		
		Cursor c = context.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, selection, args, null);
		
		boolean hasFeat = c.getCount() > 0;
		
		c.close();
		
		return hasFeat;
	}
	
	public static void clearFeats(Context context, String featName, Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		long start = calendar.getTimeInMillis();
		
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		long end = calendar.getTimeInMillis();

		String selection = "recorded >= ? AND recorded <= ? AND feat = ?";
		String[] args = { "" + start, "" + end, featName };
		
		context.getContentResolver().delete(FeatsProvider.RESPONSES_URI, selection, args);
	}
	
	public static void createFeat(Context context, String featName, int depressionLevel)
	{
		ContentValues values = new ContentValues();
		values.put("depression_level", depressionLevel);
		values.put("recorded", System.currentTimeMillis());
		values.put("feat", featName);
		
		context.getContentResolver().insert(RESPONSES_URI, values);
	}
}
