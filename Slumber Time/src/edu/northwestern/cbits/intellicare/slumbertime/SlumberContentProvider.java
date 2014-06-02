package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.format.DateFormat;

public class SlumberContentProvider extends ContentProvider 
{
    private static final int ALARMS = 1;
    private static final int NOTES = 2;
    private static final int TIPS = 3;
    private static final int CHECKLIST_ITEMS = 4;
    private static final int CHECKLIST_EVENTS = 5;
    private static final int SLEEP_DIARIES = 6;
    private static final int SENSOR_READINGS = 7;

    private static final String ALARMS_TABLE = "alarms";
    private static final String NOTES_TABLE = "notes";
    private static final String TIPS_TABLE = "tips";
    private static final String CHECKLIST_ITEMS_TABLE = "checklist_items";
    private static final String CHECKLIST_EVENTS_TABLE = "checklist_events";
    private static final String SLEEP_DIARIES_TABLE = "sleep_diaries";
    private static final String SENSOR_READINGS_TABLE = "sensor_readings";
    
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.slumbertime";

    public static final Uri ALARMS_URI = Uri.parse("content://" + AUTHORITY + "/" + ALARMS_TABLE);
    public static final Uri NOTES_URI = Uri.parse("content://" + AUTHORITY + "/" + NOTES_TABLE);
    public static final Uri TIPS_URI = Uri.parse("content://" + AUTHORITY + "/" + TIPS_TABLE);
    public static final Uri CHECKLIST_ITEMS_URI = Uri.parse("content://" + AUTHORITY + "/" + CHECKLIST_ITEMS_TABLE);
    public static final Uri CHECKLIST_EVENTS_URI = Uri.parse("content://" + AUTHORITY + "/" + CHECKLIST_EVENTS_TABLE);
    public static final Uri SLEEP_DIARIES_URI = Uri.parse("content://" + AUTHORITY + "/" + SLEEP_DIARIES_TABLE);
    public static final Uri SENSOR_READINGS_URI = Uri.parse("content://" + AUTHORITY + "/" + SENSOR_READINGS_TABLE);

    private static final int DATABASE_VERSION = 9;

    public static final String ALARM_NAME = "name";
    public static final String ALARM_HOUR = "hour";
    public static final String ALARM_MINUTE = "minute";
	public static final String ALARM_SUNDAY = "sunday";
    public static final String ALARM_MONDAY = "monday";
	public static final String ALARM_TUESDAY = "tuesday";
	public static final String ALARM_WEDNESDAY = "wednesday";
	public static final String ALARM_THURSDAY = "thursday";
	public static final String ALARM_FRIDAY = "friday";
	public static final String ALARM_SATURDAY = "saturday";
	public static final String ALARM_ENABLED = "enabled";
	public static final String ALARM_CONTENT_URI = "content_uri";
	public static final String NOTE_TEXT = "note";
	public static final String NOTE_TIMESTAMP = "timestamp";
	public static final String CHECKLIST_ITEM_NAME = "name";
	public static final String CHECKLIST_ITEM_CATEGORY = "category";
	public static final String CHECKLIST_ITEM_ENABLED = "enabled";

	public static final String CHECKLIST_EVENT_ITEM_ID = "item_id";
	public static final String CHECKLIST_EVENT_TIMESTAMP = "timestamp";

	public static final String DIARY_NAP = "did_nap";
	public static final String DIARY_EARLIER = "woke_earlier";
	public static final String DIARY_BED_HOUR = "bed_hour";
	public static final String DIARY_BED_MINUTE = "bed_minute";
	public static final String DIARY_SLEEP_HOUR = "sleep_hour";
	public static final String DIARY_SLEEP_MINUTE = "sleep_minute";
	public static final String DIARY_WAKE_HOUR = "wake_hour";
	public static final String DIARY_WAKE_MINUTE = "wake_minute";
	public static final String DIARY_UP_HOUR = "up_hour";
	public static final String DIARY_UP_MINUTE = "up_minute";
	public static final String DIARY_SLEEP_DELAY = "sleep_delay";
	public static final String DIARY_WAKE_COUNT = "wake_count";
	public static final String DIARY_SLEEP_QUALITY = "sleep_quality";
	public static final String DIARY_TIMESTAMP = "timestamp";
	public static final String DIARY_COMMENTS = "comments";
	public static final String DIARY_RESTED = "rested";

	public static final String READING_NAME = "name";
	public static final String READING_VALUE = "value";
	public static final String READING_RECORDED = "recorded";

	public static final String AUDIO_FREQUENCY = "audio_frequency";
	public static final String AUDIO_MAGNITUDE = "audio_magnitude";
	public static final String LIGHT_LEVEL = "light_level";
	public static final String TEMPERATURE = "temperature";

	private static final double MAX_LIGHT_LEVEL = 500; // SI lux

	public static final String READING_MINIMUM = "reading_minimum";
	public static final String READING_MULTIPLIER = "reading_multiplier";

	public static final String DIARY_NAP_DURATION = "nap_duration";
	public static final String DIARY_ALCOHOL = "had_alcohol";
	public static final String DIARY_ALCOHOL_AMOUNT = "alcohol_amount";
	public static final String DIARY_ALCOHOL_TIME = "alcohol_time";
	public static final String DIARY_CAFFEINE = "had_caffeine";
	public static final String DIARY_CAFFEINE_AMOUNT = "caffeine_amount";
	public static final String DIARY_CAFFEINE_TIME = "caffeine_time";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    public SlumberContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, ALARMS_TABLE, ALARMS);
        this._matcher.addURI(AUTHORITY, NOTES_TABLE, NOTES);
        this._matcher.addURI(AUTHORITY, TIPS_TABLE, TIPS);
        this._matcher.addURI(AUTHORITY, CHECKLIST_ITEMS_TABLE, CHECKLIST_ITEMS);
        this._matcher.addURI(AUTHORITY, CHECKLIST_EVENTS_TABLE, CHECKLIST_EVENTS);
        this._matcher.addURI(AUTHORITY, SLEEP_DIARIES_TABLE, SLEEP_DIARIES);
        this._matcher.addURI(AUTHORITY, SENSOR_READINGS_TABLE, SENSOR_READINGS);
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
	                case 8:
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_nap_duration));
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_had_alcohol));
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_alcohol_amount));
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_alcohol_time));
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_had_caffeine));
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_caffeine_amount));
	                	db.execSQL(context.getString(R.string.db_update_sleep_diary_add_caffeine_time));
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
	        case SlumberContentProvider.CHECKLIST_ITEMS:
	            return this._db.query(SlumberContentProvider.CHECKLIST_ITEMS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case SlumberContentProvider.CHECKLIST_EVENTS:
	            return this._db.query(SlumberContentProvider.CHECKLIST_EVENTS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case SlumberContentProvider.SLEEP_DIARIES:
	            return this._db.query(SlumberContentProvider.SLEEP_DIARIES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case SlumberContentProvider.SENSOR_READINGS:
	            return this._db.query(SlumberContentProvider.SENSOR_READINGS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
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
	        case SlumberContentProvider.CHECKLIST_ITEMS:
	            return this._db.update(SlumberContentProvider.CHECKLIST_ITEMS_TABLE, values, selection, selectionArgs);
	        case SlumberContentProvider.CHECKLIST_EVENTS:
	            return this._db.update(SlumberContentProvider.CHECKLIST_EVENTS_TABLE, values, selection, selectionArgs);
	        case SlumberContentProvider.SLEEP_DIARIES:
	            return this._db.update(SlumberContentProvider.SLEEP_DIARIES_TABLE, values, selection, selectionArgs);
	        case SlumberContentProvider.SENSOR_READINGS:
	            return this._db.update(SlumberContentProvider.SENSOR_READINGS_TABLE, values, selection, selectionArgs);
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
	        case SlumberContentProvider.CHECKLIST_ITEMS:
	            return this._db.delete(SlumberContentProvider.CHECKLIST_ITEMS_TABLE, selection, selectionArgs);
	        case SlumberContentProvider.CHECKLIST_EVENTS:
	            return this._db.delete(SlumberContentProvider.CHECKLIST_EVENTS_TABLE, selection, selectionArgs);
	        case SlumberContentProvider.SLEEP_DIARIES:
	            return this._db.delete(SlumberContentProvider.SLEEP_DIARIES_TABLE, selection, selectionArgs);
	        case SlumberContentProvider.SENSOR_READINGS:
	            return this._db.delete(SlumberContentProvider.SENSOR_READINGS_TABLE, selection, selectionArgs);
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
	        case SlumberContentProvider.CHECKLIST_ITEMS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".checklist_item";
	        case SlumberContentProvider.CHECKLIST_EVENTS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".checklist_event";
	        case SlumberContentProvider.SLEEP_DIARIES:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".sleep_diary";
	        case SlumberContentProvider.SENSOR_READINGS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".sensor_reading";
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
	            id = this._db.insert(SlumberContentProvider.NOTES_TABLE, null, values);
	            break;
	        case SlumberContentProvider.CHECKLIST_ITEMS:
	            id = this._db.insert(SlumberContentProvider.CHECKLIST_ITEMS_TABLE, null, values);
	            break;
	        case SlumberContentProvider.CHECKLIST_EVENTS:
	            id = this._db.insert(SlumberContentProvider.CHECKLIST_EVENTS_TABLE, null, values);
	            break;
	        case SlumberContentProvider.SLEEP_DIARIES:
	            id = this._db.insert(SlumberContentProvider.SLEEP_DIARIES_TABLE, null, values);
	            break;
	        case SlumberContentProvider.SENSOR_READINGS:
	            id = this._db.insert(SlumberContentProvider.SENSOR_READINGS_TABLE, null, values);
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

	public static double scoreSleep(Cursor cursor, int multiplier) 
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_BED_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_BED_MINUTE)));
		
		long bedtime = c.getTimeInMillis();

		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_MINUTE)));

		long sleeptime = c.getTimeInMillis();

		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_WAKE_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_WAKE_MINUTE)));

		long waketime = c.getTimeInMillis();

		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_UP_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_UP_MINUTE)));
		
		long uptime = c.getTimeInMillis();
		 
		if (uptime < bedtime)
			uptime += SleepDiaryActivity.DAY_LENGTH;
		
		double total = uptime - bedtime;
		
		if (total == 0)
			return 0;

		if (sleeptime > waketime)
			waketime += SleepDiaryActivity.DAY_LENGTH;
		
		double sleep = waketime - sleeptime;
		
		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_EARLIER)) != 0)
			sleep -= (15 * 60 * 1000);

		sleep -= (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_DELAY)) * 60 * 1000);
		
		return (multiplier * sleep) / total;
	}

	public static Cursor fetchNormalizedSensorReadings(Context context, String sensor, long startTime) 
	{
		String[] columnNames = { SlumberContentProvider.READING_RECORDED, SlumberContentProvider.READING_VALUE, SlumberContentProvider.READING_MINIMUM, SlumberContentProvider.READING_MULTIPLIER };
		MatrixCursor retCursor = new MatrixCursor(columnNames);
		
		String where = SlumberContentProvider.READING_NAME + " = ? AND " + SlumberContentProvider.READING_RECORDED + " > ?";
		String[] args = { sensor, "" + startTime };
		
		Cursor c = context.getContentResolver().query(SlumberContentProvider.SENSOR_READINGS_URI, null, where, args, SlumberContentProvider.READING_RECORDED);
		
		double minValue = Double.MAX_VALUE;
		double maxValue = 0 - Double.MAX_VALUE;
		
		double[] readings = new double[c.getCount()];
		long[] recordeds = new long[readings.length];
		int index = 0;
		
		while (c.moveToNext())
		{
			double reading = c.getDouble(c.getColumnIndex(SlumberContentProvider.READING_VALUE));
			
			readings[index] = reading;
			recordeds[index] = c.getLong(c.getColumnIndex(SlumberContentProvider.READING_RECORDED));

			index += 1;
			
			if (reading > maxValue)
				maxValue = reading;
			
			if (reading < minValue)
				minValue = reading;
		}

		if (SlumberContentProvider.LIGHT_LEVEL.equalsIgnoreCase(sensor) && maxValue > SlumberContentProvider.MAX_LIGHT_LEVEL)
			maxValue = SlumberContentProvider.MAX_LIGHT_LEVEL;

		double spread = maxValue - minValue;
		
		c.close();
		

		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for (int i = 0; i < readings.length; i++)
		{
			double reading = readings[i];
			long recorded = recordeds[i];
			
			if (reading > maxValue)
				reading = maxValue;
			
			if (reading < minValue)
				reading = minValue;
			
			double normalized = (reading - minValue) / spread;
			
			Object[] values = {recorded, normalized, minValue, spread };
			
			retCursor.addRow(values);
			
			stats.addValue(normalized);
		}
		
		retCursor.moveToPosition(-1);

 		double stdDev = stats.getStandardDeviation();

		MatrixCursor cleanedCursor = new MatrixCursor(columnNames);
		double lastValue = 0 - Double.MAX_VALUE;

		while(retCursor.moveToNext())
		{
			double value = retCursor.getDouble(1);

			if (Math.abs(value - lastValue) > stdDev)
			{
				long timestamp = retCursor.getLong(0);

				Object[] values = { timestamp, value, minValue, spread };

				cleanedCursor.addRow(values);
				
				lastValue = value;
			}
		}

		retCursor.close();
		
		cleanedCursor.moveToPosition(-1);
		
		return cleanedCursor;
	}

    
    

	public static Object nameForKey(Context context, String key) 
	{
		if (SlumberContentProvider.LIGHT_LEVEL.equals(key))
			return context.getString(R.string.label_light);
		else if (SlumberContentProvider.AUDIO_MAGNITUDE.equals(key))
			return context.getString(R.string.label_audio_volume);
		else if (SlumberContentProvider.AUDIO_FREQUENCY.equals(key))
			return context.getString(R.string.label_audio_pitch);

		return key;
	}

	public static String keyForName(Context context, String name) 
	{
		if (context.getString(R.string.label_light).equals(name))
			return SlumberContentProvider.LIGHT_LEVEL;
		else if (context.getString(R.string.label_audio_volume).equals(name))
			return SlumberContentProvider.AUDIO_MAGNITUDE;
		else if (context.getString(R.string.label_audio_pitch).equals(name))
			return SlumberContentProvider.AUDIO_FREQUENCY;

		return name;
	}

	public static String summarize(Context context, Cursor cursor) 
	{
		StringBuffer sb = new StringBuffer();

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_BED_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_BED_MINUTE)));

		long bedtime = c.getTimeInMillis();

		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_MINUTE)));

		long sleeptime = c.getTimeInMillis();

		if (sleeptime < bedtime)
			sleeptime += SleepDiaryActivity.DAY_LENGTH;

		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_WAKE_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_WAKE_MINUTE)));

		long waketime = c.getTimeInMillis();

		if (waketime < sleeptime)
			waketime += SleepDiaryActivity.DAY_LENGTH;

		c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_UP_HOUR)));
		c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_UP_MINUTE)));
		
		long uptime = c.getTimeInMillis();
		
		if (uptime < waketime)
			uptime += SleepDiaryActivity.DAY_LENGTH;

		double total = uptime - bedtime;
		double sleep = waketime - sleeptime;
		
		java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
		
		sb.append(context.getString(R.string.summary_bedtime, (total / 3600000), timeFormat.format(new Date(bedtime)), timeFormat.format(new Date(uptime))));
		sb.append(System.getProperty("line.separator"));

		sb.append(context.getString(R.string.summary_sleeptime, (sleep / 3600000), timeFormat.format(new Date(sleeptime)), timeFormat.format(new Date(waketime))));
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		
		String delay = context.getString(R.string.value_immediately);
		
		switch(cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_DELAY)))
		{
			case 1:
				delay = context.getString(R.string.value_five_min);
				break;
			case 2:
				delay = context.getString(R.string.value_fifteen_min);
				break;
			case 3:
				delay = context.getString(R.string.value_thirty_min);
				break;
			case 4:
				delay = context.getString(R.string.value_one_hour);
				break;
			case 5:
				delay = context.getString(R.string.value_two_hour);
				break;
			case 6:
				delay = context.getString(R.string.value_four_hour);
				break;
			case 7:
				delay = context.getString(R.string.value_eight_hour);
				break;
			case 8:
				delay = context.getString(R.string.value_never);
				break;
		}

		sb.append(context.getString(R.string.summary_delay, delay));
		sb.append(System.getProperty("line.separator"));

		int wakeCount = cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_WAKE_COUNT));
		String wake = context.getString(R.string.value_multiple_times, wakeCount);
		
		switch(wakeCount)
		{
			case 0:
				wake = context.getString(R.string.label_never);
				break;
			case 1:
				wake = context.getString(R.string.value_once);
				break;
			case 11:
				wake = context.getString(R.string.value_greater_ten);
				break;
		}

		sb.append(context.getString(R.string.summary_wake_count, wake));
		sb.append(System.getProperty("line.separator"));

		int quality = cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_SLEEP_QUALITY));
		String qualityString = context.getString(R.string.value_terrible);
		
		switch(quality)
		{
			case 1:
				qualityString = context.getString(R.string.value_poor);
				break;
			case 2:
				qualityString = context.getString(R.string.value_adequate);
				break;
			case 3:
				qualityString = context.getString(R.string.value_good);
				break;
			case 4:
				qualityString = context.getString(R.string.value_excellent);
				break;
		}

		sb.append(context.getString(R.string.summary_quality, qualityString));
		sb.append(System.getProperty("line.separator"));
		
		int rested = cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_RESTED));
		String restedString = context.getString(R.string.label_not_rested);
		
		switch(rested)
		{
			case 1:
				restedString = context.getString(R.string.label_slightly_rested);
				break;
			case 2:
				restedString = context.getString(R.string.label_somewhat_rested);
				break;
			case 3:
				restedString = context.getString(R.string.label_well_rested);
				break;
			case 4:
				restedString = context.getString(R.string.label_very_rested);
				break;
		}

		sb.append(context.getString(R.string.summary_rested, restedString));
		
		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_NAP)) != 0)
		{
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));

			sb.append(context.getString(R.string.summary_napped));
		}
		
		if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.DIARY_EARLIER)) != 0)
		{
			sb.append(System.getProperty("line.separator"));
			sb.append(System.getProperty("line.separator"));

			sb.append(context.getString(R.string.summary_earlier));
		}
		return sb.toString();
		
		/*
	public static final String DIARY_RESTED = "rested";
*/
	}
}

