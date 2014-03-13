package edu.northwestern.cbits.intellicare.avast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;

public class AvastContentProvider extends ContentProvider 
{
	public static class Venue
	{
		String name = null;
		LatLng location = null;
		String foursquareId = null;
	}

	public static class Category
	{
		String name = null;
		String id = null;
		String parentId = null;
	}

	protected static final String SAVED_CATEGORIES = "cache_saved_categories";

	protected static ArrayList<Venue> _lastVenues;
	protected static ArrayList<Category> _lastCategories;

    private static final int VENUE_TYPES = 1;
    private static final int LOCATIONS = 2;
    private static final int VENUES = 3;
    private static final int CHECKINS = 4;

    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.avast";

    private static final String VENUE_TYPE_TABLE = "venue_types";
    private static final String LOCATION_TABLE = "locations";
    private static final String VENUE_TABLE = "venues";
    private static final String CHECKIN_TABLE = "checkins";

    public static final Uri VENUE_TYPE_URI = Uri.parse("content://" + AUTHORITY + "/" + VENUE_TYPE_TABLE);
    public static final Uri LOCATION_URI = Uri.parse("content://" + AUTHORITY + "/" + LOCATION_TABLE);
    public static final Uri VENUE_URI = Uri.parse("content://" + AUTHORITY + "/" + VENUE_TABLE);
    public static final Uri CHECKIN_URI = Uri.parse("content://" + AUTHORITY + "/" + CHECKIN_TABLE);

    private static final int DATABASE_VERSION = 1;

    public static final String LOCATION_NAME = "name";
    public static final String LOCATION_LATITUDE = "latitude";
    public static final String LOCATION_LONGITUDE = "longitude";
    public static final String LOCATION_RADIUS = "radius";
    public static final String LOCATION_DURATION = "duration";
    public static final String LOCATION_ENABLED = "enabled";
	public static final String LOCATION_ID = "_id";

	protected static final String VENUE_TYPE_FOURSQUARE_ID = "foursquare_id";
	protected static final String VENUE_TYPE_ENABLED = "enabled";
	protected static final String VENUE_TYPE_NAME = "name";
	protected static final String VENUE_TYPE_FOURSQUARE_PARENT_ID = "parent_id";
	protected static final String VENUE_TYPE_ID = "_id";

	public static final double DEFAULT_RADIUS = 200;
	public static final double DEFAULT_INITIAL_DURATION = 0;


    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase _db = null;
    
    public AvastContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, VENUE_TYPE_TABLE, VENUE_TYPES);
        this._matcher.addURI(AUTHORITY, LOCATION_TABLE, LOCATIONS);
        this._matcher.addURI(AUTHORITY, VENUE_TABLE, VENUES);
        this._matcher.addURI(AUTHORITY, CHECKIN_TABLE, CHECKINS);
    }
    
    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "avast.db", null, AvastContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_venue_types_table));
	            db.execSQL(context.getString(R.string.db_create_locations_table));
	            db.execSQL(context.getString(R.string.db_create_venues_table));
	            db.execSQL(context.getString(R.string.db_create_checkins_table));

	            this.onUpgrade(db, 0, AvastContentProvider.DATABASE_VERSION);
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
	        case AvastContentProvider.VENUE_TYPES:
	            return this._db.delete(AvastContentProvider.VENUE_TYPE_TABLE, where, whereArgs);
	        case AvastContentProvider.LOCATIONS:
	            return this._db.delete(AvastContentProvider.LOCATION_TABLE, where, whereArgs);
	        case AvastContentProvider.CHECKINS:
	            return this._db.delete(AvastContentProvider.CHECKIN_TABLE, where, whereArgs);
	        case AvastContentProvider.VENUES:
	            return this._db.delete(AvastContentProvider.VENUE_TABLE, where, whereArgs);
        }
		
		return 0;
	}

	@Override
	public String getType(Uri uri) 
	{
        switch(this._matcher.match(uri))
        {
	        case AvastContentProvider.VENUE_TYPES:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".venue_type";
	        case AvastContentProvider.LOCATIONS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".location";
	        case AvastContentProvider.CHECKINS:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".checkin";
	        case AvastContentProvider.VENUES:
	        	return "vnd.android.cursor.dir/" + AUTHORITY + ".venue";
        }
        
        return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		long newId = -1;
		
        switch(this._matcher.match(uri))
        {
	        case AvastContentProvider.VENUE_TYPES:
	            newId = this._db.insert(AvastContentProvider.VENUE_TYPE_TABLE, null, values);

	            break;
	        case AvastContentProvider.LOCATIONS:
	            newId = this._db.insert(AvastContentProvider.LOCATION_TABLE, null, values);

	            break;
	        case AvastContentProvider.CHECKINS:
	            newId = this._db.insert(AvastContentProvider.CHECKIN_TABLE, null, values);

	            break;
	        case AvastContentProvider.VENUES:
	            newId = this._db.insert(AvastContentProvider.VENUE_TABLE, null, values);

	            break;
        }
        
        if (newId != -1)
            return Uri.withAppendedPath(uri, "" + newId);
		
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
        switch(this._matcher.match(uri))
        {
	        case AvastContentProvider.VENUE_TYPES:
	            return this._db.query(AvastContentProvider.VENUE_TYPE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case AvastContentProvider.LOCATIONS:
	            return this._db.query(AvastContentProvider.LOCATION_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case AvastContentProvider.CHECKINS:
	            return this._db.query(AvastContentProvider.CHECKIN_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	        case AvastContentProvider.VENUES:
	            return this._db.query(AvastContentProvider.VENUE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }
		
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case AvastContentProvider.VENUE_TYPES:
	            return this._db.update(AvastContentProvider.VENUE_TYPE_TABLE, values, where, whereArgs);
	        case AvastContentProvider.LOCATIONS:
	            return this._db.update(AvastContentProvider.LOCATION_TABLE, values, where, whereArgs);
	        case AvastContentProvider.CHECKINS:
	            return this._db.update(AvastContentProvider.CHECKIN_TABLE, values, where, whereArgs);
	        case AvastContentProvider.VENUES:
	            return this._db.update(AvastContentProvider.VENUE_TABLE, values, where, whereArgs);
        }
		
		return 0;
	}

	
	public static void fetchLocations(final Context context, final LatLng target, final String category, final Runnable callback) 
	{
		Runnable r = new Runnable()
		{
			public void run() 
			{
				String query = context.getString(R.string.query_venue, context.getString(R.string.fs_client_id), context.getString(R.string.fs_client_secret), target.latitude, target.longitude, category);

				try 
				{
					URL url = new URL(query);

					URLConnection con = url.openConnection();
					
					InputStream in = con.getInputStream();
					
					String encoding = con.getContentEncoding();
					
					encoding = encoding == null ? "UTF-8" : encoding;

					String body = IOUtils.toString(in, encoding);

					JSONObject json = new JSONObject(body);
					JSONObject response = json.getJSONObject("response");
					JSONArray venues = response.getJSONArray("venues");
					
					AvastContentProvider._lastVenues = new ArrayList<Venue>();
					
					for (int i = 0; i < venues.length(); i++)
					{
						JSONObject venue = venues.getJSONObject(i);
						
						Venue v = new Venue();
						v.name = venue.getString("name");
						v.foursquareId = venue.getString("id");
						
						JSONObject location = venue.getJSONObject("location");
						v.location = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
						
						AvastContentProvider._lastVenues.add(v);
					}
					
					if (context instanceof Activity)
					{
						Activity a = (Activity) context;
						
						a.runOnUiThread(callback);
					}
					else
						callback.run();
				} 
				catch (MalformedURLException e) 
				{
					LogManager.getInstance(context).logException(e);
				} 
				catch (IOException e) 
				{
					LogManager.getInstance(context).logException(e);
				} 
				catch (JSONException e) 
				{
					LogManager.getInstance(context).logException(e);
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}

	public static List<Venue> getLastLocations() 
	{
		return AvastContentProvider._lastVenues;
	}

	public static void fetchCategories(final Context context, final Runnable callback) 
	{
		if (AvastContentProvider._lastCategories != null)
			callback.run();
		
		Runnable r = new Runnable()
		{
			public void run() 
			{
				String query = context.getString(R.string.query_categories, context.getString(R.string.fs_client_id), context.getString(R.string.fs_client_secret));

				try 
				{
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					
					String savedCategories = prefs.getString(AvastContentProvider.SAVED_CATEGORIES, null);
					
					if (savedCategories == null)
					{
						URL url = new URL(query);
	
						URLConnection con = url.openConnection();
						
						InputStream in = con.getInputStream();
						
						String encoding = con.getContentEncoding();
						
						encoding = encoding == null ? "UTF-8" : encoding;
	
						savedCategories = IOUtils.toString(in, encoding);
					}

					JSONObject json = new JSONObject(savedCategories);
					JSONObject response = json.getJSONObject("response");
					JSONArray categories = response.getJSONArray("categories");
					
					AvastContentProvider._lastCategories = new ArrayList<Category>();

					for (int i = 0; i < categories.length(); i++)
					{
						JSONObject category = categories.getJSONObject(i);
						
						List<Category> foundCategories = AvastContentProvider.parseCategories(category, null);
						
						AvastContentProvider._lastCategories.addAll(foundCategories);
					}
					
					Editor e = prefs.edit();
					e.putString(AvastContentProvider.SAVED_CATEGORIES, savedCategories);
					e.commit();
					
					if (context instanceof Activity)
					{
						Activity a = (Activity) context;
						
						a.runOnUiThread(callback);
					}
					else
						callback.run();
				} 
				catch (MalformedURLException e) 
				{
					LogManager.getInstance(context).logException(e);
				} 
				catch (IOException e) 
				{
					LogManager.getInstance(context).logException(e);
				} 
				catch (JSONException e) 
				{
					LogManager.getInstance(context).logException(e);
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}

	protected static List<Category> parseCategories(JSONObject category, String parent) throws JSONException 
	{
		ArrayList<Category> categoryList = new ArrayList<Category>();
		
		Category c = new Category();
		
		if (parent != null)
			c.name = parent + " » " + category.getString("name");
		else
			c.name = category.getString("name");
		
		c.id = category.getString("id");
		
		categoryList.add(c);
		
		if (category.has("categories"))
		{
			JSONArray subcategories = category.getJSONArray("categories");
			
			for (int i = 0; i < subcategories.length(); i++)
			{
				JSONObject subcategory = subcategories.getJSONObject(i);
				
				List<Category> subs = AvastContentProvider.parseCategories(subcategory, c.name);
				
				categoryList.addAll(subs);
			}
		}
		
		return categoryList;
	}

	public static List<Category> getLastCategories()
	{
		return AvastContentProvider._lastCategories;
	}

	public static Uri uriForId(Context context, String id) 
	{
		Uri u = Uri.parse(context.getString(R.string.foursquare_uri_root));
		
		return Uri.withAppendedPath(u, id);
	}
}
