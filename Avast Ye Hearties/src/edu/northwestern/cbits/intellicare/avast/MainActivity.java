package edu.northwestern.cbits.intellicare.avast;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Venue;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends ActionBarActivity 
{
	private static final String LAST_LATITUDE = "last_known_latitude";
	private static final String LAST_LONGITUDE = "last_known_longitude";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_main);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		double latitude = (double) prefs.getFloat(MainActivity.LAST_LATITUDE, Float.MAX_VALUE);
		double longitude = (double) prefs.getFloat(MainActivity.LAST_LONGITUDE, Float.MAX_VALUE);
		
		if (latitude == Float.MAX_VALUE || longitude == Float.MAX_VALUE)
		{
			LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			Location last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			if (last != null)
			{
				latitude = last.getLatitude();
				longitude = last.getLongitude();
			}
		}		
		
		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		
		GoogleMap map = fragment.getMap();
		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.redrawMap();
	}
	
	private void redrawMap()
	{
		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		
		final GoogleMap map = fragment.getMap();

		AvastContentProvider.fetchLocations(this, map.getCameraPosition().target, "4bf58dd8d48988d17f941735", new Runnable()
		{
			public void run() 
			{
				map.clear();

				for (Venue venue : AvastContentProvider.getLastLocations())
				{
					MarkerOptions marker = new MarkerOptions();
					marker.position(venue.location);
					marker.title(venue.name);
					
					Log.e("AYH", "MARKER: " + marker);
					
					map.addMarker(marker);
				}
			}
		}); // Movie Theatre
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
