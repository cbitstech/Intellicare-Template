package edu.northwestern.cbits.intellicare.conductor;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLException;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.StatFs;

import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ConductorContentProvider extends ContentProvider
{
    private static final int APPS = 1;
    private static final int MESSAGES = 2;
    private static final int INSTALLED_APPS = 3;
    
	private static final String INSTALLED_APPS_TABLE = "installed_apps";

    private static final String APPS_TABLE = "apps";
    private static final String MESSAGES_TABLE = "messages";
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.conductor";

    public static final Uri INSTALLED_APPS_URI = Uri.parse("content://" + AUTHORITY + "/" + INSTALLED_APPS_TABLE);
    public static final Uri APPS_URI = Uri.parse("content://" + AUTHORITY + "/" + APPS_TABLE);
    public static final Uri MESSAGES_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGES_TABLE);

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase _db = null;

    private static final int DATABASE_VERSION = 5;

    private static HashMap<String, String> _cachedHashes = new HashMap<String, String>();

    private static String createHash(Context context, String string)
    {
        if (string == null)
            return null;
        
        String hash = ConductorContentProvider._cachedHashes.get(string);
        
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
        
        if (ConductorContentProvider._cachedHashes.size() > 256)
            ConductorContentProvider._cachedHashes.clear();
        
        ConductorContentProvider._cachedHashes.put(string, hash);

        return hash;
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

    public static Uri fetchCachedUri(final Context context, final Uri uri, final Runnable next) 
    {
        if (uri == null || uri.getScheme() == null)
            return null;
        
        final String hashString = ConductorContentProvider.createHash(context, uri.toString());
        
        final File cacheDir = ConductorContentProvider.fetchCacheDir(context);
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
                    long freeSpace = ConductorContentProvider.cacheSpace(context);
                    
                    if (freeSpace < 4096 * 4096)
                    {
                        ConductorContentProvider.cleanCache(context);
                    }
                    
                    freeSpace = ConductorContentProvider.cacheSpace(context);

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

    public ConductorContentProvider()
    {
    	super();
    	
    	this._matcher.addURI(AUTHORITY, APPS_TABLE, APPS);
    	this._matcher.addURI(AUTHORITY, MESSAGES_TABLE, MESSAGES);
    	this._matcher.addURI(AUTHORITY, INSTALLED_APPS_TABLE, INSTALLED_APPS);
    }

    public boolean onCreate()
    {
    	final Context context = this.getContext().getApplicationContext();
    	
    	SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "conductor.db", null, ConductorContentProvider.DATABASE_VERSION)
    	{
			public void onCreate(SQLiteDatabase db) 
			{
		        db.execSQL(context.getString(R.string.db_create_apps_table));
		        db.execSQL(context.getString(R.string.db_create_messages_table));

		        this.onUpgrade(db, 0, ConductorContentProvider.DATABASE_VERSION);
			}

			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
			{
                switch (oldVersion)
                {
                    case 0:
                    	
                    case 1:
                    	/* ContentValues message = new ContentValues();
                    	message.put("package", "edu.northwestern.cbits.intellicare.dailyfeats");
                    	message.put("name", "Daily Feats");
                    	message.put("message", "This is a test message. (Weight 1)");
                    	message.put("date", System.currentTimeMillis());
                    	message.put("weight", 1);
                    	
                    	db.insert(ConductorContentProvider.MESSAGES_TABLE, null, message);
                    	
                    	message = new ContentValues();
                    	message.put("package", "edu.northwestern.cbits.intellicare.dailyfeats");
                    	message.put("name", "Daily Feats");
                    	message.put("message", "This is a test message. (Weight 2)");
                    	message.put("date", System.currentTimeMillis() + 10);
                    	message.put("weight", 2);
                    	
                    	db.insert(ConductorContentProvider.MESSAGES_TABLE, null, message);
                    	
                    	message = new ContentValues();
                    	message.put("package", "edu.northwestern.cbits.intellicare.dailyfeats");
                    	message.put("name", "Daily Feats");
                    	message.put("message", "This is a test message. (Weight 3)");
                    	message.put("date", System.currentTimeMillis() + 20);
                    	message.put("weight", 3);
                    	
                    	db.insert(ConductorContentProvider.MESSAGES_TABLE, null, message);

                    	message = new ContentValues();
                    	message.put("package", "edu.northwestern.cbits.intellicare.dailyfeats");
                    	message.put("name", "Daily Feats");
                    	message.put("message", "This is a responded message.");
                    	message.put("date", System.currentTimeMillis() - 10);
                    	message.put("responded", 1);
                    	
                    	db.insert(ConductorContentProvider.MESSAGES_TABLE, null, message);

                    	message = new ContentValues();
                    	message.put("package", "edu.northwestern.cbits.intellicare.dailyfeats");
                    	message.put("name", "Daily Feats");
                    	message.put("message", "This is another responded message.");
                    	message.put("date", System.currentTimeMillis() - 20);
                    	message.put("responded", 1);
                    	message.put("weight", 4);
                    	
                    	db.insert(ConductorContentProvider.MESSAGES_TABLE, null, message); */
                    case 2:
                    	db.execSQL(context.getString(R.string.db_update_apps_add_icon));
                    	db.execSQL(context.getString(R.string.db_update_apps_add_version));
                    	db.execSQL(context.getString(R.string.db_update_apps_add_changelog));
                    case 3:
                    	db.execSQL(context.getString(R.string.db_update_apps_add_synopsis));
                    case 4:
                    	db.execSQL(context.getString(R.string.db_update_messages_add_uri));
                    default:
                    	break;
                }
			}
    	};
    	
        this._db  = helper.getWritableDatabase();

        return true;
    }

	public int delete(Uri uri, String selection, String[] args) 
	{
        switch(this._matcher.match(uri))
        {
            case ConductorContentProvider.APPS:
                return this._db.delete(APPS_TABLE, selection, args);
            case ConductorContentProvider.MESSAGES:
                return this._db.delete(MESSAGES_TABLE, selection, args);
        }

        return 0;
	}

	public String getType(Uri uri) 
	{
        switch(this._matcher.match(uri))
        {
            case ConductorContentProvider.APPS:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".app";
            case ConductorContentProvider.MESSAGES:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".message";
        }
        
        return null;
	}

	public Uri insert(Uri uri, ContentValues values)
	{
		long insertedId = 0;
		
        switch(this._matcher.match(uri))
        {
            case ConductorContentProvider.APPS:
                insertedId = this._db.insert(APPS_TABLE, null, values);
                
                return Uri.withAppendedPath(APPS_URI, "" + insertedId);

            case ConductorContentProvider.MESSAGES:
                insertedId = this._db.insert(MESSAGES_TABLE, null, values);

                return Uri.withAppendedPath(MESSAGES_URI, "" + insertedId);
        }
        
        return null;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
        switch(this._matcher.match(uri))
        {
	        case ConductorContentProvider.APPS:
	            return this._db.query(ConductorContentProvider.APPS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case ConductorContentProvider.MESSAGES:
	            return this._db.query(ConductorContentProvider.MESSAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case ConductorContentProvider.INSTALLED_APPS:
	        	return this.getInstalledApps();
        }

        return null;
	}

	private Cursor getInstalledApps() 
	{
		String[] columns = { "package", "name", "icon" };
		
		HashMap<String, String> names = new HashMap<String, String>();
		HashMap<String, String> icons = new HashMap<String, String>();
		
		MatrixCursor cursor = new MatrixCursor(columns);
		
		ArrayList<PackageInfo> packages = new ArrayList<PackageInfo>();

		PackageManager packageManager = this.getContext().getPackageManager();
		
		Cursor c = this.query(ConductorContentProvider.APPS_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			String packageName = c.getString(c.getColumnIndex("package"));

			try 
			{
				PackageInfo info = packageManager.getPackageInfo(packageName, 0);

				packages.add(info);
				
				names.put(packageName, c.getString(c.getColumnIndex("name")));
				icons.put(packageName, c.getString(c.getColumnIndex("icon")));

			}
			catch (NameNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		
		Collections.sort(packages, new Comparator<PackageInfo>()
		{
			public int compare(PackageInfo one, PackageInfo two) 
			{
				long delta = two.lastUpdateTime - one.lastUpdateTime;
				
				if (delta > 0)
					return 1;
				else if (delta < 0)
					return -1;
				
				return 0;
			}
		});

		for (PackageInfo pkg : packages)
		{
			Object[] values = { pkg.packageName, names.get(pkg.packageName), icons.get(pkg.packageName) };
			
			cursor.addRow(values);
		}
		
		return cursor;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case ConductorContentProvider.APPS:
	            return this._db.update(ConductorContentProvider.APPS_TABLE, values, selection, selectionArgs);
	        case ConductorContentProvider.MESSAGES:
	            return this._db.update(ConductorContentProvider.MESSAGES_TABLE, values, selection, selectionArgs);
         }

        return 0;
	}

	public static Uri iconUri(final Context context, String packageName, final Runnable next) 
	{
        String selection = "package = ?";
        String[] args = { packageName };
        
        Cursor cursor = context.getContentResolver().query(ConductorContentProvider.APPS_URI, null, selection, args, null);
        
        if (cursor.moveToNext())
        {
            final Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex("icon")));

            Uri cachedUri = ConductorContentProvider.fetchCachedUri(context, imageUri, new Runnable()
            {
				public void run() 
				{
					if (next != null)
					{
						if (context instanceof Activity)
						{
							Activity activity = (Activity) context;
							
							activity.runOnUiThread(next);
						}
					}
				}		                
            });
            
            return cachedUri;
        }
        
        return null;
	}
}
