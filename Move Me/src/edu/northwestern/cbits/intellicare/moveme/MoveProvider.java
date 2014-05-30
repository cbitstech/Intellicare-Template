package edu.northwestern.cbits.intellicare.moveme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;

public class MoveProvider extends ContentProvider 
{
    private static final int EXERCISES = 1;
    private static final int FITBIT = 2;

    private static final String EXERCISES_TABLE = "exercises";
    private static final String FITBIT_TABLE = "fitbit";
    
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.moveme";

    public static final Uri EXERCISES_URI = Uri.parse("content://" + AUTHORITY + "/" + EXERCISES_TABLE);
	public static final Uri FITBIT_URI = Uri.parse("content://" + AUTHORITY + "/" + FITBIT_TABLE);;

    private UriMatcher _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    private static final int DATABASE_VERSION = 2;

	protected static final String RECORDED = "recorded";
	protected static final String DURATION = "duration";
	protected static final String PRE_MOOD = "premood";
	protected static final String POST_MOOD = "postmood";

	public static final String FITBIT_TIMESTAMP = "timestamp";
	public static final String FITBIT_LOGGED = "logged";
	public static final String FITBIT_GOAL = "goal";

	public static class CalendarEvent
	{
		public String title = null;
		public long start = 0;
		public long id = -1;

		public CalendarEvent(String title, long start, long id) 
		{
			this.title = title;
			this.start = start;
			this.id = id;
		}
	}
	
    public MoveProvider()
    {
    	super();
    	
    	this._uriMatcher.addURI(AUTHORITY, EXERCISES_TABLE, EXERCISES);
    	this._uriMatcher.addURI(AUTHORITY, FITBIT_TABLE, FITBIT);
    }

	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
        switch(this._uriMatcher.match(uri))
        {
	        case MoveProvider.EXERCISES:
	            return this._db.delete(MoveProvider.EXERCISES_TABLE, selection, selectionArgs);
	        case MoveProvider.FITBIT:
	            return this._db.delete(MoveProvider.FITBIT_TABLE, selection, selectionArgs);
        }

		return 0;
	}

	public String getType(Uri uri) 
	{
        switch(this._uriMatcher.match(uri))
        {
	        case MoveProvider.EXERCISES:
	            return "vnd.android.cursor.dir/" + AUTHORITY + ".exercises";
	        case MoveProvider.FITBIT:
	            return "vnd.android.cursor.dir/" + AUTHORITY + ".fitbit";
        }
        
        return null;
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
        long insertedId = -1;

        Uri inserted = Uri.EMPTY;

        switch(this._uriMatcher.match(uri))
        {
            case MoveProvider.EXERCISES:
                insertedId = this._db.insert(EXERCISES_TABLE, null, values);
                return Uri.withAppendedPath(EXERCISES_URI, "" + insertedId);
            case MoveProvider.FITBIT:
                insertedId = this._db.insert(FITBIT_TABLE, null, values);
                return Uri.withAppendedPath(FITBIT_URI, "" + insertedId);
        }

        return inserted;
	}

    public boolean onCreate()
    {
    	final Context context = this.getContext().getApplicationContext();
    	
    	SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "exercises.db", null, MoveProvider.DATABASE_VERSION)
    	{
			public void onCreate(SQLiteDatabase db) 
			{
		        db.execSQL(context.getString(R.string.db_create_exercises_table));

		        this.onUpgrade(db, 0, MoveProvider.DATABASE_VERSION);
			}

			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
			{
                switch (oldVersion)
                {
                    case 0:
                    	
                    case 1:
        		        db.execSQL(context.getString(R.string.db_create_fitbit_table));
                    default:
                    	break;
                }
			}
    	};
    	
        this._db = helper.getWritableDatabase();

        return true;
    }

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
        switch(this._uriMatcher.match(uri))
        {
            case MoveProvider.EXERCISES:
                return this._db.query(MoveProvider.EXERCISES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case MoveProvider.FITBIT:
                return this._db.query(MoveProvider.FITBIT_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
         }

        return null;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
        switch(this._uriMatcher.match(uri))
        {
	        case MoveProvider.EXERCISES:
	            return this._db.update(MoveProvider.EXERCISES_TABLE, values, selection, selectionArgs);
            case MoveProvider.FITBIT:
                return this._db.update(MoveProvider.FITBIT_TABLE, values, selection, selectionArgs);
        }

		return 0;
	}
	
	public static List<CalendarEvent> events(Context context)
	{
		ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();
		
		long now = System.currentTimeMillis();

		String selection = CalendarContract.Events.DESCRIPTION + " LIKE ? AND " + CalendarContract.Events.DTEND + " > ?";
		String[] args = { "%" + context.getString(R.string.event_description) + "%", "" + now };

		Cursor c = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, args, CalendarContract.Events.DTSTART);
		
		while (c.moveToNext())
		{
			String eventTitle = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE));
			
			long id = c.getLong(c.getColumnIndex(CalendarContract.Events._ID));
			
			long start = c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART));
			
			CalendarEvent event = new CalendarEvent(eventTitle, start, id);
			
			events.add(event);
			
		}
		
		c.close();
		
		return events;
	}

	@SuppressLint("SimpleDateFormat")
	public static String fetchNextEventTitle(Context context) 
	{
		List<CalendarEvent> events = MoveProvider.events(context);
		
		if (events.size() > 0)
		{
			CalendarEvent event = events.get(0);
			
			Date date = new Date(event.start);
			
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
			DateFormat dateFormat = new SimpleDateFormat("E., ");
			
			return dateFormat.format(date) + timeFormat.format(date) + ": " + event.title;
		}

		return context.getString(R.string.label_no_upcoming_events);
	}

	public static int goal(Context context, long timestamp) 
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int goal = prefs.getInt(SettingsActivity.SETTING_DAILY_GOAL, SettingsActivity.SETTING_DAILY_GOAL_DEFAULT);
		
		return goal * (1000 * 60);
		
		/*

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		long startTime = cal.getTimeInMillis();
		
		cal.add(Calendar.DATE, 1);
		
		long endTime = cal.getTimeInMillis() - 1;

		int count = 30 * (60 * 1000);

		String where = MoveProvider.RECORDED + " >= ? AND " + MoveProvider.RECORDED + " <= ?";
		String[] args = { "" + startTime, "" + endTime }; 
		
//		Cursor c = context.getContentResolver().query(MoveProvider.EXERCISES_URI, null, where, args, null);
//
//		if (c.moveToNext())
//			count = c.getInt(c.getColumnIndex(MoveProvider.DURATION));
//		
//		c.close();

		if (count == 0)
		{
			where = MoveProvider.FITBIT_TIMESTAMP + " >= ? AND " + MoveProvider.FITBIT_TIMESTAMP + " <= ?";
			
			Cursor c = context.getContentResolver().query(MoveProvider.FITBIT_URI, null, where, args, null);
	
			if (c.moveToNext())
				count = c.getInt(c.getColumnIndex(MoveProvider.FITBIT_GOAL)) * (60 * 1000);
			
			c.close();
		}
		
		return count; */
	}

	public static int progress(Context context, long timestamp) 
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		long startTime = cal.getTimeInMillis();
		
		cal.add(Calendar.DATE, 1);
		
		long endTime = cal.getTimeInMillis() - 1;

		int count = 0;

		String where = MoveProvider.RECORDED + " >= ? AND " + MoveProvider.RECORDED + " <= ?";
		String[] args = { "" + startTime, "" + endTime }; 
		
		Cursor c = context.getContentResolver().query(MoveProvider.EXERCISES_URI, null, where, args, null);

		while (c.moveToNext())
			count += c.getInt(c.getColumnIndex(MoveProvider.DURATION));
		
		c.close();
		
		if (count == 0)
		{
			where = MoveProvider.FITBIT_TIMESTAMP + " >= ? AND " + MoveProvider.FITBIT_TIMESTAMP + " <= ?";
			
			c = context.getContentResolver().query(MoveProvider.FITBIT_URI, null, where, args, null);
	
			if (c.moveToNext())
				count = c.getInt(c.getColumnIndex(MoveProvider.FITBIT_LOGGED)) * (60 * 1000);
			
			c.close();
		}
		
		return count;
	}
	
	public static int remainingTime(Context context)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		int offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
		
		cal.add(Calendar.DATE, -7);
		
		int total = 0;
		int week = 0;

		for (int i = 0; i < 7; i++)
		{
			long time = cal.getTimeInMillis() + offset;
				
			int complete = MoveProvider.progress(context, time);
			int goal = MoveProvider.goal(context, time);
			
			total += goal;
			week += complete;
				
			cal.add(Calendar.DATE, 1);
		}
		
		int remaining = total - week;
		
		if (remaining < 0)
			remaining = 0;
		
		return remaining;
	}
	
	
}
