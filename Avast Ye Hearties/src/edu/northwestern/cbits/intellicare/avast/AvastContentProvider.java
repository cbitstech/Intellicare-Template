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
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class AvastContentProvider extends ContentProvider 
{
	public static class Venue
	{
		String name = null;
		LatLng location = null;
		String foursquareId = null;
	}

	protected static ArrayList<Venue> _lastVenues;

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
				String query = "https://api.foursquare.com/v2/venues/search?client_id=" + context.getString(R.string.fs_client_id) + 
							 "&client_secret=" + context.getString(R.string.fs_client_secret) + "&v=20130815&ll=" + target.latitude + "," + target.longitude +
							 "&categoryId=" + category;
								
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
					
					Log.e("AYH", "JSON: " + json.toString(2));
					
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
}
