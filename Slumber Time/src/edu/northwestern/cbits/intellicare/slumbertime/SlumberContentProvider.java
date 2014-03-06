package edu.northwestern.cbits.intellicare.slumbertime;

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
import android.os.StatFs;
import edu.northwestern.cbits.intellicare.logging.LogManager;

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

    private static final int DATABASE_VERSION = 7;

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

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    private static HashMap<String, String> _cachedHashes = new HashMap<String, String>();

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
		
		while (c.moveToNext())
		{
			double reading = c.getDouble(c.getColumnIndex(SlumberContentProvider.READING_VALUE));
			
			if (reading > maxValue)
				maxValue = reading;
			
			if (reading < minValue)
				minValue = reading;
		}

		if (SlumberContentProvider.LIGHT_LEVEL.equalsIgnoreCase(sensor) && maxValue > SlumberContentProvider.MAX_LIGHT_LEVEL)
			maxValue = SlumberContentProvider.MAX_LIGHT_LEVEL;

		double spread = maxValue - minValue;

		c.moveToPosition(-1);
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		while (c.moveToNext())
		{
			double reading = c.getDouble(c.getColumnIndex(SlumberContentProvider.READING_VALUE));
			
			if (reading > maxValue)
				reading = maxValue;
			
			if (reading < minValue)
				reading = minValue;
			
			double normalized = (reading - minValue) / spread;
			
			Object[] values = {c.getLong(c.getColumnIndex(SlumberContentProvider.READING_RECORDED)), normalized, minValue, spread };
			
			retCursor.addRow(values);
			
			stats.addValue(normalized);
		}
		
		c.close();
		
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

    public static Uri fetchCachedUri(final Context context, final Uri uri, final Runnable next) 
    {
        if (uri == null || uri.getScheme() == null)
            return null;
        
        final String hashString = SlumberContentProvider.createHash(context, uri.toString());
        
        final File cacheDir = SlumberContentProvider.fetchCacheDir(context);
        final File cachedFile = new File(cacheDir, hashString);
        
        if (cachedFile.exists() && cachedFile.length() > 0)
        {
            cachedFile.setLastModified(System.currentTimeMillis());
            return Uri.fromFile(cachedFile);
        }
        
        Runnable r = new Runnable()
        {
            public void run() 
            {
                File tempFile = null;
                
                try 
                {
                    long freeSpace = SlumberContentProvider.cacheSpace(context);
                    
                    if (freeSpace < 4096 * 4096)
                    {
                        SlumberContentProvider.cleanCache(context);
                    }
                    
                    freeSpace = SlumberContentProvider.cacheSpace(context);

                    if (freeSpace > 2048 * 2048)
                    {
                        URL u = new URL(uri.toString());
                        
                        URLConnection conn = u.openConnection();
                        InputStream in = conn.getInputStream();
                        
                        tempFile = File.createTempFile(hashString, "tmp", cacheDir);

                        FileOutputStream out = new FileOutputStream(tempFile);

                        byte[] buffer = new byte[8192];
                        int read = 0;
                        
                        while ((read = in.read(buffer, 0, buffer.length)) != -1)
                        {
                            out.write(buffer, 0, read);
                        }
                        
                        out.close();
                        
                        tempFile.renameTo(cachedFile);
                    }
                } 
                catch (SocketTimeoutException e)
                {

                }
                catch (SocketException e)
                {

                }
                catch (SSLException e)
                {

                }
                catch (UnknownHostException e)
                {

                }
                catch (EOFException e)
                {

                }
                catch (FileNotFoundException e) 
                {
                	LogManager.getInstance(context).logException(e);
                }
                catch (IOException e) 
                {
                	LogManager.getInstance(context).logException(e);
                }
                finally
                {
                    if (tempFile != null && tempFile.exists())
                        tempFile.delete();
                }

                if (next != null)
                	next.run();
            }
        };
        
        Thread t = new Thread(r);
        t.start();

        return null;
    }
 
    
    private static File fetchCacheDir(Context context)
    {
    	File cache = context.getCacheDir();
    
        if (cache.exists() == false)
            cache.mkdirs();
            
        return cache;
    }

    @SuppressWarnings("deprecation")
	public static synchronized long cacheSpace(Context context)
    {
        File cache = context.getCacheDir();
        
        if (cache.exists() == false)
        	cache.mkdirs();
        
        StatFs stat = new StatFs(cache.getAbsolutePath());
        
        return ((long) stat.getBlockSize()) * ((long) stat.getAvailableBlocks());
    }

    public static synchronized void cleanCache(Context context) 
    {
        File cache = context.getCacheDir();
        
        int cacheSize = 16 * 1024 * 1024;

        if (cache.exists() == false)
            cache.mkdirs();
        
        synchronized(context.getApplicationContext())
        {
            List<File> files = new ArrayList<File>();
            
            for (File f : cache.listFiles())
            {
                files.add(f);
            }

            try
            {
                Collections.sort(files, new Comparator<File>()
                {
                    public int compare(File first, File second) 
                    {
                        if (first.lastModified() < second.lastModified())
                            return 1;
                        else if (first.lastModified() > second.lastModified())
                            return -1;

                        return first.getName().compareTo(second.getName());
                    }
                });
            }
            catch (IllegalArgumentException e)
            {
            	LogManager.getInstance(context).logException(e);
            }
            
            List<File> toRemove = new ArrayList<File>();
            
            long totalCached = 0;
            
            for (File f : files)
            {
                if (totalCached < cacheSize)
                    totalCached += f.length();
                else
                    toRemove.add(f);
            }
            
            for (File f : toRemove)
            {
                f.delete();
            }
        }
    }
    
    private static String createHash(Context context, String string)
    {
        if (string == null)
            return null;
        
        String hash = SlumberContentProvider._cachedHashes.get(string);
        
        if (hash != null)
            return hash;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(string.getBytes("UTF-8"));

            hash = (new BigInteger(1, digest)).toString(16);

            while (hash.length() < 32)
            {
                    hash = "0" + hash;
            }
        }
        catch (NoSuchAlgorithmException e)
        {
        	LogManager.getInstance(context).logException(e);
        }
        catch (UnsupportedEncodingException e)
        {
        	LogManager.getInstance(context).logException(e);
        }
        
        if (SlumberContentProvider._cachedHashes.size() > 256)
            SlumberContentProvider._cachedHashes.clear();
        
        SlumberContentProvider._cachedHashes.put(string, hash);

        return hash;
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
}

