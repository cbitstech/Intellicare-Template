package edu.northwestern.cbits.intellicare.avast;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DataContentProvider extends ContentProvider 
{
    private static final int VENUE_TYPES = 1;
    private static final int LOCATION = 2;

    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.avast";

    private static final String VENUE_TYPE_TABLE = "venue_types";
    private static final String LOCATION_TABLE = "locations";

    public static final Uri VENUE_TYPE_URI = Uri.parse("content://" + AUTHORITY + "/" + VENUE_TYPE_TABLE);
    public static final Uri LOCATION_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATION_TABLE);

    private static final int DATABASE_VERSION = 1;

    public static final String LOCATION_NAME = "name";
    public static final String LOCATION_LATITUDE = "latitude";
    public static final String LOCATION_LONGITUDE = "longitude";
    public static final String LOCATION_RADIUS = "radius";
    public static final String LOCATION_DURATION = "duration";
    public static final String LOCATION_ENABLED = "enabled";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase _db = null;
    
    // content://edu.northwestern.cbits.intellicare.avast/venue_types
	// Cursor cursor = context.getContentResolver().query(DataContentProvider.VENUE_TYPE_URI, columns, where, whereArgs, sortOrder);

    // content://edu.northwestern.cbits.intellicare.avast/locations/1

    public DataContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, VENUE_TYPE_TABLE, VENUE_TYPES);
        this._matcher.addURI(AUTHORITY, LOCATION_TABLE, LOCATION);
    }
    
    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();

        /*
        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "avast.db", null, DataContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_alarms_table));
	            db.execSQL(context.getString(R.string.db_create_notes_table));

	            this.onUpgrade(db, 0, DataContentProvider.DATABASE_VERSION);
            }
            
            // Called when DB version number in DB file is not same as code. Updates database in place instead of blowing away and rebuilding (migration)...

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
            {
            	switch (oldVersion)
            	{
	                case 0:

	                case 1:
	                	db.execSQL(context.getString(R.string.db_update_alarms_add_enabled));
	                case 2:
	                	db.execSQL(context.getString(R.string.db_create_checklist_items_table));

	                	for (String item : context.getResources().getStringArray(R.array.initial_checklist_items))
	                	{
	                		String[] tokens = item.split("/");

	                		if (tokens.length > 1)
	                		{
		                		ContentValues values = new ContentValues();
		                		values.put(SlumberContentProvider.CHECKLIST_ITEM_NAME, tokens[0].trim());
		                		values.put(SlumberContentProvider.CHECKLIST_ITEM_CATEGORY, tokens[1].trim());

		                		db.insert(SlumberContentProvider.CHECKLIST_ITEMS_TABLE, null, values);
	                		}	                		
	                	}
	                case 3:
	                	db.execSQL(context.getString(R.string.db_create_checklist_events_table));
	                case 4:
	                	db.execSQL(context.getString(R.string.db_create_sleep_diary_table));
	                case 5:
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_comments));
	                case 6:
	                	db.execSQL(context.getString(R.string.db_create_sensor_readings_table));
	                case 7:
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_rested));
	                default:
                        break;
            	}
            }
        };
        
        */
        
//        this._db  = helper.getWritableDatabase();

        return true;
	}

    
	public int delete(Uri uri, String where, String[] whereArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case DataContentProvider.VENUE_TYPES:
	            return this._db.delete(DataContentProvider.VENUE_TYPE_TABLE, where, whereArgs);
	        case DataContentProvider.LOCATION:
	            return this._db.delete(DataContentProvider.LOCATION_TABLE, where, whereArgs);
        }
		
		return 0;
	}

	@Override
	public String getType(Uri uri) 
	{
        switch(this._matcher.match(uri))
        {
	        case DataContentProvider.VENUE_TYPES:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".venue_type";
	        case DataContentProvider.LOCATION:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".location";
        }
        
        return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
        switch(this._matcher.match(uri))
        {
	        case DataContentProvider.VENUE_TYPES:
	        	break;
	        case DataContentProvider.LOCATION:
	            long newLocationId = this._db.insert(DataContentProvider.LOCATION_TABLE, null, values);
	            
	            return Uri.withAppendedPath(uri, "" + newLocationId);
        }
		
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
        switch(this._matcher.match(uri))
        {
	        case DataContentProvider.VENUE_TYPES:
	            return this._db.query(DataContentProvider.VENUE_TYPE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case DataContentProvider.LOCATION:
	            return this._db.query(DataContentProvider.LOCATION_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
		
		return null;
	}

	// String where = "name = ?";
	// String[] whereArgs = { "pizza hut" };
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case DataContentProvider.VENUE_TYPES:
	            return this._db.update(DataContentProvider.VENUE_TYPE_TABLE, values, where, whereArgs);
	        case DataContentProvider.LOCATION:
	            return this._db.update(DataContentProvider.LOCATION_TABLE, values, where, whereArgs);
        }
		
		return 0;
	}
}
