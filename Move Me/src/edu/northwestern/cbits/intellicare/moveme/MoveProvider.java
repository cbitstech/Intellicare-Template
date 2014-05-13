package edu.northwestern.cbits.intellicare.moveme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

public class MoveProvider extends ContentProvider 
{
    private static final int EXERCISES = 1;

    private static final String EXERCISES_TABLE = "exercises";
    
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.moveme";

    public static final Uri EXERCISES_URI = Uri.parse("content://" + AUTHORITY + "/" + EXERCISES_TABLE);

    private UriMatcher _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    private static final int DATABASE_VERSION = 1;

	protected static final String RECORDED = "recorded";
	protected static final String DURATION = "duration";
	protected static final String PRE_MOOD = "premood";
	protected static final String POST_MOOD = "postmood";

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
    }

	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
        switch(this._uriMatcher.match(uri))
        {
	        case MoveProvider.EXERCISES:
	            return this._db.delete(MoveProvider.EXERCISES_TABLE, selection, selectionArgs);
        }

		return 0;
	}

	public String getType(Uri uri) 
	{
        switch(this._uriMatcher.match(uri))
        {
            case MoveProvider.EXERCISES:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".exercises";
        }

        return null;
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
		Log.e("MM", "START INSERT");
		
        long insertedId = -1;

        Uri inserted = Uri.EMPTY;

        switch(this._uriMatcher.match(uri))
        {
            case MoveProvider.EXERCISES:
                insertedId = this._db.insert(EXERCISES_TABLE, null, values);
                
                Log.e("MM", "INSERTED " + insertedId);
                
                return Uri.withAppendedPath(EXERCISES_URI, "" + insertedId);
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
         }

        return null;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
        switch(this._uriMatcher.match(uri))
        {
	        case MoveProvider.EXERCISES:
	            return this._db.update(MoveProvider.EXERCISES_TABLE, values, selection, selectionArgs);
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

	public static String fetchNextEventTitle(Context context) 
	{
		List<CalendarEvent> events = MoveProvider.events(context);
		
		Log.e("MM", "EVENTS: " + events.size());
		
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
}
