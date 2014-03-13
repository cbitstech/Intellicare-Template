package edu.northwestern.cbits.intellicare.avast;

import java.util.HashMap;
import java.util.HashSet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Venue;

public class MainActivity extends ConsentedActivity 
{
	private static final String LAST_LATITUDE = "last_known_latitude";
	private static final String LAST_LONGITUDE = "last_known_longitude";
	
	private HashSet<Marker> _venueMarkers = new HashSet<Marker>();
	private HashMap<Marker, String> _markerIds = new HashMap<Marker, String>();
	private HashMap<Marker, Long> _placeIds = new HashMap<Marker, Long>();
	
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
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
		
		this.getSupportActionBar().setSubtitle(R.string.foursquare_attribution);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();
		
		final MainActivity me = this;

		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = fragment.getMap();

		Cursor c = this.getContentResolver().query(AvastContentProvider.LOCATION_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			MarkerOptions marker = new MarkerOptions();
			marker.title(c.getString(c.getColumnIndex(AvastContentProvider.LOCATION_NAME)));
			marker.anchor(0.5f, 0.5f);

			LatLng position = new LatLng(c.getDouble(c.getColumnIndex(AvastContentProvider.LOCATION_LATITUDE)), c.getDouble(c.getColumnIndex(AvastContentProvider.LOCATION_LONGITUDE)));
			
			marker.position(position);
			marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mark_red));
	
			this._placeIds.put(map.addMarker(marker), c.getLong(c.getColumnIndex(AvastContentProvider.LOCATION_ID)));
		}
		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
		{
			public void onInfoWindowClick(final Marker marker) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(marker.getTitle());
				builder.setItems(R.array.list_venue_options, new OnClickListener()
				{

					public void onClick(DialogInterface arg0, int which) 
					{
						switch (which)
						{
							case 0:
								String id = me._markerIds.get(marker);
								
								if (id != null)
								{
									Uri u = AvastContentProvider.uriForId(me, id);
									
									if (u != null)
									{
										Intent intent = new Intent(Intent.ACTION_VIEW);
										intent.setData(u);
										
										me.startActivity(intent);
									}
								}
								
								Long placeId = me._placeIds.get(marker);
								
								if (placeId != null)
									me.selectLocation(placeId.longValue());
								
								break;
							case 1:
								Toast.makeText(me, "ToDo: CHEck iN", Toast.LENGTH_LONG).show();
								
								break;
							case 2:
								Toast.makeText(me, "ToDo: add tO tOdO lIST", Toast.LENGTH_LONG).show();
								
								break;
						}
					}
				});
				
				builder.create().show();
			}
		});
		
		c.close();

		
		final ListView locations = (ListView) this.findViewById(R.id.locations_list);
		
		String selection = AvastContentProvider.LOCATION_ENABLED + " = 1";
		
		c = this.getContentResolver().query(AvastContentProvider.LOCATION_URI, null, selection, null, AvastContentProvider.LOCATION_DURATION + " DESC");
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_location_main, c, new String[0], new int[0])
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView name = (TextView) view.findViewById(R.id.label_location_name);
				name.setText(cursor.getString(cursor.getColumnIndex(AvastContentProvider.LOCATION_NAME)));

				TextView desc = (TextView) view.findViewById(R.id.label_location_desc);
				
				double latitude = cursor.getDouble(cursor.getColumnIndex(AvastContentProvider.LOCATION_LATITUDE));
				double longitude = cursor.getDouble(cursor.getColumnIndex(AvastContentProvider.LOCATION_LONGITUDE));
				
				String message = me.getString(R.string.label_location_desc, latitude, longitude);
				
				desc.setText(message);
			}
		};
		
		locations.setAdapter(adapter);
		
		locations.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
			{
				me.selectLocation(id);
			}
		});
	}

	protected void selectLocation(long id) 
	{
		final MainActivity me = this;

		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		final GoogleMap map = fragment.getMap();

		String selection = AvastContentProvider.LOCATION_ID + " = ?";
		String[] selectionArgs = { "" + id };
		
		Cursor c = this.getContentResolver().query(AvastContentProvider.LOCATION_URI, null, selection, selectionArgs, null);
		
		if (c.moveToNext())
		{
			selection = AvastContentProvider.VENUE_TYPE_ENABLED + " = 1";
			
			Cursor venueCursor = this.getContentResolver().query(AvastContentProvider.VENUE_TYPE_URI, null, selection, null, null);
			
			StringBuffer sb = new StringBuffer();
			
			while (venueCursor.moveToNext())
			{
				if (sb.length() > 0)
					sb.append(",");

				sb.append(venueCursor.getString(venueCursor.getColumnIndex(AvastContentProvider.VENUE_TYPE_FOURSQUARE_ID)));
			}
			
			venueCursor.close();

			LatLng coord = new LatLng(c.getDouble(c.getColumnIndex(AvastContentProvider.LOCATION_LATITUDE)), c.getDouble(c.getColumnIndex(AvastContentProvider.LOCATION_LONGITUDE)));
			
			CameraUpdate update = CameraUpdateFactory.newLatLng(coord);
			map.animateCamera(update);

			for (Marker marker : this._venueMarkers)
			{
				marker.remove();
			}
			
			this._venueMarkers.clear();
			this._markerIds.clear();
			
			if (sb.length() > 0)
			{		
				AvastContentProvider.fetchLocations(me, coord, sb.toString(), new Runnable()
				{
					public void run() 
					{
						for (Venue venue : AvastContentProvider.getLastLocations())
						{
							MarkerOptions marker = new MarkerOptions();
							marker.position(venue.location);
							marker.title(venue.name);
	
							Marker m = map.addMarker(marker);
							
							me._venueMarkers.add(m);
							me._markerIds.put(m, venue.foursquareId);
						}
					}
				});
			}
		}
		
		c.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final MainActivity me = this;

		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_edit_venues:
				Intent venueIntent = new Intent(this, VenueTypeActivity.class);
				venueIntent.putExtra(VenueTypeActivity.ONE_SHOT, true);
				
				this.startActivity(venueIntent);

				break;
			case R.id.action_edit_locations:
				Intent locationIntent = new Intent(this, LocationChooserActivity.class);
				locationIntent.putExtra(LocationChooserActivity.ONE_SHOT, true);
				
				this.startActivity(locationIntent);
					
				break;
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				this.startActivity(settingsIntent);
				
				break;
			case R.id.action_feedback:
				this.sendFeedback(this.getString(R.string.app_name));
					
				break;
			case R.id.action_filter_venues:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_filter_places);
				
				final Cursor c = this.getContentResolver().query(AvastContentProvider.VENUE_TYPE_URI, null, null, null, AvastContentProvider.VENUE_TYPE_NAME);
				
				builder.setMultiChoiceItems(c, AvastContentProvider.VENUE_TYPE_ENABLED, AvastContentProvider.VENUE_TYPE_NAME, new OnMultiChoiceClickListener()
				{
					public void onClick(DialogInterface arg0, int which, boolean checked) 
					{
						c.moveToPosition(which);
						
						long id = c.getLong(c.getColumnIndex(AvastContentProvider.VENUE_TYPE_ID));
						
						ContentValues values = new ContentValues();
						values.put(AvastContentProvider.VENUE_TYPE_ENABLED, checked);
						
						String where = AvastContentProvider.VENUE_TYPE_ID + " = ?";
						String[] whereArgs = { "" + id };
						
						me.getContentResolver().update(AvastContentProvider.VENUE_TYPE_URI, values, where, whereArgs);
					}
				});
				
				builder.setPositiveButton(R.string.action_close, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{

					}
				});
				
				builder.create().show();

				break;
		}
		
		return true;
	}
}
