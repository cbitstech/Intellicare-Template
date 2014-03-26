package edu.northwestern.cbits.intellicare.avast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.android.gms.maps.model.LatLng;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Venue;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class AvastVenuesManager implements LocationListener 
{
	public static final String LAST_TITLE = "pref_last_title";

	private static AvastVenuesManager _instance = null;
	private Context _context = null;
	private boolean _setup = false;

	public AvastVenuesManager(Context context) 
	{
		this._context = context.getApplicationContext();
	}

	public static AvastVenuesManager getInstance(Context context)
	{
		if (AvastVenuesManager._instance == null)
		{
			AvastVenuesManager._instance = new AvastVenuesManager(context.getApplicationContext());
			AvastVenuesManager._instance.setup();
		}
		
		return AvastVenuesManager._instance;
	}

	public void setup() 
	{
		if (this._setup  == false)
		{
			LocationManager locationManager = (LocationManager) this._context.getSystemService(Context.LOCATION_SERVICE);
			
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 800, this);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 800, this);
			
			this._setup = true;
		}
	}

	public void onLocationChanged(Location location) 
	{
		location.setAltitude(0);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		float minDistance = Float.parseFloat(prefs.getString(AvastContentProvider.PREF_DISTANCE, AvastContentProvider.PREF_DISTANCE_DEFAULT));
		
		ArrayList<Venue> nearby = new ArrayList<Venue>();

		Cursor c = this._context.getContentResolver().query(AvastContentProvider.VENUE_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			double latitude = c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LATITUDE));
			double longitude = c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LONGITUDE));
			
			Location venue = new Location(this.getClass().getCanonicalName());
			venue.setLatitude(latitude);
			venue.setLongitude(longitude);
			venue.setAltitude(0);
			
			if (venue.distanceTo(location) < minDistance)
			{
				Venue v = new Venue();
				
	        	v.foursquareId = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_FOURSQUARE_ID));
	        	v.name = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_NAME));
	        	v.address = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_ADDRESS));
	        	v.typeId = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_CATEGORY_ID));
	        	v.location = new LatLng(c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LATITUDE)), c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LONGITUDE)));
				v.distance = venue.distanceTo(location);
				
				nearby.add(v);
			}
		}
		
		c.close();
		
		Collections.sort(nearby, new Comparator<Venue>()
		{
			public int compare(Venue one, Venue two) 
			{
				if (one.distance < two.distance)
					return -1;
				else if (one.distance > two.distance)
					return 1;
				
				return 0;
			}
		});
		
		if (nearby.size() > 0)
		{
			String title = this._context.getString(R.string.title_nearby_single);
			String message = this._context.getString(R.string.message_nearby_single);
			
			if (nearby.size() > 1)
			{
				title = this._context.getString(R.string.title_nearby, nearby.size());
				message = this._context.getString(R.string.message_nearby, nearby.size());
			}
			
			String lastTitle = prefs.getString(AvastVenuesManager.LAST_TITLE, "");
			
			if (title.equalsIgnoreCase(lastTitle) == false)
			{
				Intent intent = new Intent(this._context, MainActivity.class);
				PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
	
				StatusNotificationManager.getInstance(this._context).notifyBigText(97531, R.drawable.ic_action_add, title, message, pi, MainActivity.URI);
				
				Editor e = prefs.edit();
				e.putString(AvastVenuesManager.LAST_TITLE, title);
				e.commit();
			}
		}
	}

	public void onProviderDisabled(String provider) 
	{

	}

	public void onProviderEnabled(String provider) 
	{

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) 
	{

	}
}
