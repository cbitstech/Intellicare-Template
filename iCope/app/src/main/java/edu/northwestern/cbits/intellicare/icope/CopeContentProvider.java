package edu.northwestern.cbits.intellicare.icope;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

public class CopeContentProvider extends ContentProvider 
{
	private static final int REMINDERS = 1;
	private static final int CARDS = 2;
	
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.icope";

    private static final String REMINDER_TABLE = "reminders";
    private static final String CARD_TABLE = "cards";

	protected static final Uri REMINDER_URI = Uri.parse("content://" + AUTHORITY + "/" + REMINDER_TABLE);;
	protected static final Uri CARD_URI = Uri.parse("content://" + AUTHORITY + "/" + CARD_TABLE);;

	protected static final String REMINDER_CARD_ID = "card_id";
	protected static final String REMINDER_HOUR = "hour";
	protected static final String REMINDER_MINUTE = "minute";
	protected static final String REMINDER_SUNDAY = "sunday";
	protected static final String REMINDER_MONDAY = "monday";
	protected static final String REMINDER_TUESDAY = "tuesday";
	protected static final String REMINDER_WEDNESDAY = "wednesday";
	protected static final String REMINDER_THURSDAY = "thursday";
	protected static final String REMINDER_FRIDAY = "friday";
	protected static final String REMINDER_SATURDAY = "saturday";

	protected static final String CARD_EVENT = "event";
	protected static final String CARD_REMINDER = "reminder";
	protected static final String CARD_ID = "_id";
	protected static final String CARD_TYPE = "card_type";
    protected static final String CARD_IMPORTANCE = "card_importance";

	private static final int DATABASE_VERSION = 1;
	public static final String ID = "_id";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase _db = null;
	
	public static class Reminder
	{
		public Date date;
		public long cardId;
		public long reminderId;
	}

    public CopeContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, REMINDER_TABLE, REMINDERS);
        this._matcher.addURI(AUTHORITY, CARD_TABLE, CARDS);
    }

    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "icope.db", null, CopeContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_reminders_table));
	            db.execSQL(context.getString(R.string.db_create_cards_table));

	            this.onUpgrade(db, 0, CopeContentProvider.DATABASE_VERSION);
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
        
        this._db  = helper.getWritableDatabase();

        return true;
	}

    public int delete(Uri uri, String where, String[] whereArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case CopeContentProvider.CARDS:
	            return this._db.delete(CopeContentProvider.CARD_TABLE, where, whereArgs);
	        case CopeContentProvider.REMINDERS:
	            return this._db.delete(CopeContentProvider.REMINDER_TABLE, where, whereArgs);
        }
        
        return 0;
	}

	@Override
	public String getType(Uri arg0) 
	{
		return null;
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
		long newId = -1;

		switch(this._matcher.match(uri))
        {
	        case CopeContentProvider.CARDS:
	        	newId = this._db.insert(CopeContentProvider.CARD_TABLE, null, values);
	        	break;
	        case CopeContentProvider.REMINDERS:
	        	newId = this._db.insert(CopeContentProvider.REMINDER_TABLE, null, values);
	        	break;
        }
		
		if (newId != -1)
			return Uri.withAppendedPath(uri, "" + newId);
		
		return null;
	}	

	public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String orderBy) 
	{
		switch(this._matcher.match(uri))
        {
	        case CopeContentProvider.CARDS:
	        	return this._db.query(CopeContentProvider.CARD_TABLE, columns, selection, selectionArgs, null, null, orderBy);
	        case CopeContentProvider.REMINDERS:
	        	return this._db.query(CopeContentProvider.REMINDER_TABLE, columns, selection, selectionArgs, null, null, orderBy);
        }
		
		return null;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		switch(this._matcher.match(uri))
        {
	        case CopeContentProvider.CARDS:
	        	return this._db.update(CopeContentProvider.CARD_TABLE, values, selection, selectionArgs);
	        case CopeContentProvider.REMINDERS:
	        	return this._db.update(CopeContentProvider.REMINDER_TABLE, values, selection, selectionArgs);
        }
		
		return 0;
	}

	public static List<String> listCardTypes(Context context) 
	{
		ArrayList<String> types = new ArrayList<String>();
		
		Cursor c = context.getContentResolver().query(CopeContentProvider.CARD_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			String type = c.getString(c.getColumnIndex(CopeContentProvider.CARD_TYPE));
			
			if (type != null && type.trim().length() > 0 && types.contains(type) == false)
				types.add(type);
		}
		
		c.close();
		
		return types;
	}

	public static ArrayList<Reminder> listUpcomingReminders(Context context) 
	{
		ArrayList<Reminder> list = new ArrayList<Reminder>();

		Cursor cursor = context.getContentResolver().query(CopeContentProvider.REMINDER_URI, null, null, null, null);

		final long now = System.currentTimeMillis();

		while (cursor.moveToNext())
		{
			for (int i = 0; i < 7; i++)
			{
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(now + (i * 24 * 60 * 60 * 1000 ));
				
				c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_HOUR)));
				c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MINUTE)));
				c.set(Calendar.SECOND, 0);

				int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
				
				boolean include = false;
				
				switch (dayOfWeek % 8)
				{
					case Calendar.SUNDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_SUNDAY)) != 0); 
						break;
					case Calendar.MONDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MONDAY)) != 0); 
						break;
					case Calendar.TUESDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_TUESDAY)) != 0); 
						break;
					case Calendar.WEDNESDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_WEDNESDAY)) != 0); 
						break;
					case Calendar.THURSDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_THURSDAY)) != 0); 
						break;
					case Calendar.FRIDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_FRIDAY)) != 0); 
						break;
					case Calendar.SATURDAY:
						include = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_SATURDAY)) != 0); 
						break;
				}
				
				Date date = c.getTime();
				
				if (include && date.getTime() > now)
				{
					Reminder r = new Reminder();
					r.date = date;
					r.cardId = cursor.getLong(cursor.getColumnIndex(CopeContentProvider.REMINDER_CARD_ID));
					r.reminderId = cursor.getLong(cursor.getColumnIndex(CopeContentProvider.ID));
					list.add(r);
				}
			}
		}
		
		cursor.close();
		
		Collections.sort(list, new Comparator<Reminder>()
		{
			public int compare(Reminder one, Reminder two) 
			{
				return one.date.compareTo(two.date);
			}
		});

		return list;
	}

	public static String[] getCategories(Context context) 
	{
		ArrayList<String> categories = new ArrayList<String>();
		
		Cursor c = context.getContentResolver().query(CopeContentProvider.CARD_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			String category = c.getString(c.getColumnIndex(CopeContentProvider.CARD_TYPE)).trim();
			
			if (categories.contains(category) == false)
				categories.add(category);
		}

		c.close();
		
		Collections.sort(categories);
		
		String[] categoriesArray = new String[categories.size()];
		
		for (int i = 0; i < categories.size(); i++)
		{
			categoriesArray[i] = categories.get(i);
		}

		return categoriesArray;
	}
}
