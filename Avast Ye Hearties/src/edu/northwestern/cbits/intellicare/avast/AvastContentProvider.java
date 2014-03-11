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
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

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

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void fetchLocations(final Context context, final LatLng target, final String category, final Runnable callback) 
	{
		Runnable r = new Runnable()
		{
			public void run() 
			{
				String query = context.getString(R.string.query_venue, context.getString(R.string.fs_client_id), context.getString(R.string.fs_client_secret), target.latitude, target.longitude, category);

				Log.e("AYH", "FETCH LOCATIONS FROM " + query);
				
				try 
				{
					URL url = new URL(query);

					URLConnection con = url.openConnection();
					
					InputStream in = con.getInputStream();
					
					String encoding = con.getContentEncoding();
					
					encoding = encoding == null ? "UTF-8" : encoding;

					String body = IOUtils.toString(in, encoding);

					Log.e("AYH", "FETCHED LOCATIONS FROM " + query);

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
}
