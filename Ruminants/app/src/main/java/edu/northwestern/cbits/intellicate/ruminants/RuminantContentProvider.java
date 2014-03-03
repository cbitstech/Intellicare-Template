package edu.northwestern.cbits.intellicate.ruminants;

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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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

import edu.northwestern.cbits.intellicare.logging.LogManager;

public class RuminantContentProvider extends ContentProvider
{
    private static final int SURVEY = 1;
    private static final int RECALL = 2;

    private static final String SURVEY_TABLE = "survey";
    private static final String RECALL_TABLE = "recall";

    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.ruminants";

    public static final Uri SURVEY_URI = Uri.parse("content://" + AUTHORITY + "/" + SURVEY_TABLE);
    public static final Uri RECALL_URI = Uri.parse("content://" + AUTHORITY + "/" + RECALL_TABLE);

    private static final int DATABASE_VERSION = 0;

    public static final String SURVEY_RUM = "ruminating";
    public static final String SURVEY_EMO = "pre_rum_emotion";
    public static final String SURVEY_RUM_DURATION = "rumination_duration";
    public static final String SURVEY_BEFORE_RUM = "before_rumination_response";
    public static final String SURVEY_RUM_STRATEGY = "rumination_strategy";
    public static final String SURVEY_STRATEGY_SUCCESS = "strategy_success";
    public static final String SURVEY_TIMESTAMP = "timestamp";


    public static final String RECALL_HELPFUL = "helpful";
    public static final String RECALL_BETTER = "better";
    public static final String RECALL_DIFFERENT_STRATEGY = "different_strategy_response";
    public static final String RECALL_TIMESTAMP = "timestamp";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    private static HashMap<String, String> _cachedHashes = new HashMap<String, String>();

    public RuminantContentProvider()
    {
    	super();
        this._matcher.addURI(AUTHORITY, SURVEY_TABLE, SURVEY);
        this._matcher.addURI(AUTHORITY, RECALL_TABLE, RECALL);

    }

    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();
        
        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "conductor.db", null, RuminantContentProvider.DATABASE_VERSION)
        {

            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_surveys_table)); /*sql statements write*/
	            db.execSQL(context.getString(R.string.db_create_recalls_table));

	
	            this.onUpgrade(db, 0, RuminantContentProvider.DATABASE_VERSION);
            }

            /*
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
		                		values.put(RuminantContentProvider.CHECKLIST_ITEM_NAME, tokens[0].trim());
		                		values.put(RuminantContentProvider.CHECKLIST_ITEM_CATEGORY, tokens[1].trim());
		                		
		                		db.insert(RuminantContentProvider.CHECKLIST_ITEMS_TABLE, null, values);
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
        }; */
        
        this._db  = helper.getWritableDatabase();

        return true;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
        /*
        switch(this._matcher.match(uri))
        {
	        case RuminantContentProvider.SURVEYS:
	            return this._db.query(RuminantContentProvider.SURVEYS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case RuminantContentProvider.RECALLS:
	            return this._db.query(RuminantContentProvider.RECALLS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
        
        return null;
	} */

        /*
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case RuminantContentProvider.SURVEYS:
	            return this._db.update(RuminantContentProvider.SURVEYS_TABLE, values, selection, selectionArgs);
	        case RuminantContentProvider.RECALLS:
	            return this._db.update(RuminantContentProvider.RECALLS_TABLE, values, selection, selectionArgs);
        }

		return 0;
	} */

        /*
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case RuminantContentProvider.SURVEYS:
	            return this._db.delete(RuminantContentProvider.SURVEYS_TABLE, selection, selectionArgs);
	        case RuminantContentProvider.RECALLS:
	            return this._db.delete(RuminantContentProvider.RECALLS_TABLE, selection, selectionArgs);
        }

        return 0;
	} */

        /*
	public String getType(Uri uri) 
	{
        switch(this._matcher.match(uri))
        {
	        case RuminantContentProvider.SURVEYS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".survey";
	        case RuminantContentProvider.RECALLS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".recall";
        }
        
        return null;
	} */

        /*

	public Uri insert(Uri uri, ContentValues values) 
	{
		long id = -1;
		
        switch(this._matcher.match(uri))
        {
	        case RuminantContentProvider.ALARMS:
	            id = this._db.insert(RuminantContentProvider.ALARMS_TABLE, null, values);
	            break;
	        case RuminantContentProvider.TIPS:
	            id = this._db.insert(RuminantContentProvider.ALARMS_TABLE, null, values);
	            break;
	        case RuminantContentProvider.NOTES:
	            id = this._db.insert(RuminantContentProvider.NOTES_TABLE, null, values);
	            break;
	        case RuminantContentProvider.CHECKLIST_ITEMS:
	            id = this._db.insert(RuminantContentProvider.CHECKLIST_ITEMS_TABLE, null, values);
	            break;
	        case RuminantContentProvider.CHECKLIST_EVENTS:
	            id = this._db.insert(RuminantContentProvider.CHECKLIST_EVENTS_TABLE, null, values);
	            break;
	        case RuminantContentProvider.SLEEP_DIARIES:
	            id = this._db.insert(RuminantContentProvider.SLEEP_DIARIES_TABLE, null, values);
	            break;
	        case RuminantContentProvider.SENSOR_READINGS:
	            id = this._db.insert(RuminantContentProvider.SENSOR_READINGS_TABLE, null, values);
	            break;
        }
        
        return Uri.withAppendedPath(uri, "" + id);
	}  */


    public static Uri fetchCachedUri(final Context context, final Uri uri, final Runnable next) 
    {
        if (uri == null || uri.getScheme() == null)
            return null;
        
        final String hashString = RuminantContentProvider.createHash(context, uri.toString());
        
        final File cacheDir = RuminantContentProvider.fetchCacheDir(context);
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
                    long freeSpace = RuminantContentProvider.cacheSpace(context);
                    
                    if (freeSpace < 4096 * 4096)
                    {
                        RuminantContentProvider.cleanCache(context);
                    }
                    
                    freeSpace = RuminantContentProvider.cacheSpace(context);

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
        
        String hash = RuminantContentProvider._cachedHashes.get(string);
        
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
        
        if (RuminantContentProvider._cachedHashes.size() > 256)
            RuminantContentProvider._cachedHashes.clear();
        
        RuminantContentProvider._cachedHashes.put(string, hash);

        return hash;
    }

}

