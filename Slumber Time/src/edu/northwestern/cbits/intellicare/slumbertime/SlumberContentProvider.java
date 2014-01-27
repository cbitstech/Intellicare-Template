package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class SlumberContentProvider extends ContentProvider 
{
    private static final int ALARMS = 1;
    private static final int NOTES = 2;
    private static final int TIPS = 3;

    private static final String ALARMS_TABLE = "alarms";
    private static final String NOTES_TABLE = "notes";
    private static final String TIPS_TABLE = "tips";
    
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.slumbertime";

    public static final Uri ALARMS_URI = Uri.parse("content://" + AUTHORITY + "/" + ALARMS_TABLE);
    public static final Uri NOTES_URI = Uri.parse("content://" + AUTHORITY + "/" + NOTES_TABLE);
    public static final Uri TIPS_URI = Uri.parse("content://" + AUTHORITY + "/" + TIPS_TABLE);

    private static final int DATABASE_VERSION = 2;

    protected static final String ALARM_NAME = "name";
    protected static final String ALARM_HOUR = "hour";
    protected static final String ALARM_MINUTE = "minute";
	public static final String ALARM_SUNDAY = "sunday";
    public static final String ALARM_MONDAY = "monday";
	public static final String ALARM_TUESDAY = "tuesday";
	public static final String ALARM_WEDNESDAY = "wednesday";
	public static final String ALARM_THURSDAY = "thursday";
	public static final String ALARM_FRIDAY = "friday";
	public static final String ALARM_SATURDAY = "saturday";
	public static final String ALARM_ENABLED = "enabled";
	public static final String ALARM_CONTENT_URI = "content_uri";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    public SlumberContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, ALARMS_TABLE, ALARMS);
        this._matcher.addURI(AUTHORITY, NOTES_TABLE, NOTES);
        this._matcher.addURI(AUTHORITY, TIPS_TABLE, TIPS);
    }

    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();
        
        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "conductor.db", null, SlumberContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_alarms_table));
	            db.execSQL(context.getString(R.string.db_create_notes_table));
	            db.execSQL(context.getString(R.string.db_create_tips_table));
	
	            this.onUpgrade(db, 0, SlumberContentProvider.DATABASE_VERSION);
            }

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
            {
            	switch (oldVersion)
            	{
	                case 0:
	
	                case 1:
	                	db.execSQL(context.getString(R.string.db_update_alarms_add_enabled));
	                default:
                        break;
            	}
            }
        };
        
        this._db  = helper.getWritableDatabase();

        return true;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
        switch(this._matcher.match(uri))
        {
	        case SlumberContentProvider.ALARMS:
	            return this._db.query(SlumberContentProvider.ALARMS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case SlumberContentProvider.TIPS:
	            return this._db.query(SlumberContentProvider.TIPS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case SlumberContentProvider.NOTES:
	            return this._db.query(SlumberContentProvider.NOTES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
        
        return null;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case SlumberContentProvider.ALARMS:
	            return this._db.update(SlumberContentProvider.ALARMS_TABLE, values, selection, selectionArgs);
	        case SlumberContentProvider.TIPS:
	            return this._db.update(SlumberContentProvider.TIPS_TABLE, values, selection, selectionArgs);
	        case SlumberContentProvider.NOTES:
	            return this._db.update(SlumberContentProvider.NOTES_TABLE, values, selection, selectionArgs);
        }

		return 0;
	}

	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case SlumberContentProvider.ALARMS:
	            return this._db.delete(SlumberContentProvider.ALARMS_TABLE, selection, selectionArgs);
	        case SlumberContentProvider.TIPS:
	            return this._db.delete(SlumberContentProvider.TIPS_TABLE, selection, selectionArgs);
	        case SlumberContentProvider.NOTES:
	            return this._db.delete(SlumberContentProvider.NOTES_TABLE, selection, selectionArgs);
        }

        return 0;
	}

	public String getType(Uri uri) 
	{
        switch(this._matcher.match(uri))
        {
	        case SlumberContentProvider.ALARMS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".alarm";
	        case SlumberContentProvider.TIPS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".tip";
	        case SlumberContentProvider.NOTES:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".note";
        }
        
        return null;
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
		long id = -1;
		
        switch(this._matcher.match(uri))
        {
	        case SlumberContentProvider.ALARMS:
	            id = this._db.insert(SlumberContentProvider.ALARMS_TABLE, null, values);
	            break;
	        case SlumberContentProvider.TIPS:
	            id = this._db.insert(SlumberContentProvider.ALARMS_TABLE, null, values);
	            break;
	        case SlumberContentProvider.NOTES:
	            id = this._db.insert(SlumberContentProvider.ALARMS_TABLE, null, values);
	            break;
        }
        
        return Uri.withAppendedPath(uri, "" + id);
	}

	public static String dateStringForAlarmCursor(Context context, Cursor cursor) 
	{
		int hour = cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_HOUR));
		int minute = cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_MINUTE));
		
		String dateString = hour + ":";
		
		if (minute < 10)
			dateString += "0";
		
		dateString += minute + " (";
		
		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_SUNDAY)) > 0)
			dateString += context.getString(R.string.abbrev_sunday);

		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_MONDAY)) > 0)
			dateString += context.getString(R.string.abbrev_monday);

		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_TUESDAY)) > 0)
			dateString += context.getString(R.string.abbrev_tuesday);

		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_WEDNESDAY)) > 0)
			dateString += context.getString(R.string.abbrev_wednesday);

		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_THURSDAY)) > 0)
			dateString += context.getString(R.string.abbrev_thursday);

		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_FRIDAY)) > 0)
			dateString += context.getString(R.string.abbrev_friday);

		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_SATURDAY)) > 0)
			dateString += context.getString(R.string.abbrev_saturday);
		
		dateString += ")";
		
		return dateString;
	}
}
