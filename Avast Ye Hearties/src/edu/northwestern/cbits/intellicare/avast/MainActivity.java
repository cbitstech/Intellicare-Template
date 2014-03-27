package edu.northwestern.cbits.intellicare.avast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;

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
	public static final Uri URI = Uri.parse("intellicare://avast/home");
	
	private HashSet<Marker> _venueMarkers = new HashSet<Marker>();
	private HashSet<Venue> _myVenues = new HashSet<Venue>();
	private HashMap<Marker, Venue> _markerMap = new HashMap<Marker, Venue>();
	private HashMap<Marker, Long> _placeIds = new HashMap<Marker, Long>();
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		AvastVenuesManager manager = AvastVenuesManager.getInstance(this);
		manager.setup();

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
		
		CrashManager.register(this, "98bee775b937c15ddc62062006394cb0", new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});

		final MainActivity me = this;

		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = fragment.getMap();
		
		map.clear();
		this._myVenues.clear();

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
		
		c.close();

        c = this.getContentResolver().query(AvastContentProvider.VENUE_URI, null, null, null, null);
        
        while (c.moveToNext())
        {
        	Venue venue = new Venue();
        	
        	venue.foursquareId = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_FOURSQUARE_ID));
        	venue.name = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_NAME));
        	venue.address = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_ADDRESS));
        	venue.typeId = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_CATEGORY_ID));
        	venue.location = new LatLng(c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LATITUDE)), c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LONGITUDE)));

        	this._myVenues.add(venue);
        	
			MarkerOptions marker = new MarkerOptions();
			marker.position(venue.location);
			marker.title(venue.name);
			marker.snippet(venue.address);
			marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

			Marker m = map.addMarker(marker);
		
			me._venueMarkers.add(m);
			me._markerMap.put(m, venue);
        }
        
        c.close();
		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
		{
			public void onInfoWindowClick(final Marker marker) 
			{
				final Venue venue = me._markerMap.get(marker);

				MainActivity.showOptions(me, venue);
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
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor e = prefs.edit();
		e.remove(AvastVenuesManager.LAST_TITLE);
		e.commit();
	}

	protected static void showOptions(final Activity activity, final Venue venue) 
	{
		if (venue == null)
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(venue.name);

		int optionsList = R.array.list_venue_options;
		
		String selection = AvastContentProvider.VENUE_FOURSQUARE_ID + " = ?";
		String[] args = { "" + venue.foursquareId };
		
		Cursor c = activity.getContentResolver().query(AvastContentProvider.VENUE_URI, null, selection, args, null);
		
		if (c.getCount() > 0)
			optionsList = R.array.list_venue_options_sans_add;
		
		c.close();
		
		builder.setItems(optionsList, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				switch (which)
				{
					case 0:
						String id = venue.foursquareId;
						
						if (id != null)
						{
							Uri u = AvastContentProvider.uriForId(activity, id);
							
							if (u != null)
							{
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setData(u);
								
								activity.startActivity(intent);
							}
						}
						
						break;
					case 1:
						AlertDialog.Builder checkInBuilder = new AlertDialog.Builder(activity);
						
						checkInBuilder.setTitle(venue.name);

						LayoutInflater inflater = LayoutInflater.from(activity);
						View view = inflater.inflate(R.layout.view_check_in, null, false);
						
						final TimePicker timePicker = (TimePicker) view.findViewById(R.id.field_checkin_time);
						
						final TextView relaxLabel = (TextView) view.findViewById(R.id.label_relaxed);
					
						final SeekBar relaxed = (SeekBar) view.findViewById(R.id.field_relaxed);
						relaxed.setMax(4);
						relaxed.setProgress(2);
						relaxed.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
						{
							public void onProgressChanged(SeekBar bar, int which, boolean fromUser) 
							{
								switch (which)
								{
									case 0:
										relaxLabel.setText(R.string.label_very_tense);
										break;
									case 1:
										relaxLabel.setText(R.string.label_tense);
										break;
									case 2:
										relaxLabel.setText(R.string.label_normal);
										break;
									case 3:
										relaxLabel.setText(R.string.label_relaxed);
										break;
									case 4:
										relaxLabel.setText(R.string.label_very_relaxed);
										break;
								}
							}

							public void onStartTrackingTouch(SeekBar arg0) 
							{

							}

							public void onStopTrackingTouch(SeekBar arg0) 
							{

							}
						});
						

						final TextView entertainLabel = (TextView) view.findViewById(R.id.label_entertained);
						
						final SeekBar entertained = (SeekBar) view.findViewById(R.id.field_entertained);
						entertained.setMax(4);
						entertained.setProgress(2);
						entertained.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
						{
							public void onProgressChanged(SeekBar bar, int which, boolean fromUser) 
							{
								switch (which)
								{
									case 0:
										entertainLabel.setText(R.string.label_very_bored);
										break;
									case 1:
										entertainLabel.setText(R.string.label_bored);
										break;
									case 2:
										entertainLabel.setText(R.string.label_normal);
										break;
									case 3:
										entertainLabel.setText(R.string.label_entertained);
										break;
									case 4:
										entertainLabel.setText(R.string.label_very_entertained);
										break;
								}
							}

							public void onStartTrackingTouch(SeekBar arg0) 
							{

							}

							public void onStopTrackingTouch(SeekBar arg0) 
							{

							}
						});

						checkInBuilder.setView(view);

						checkInBuilder.setPositiveButton(R.string.action_checkin, new OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								int hour = timePicker.getCurrentHour();
								int minute = timePicker.getCurrentMinute();
								
								Calendar c = Calendar.getInstance();
								c.set(Calendar.HOUR_OF_DAY, hour);
								c.set(Calendar.MINUTE, minute);
								
								long checkinTime = c.getTimeInMillis();
								long now = System.currentTimeMillis();
								
								if (checkinTime > now)
									checkinTime -= 24 * 60 * 60 * 1000;
								
								int relaxedValue = relaxed.getProgress();
								int entertainedValue = entertained.getProgress();

								ContentValues values = new ContentValues();
								values.put(AvastContentProvider.CHECKIN_VENUE_ID, venue.foursquareId);
								values.put(AvastContentProvider.CHECKIN_DATE, checkinTime);
								values.put(AvastContentProvider.CHECKIN_RELAXED, relaxedValue);
								values.put(AvastContentProvider.CHECKIN_ENTERTAINED, entertainedValue);
								
								activity.getContentResolver().insert(AvastContentProvider.CHECKIN_URI, values);
							}
						});

						checkInBuilder.setNegativeButton(R.string.action_cancel, new OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which) 
							{

							}
						});

						checkInBuilder.create().show();
						
						break;
					case 2:
						String selection = AvastContentProvider.VENUE_FOURSQUARE_ID + " = ?";
						String[] args = { "" + venue.foursquareId };
						
						Cursor c = activity.getContentResolver().query(AvastContentProvider.VENUE_URI, null, selection, args, null);
						
						if (c.getCount() > 0)
						{
							
						}
						else
						{
							ContentValues values = new ContentValues();
							values.put(AvastContentProvider.VENUE_NAME, venue.name);
							values.put(AvastContentProvider.VENUE_FOURSQUARE_ID, venue.foursquareId);
							values.put(AvastContentProvider.VENUE_CATEGORY_ID, venue.typeId);
							values.put(AvastContentProvider.VENUE_ADDRESS, venue.address);
							values.put(AvastContentProvider.VENUE_LATITUDE, venue.location.latitude);
							values.put(AvastContentProvider.VENUE_LONGITUDE, venue.location.longitude);
							
							activity.getContentResolver().insert(AvastContentProvider.VENUE_URI, values);
						}

						c.close();
						
						break;
				}
			}
		});
		
		builder.create().show();	}

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
			this._markerMap.clear();
			
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
							marker.snippet(venue.address);
	
							if (me._myVenues.contains(venue))
								marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

							Marker m = map.addMarker(marker);
							
							me._venueMarkers.add(m);
							me._markerMap.put(m, venue);
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
			case R.id.action_list_venues:
				Intent myVenuesIntent = new Intent(this, MyVenuesActivity.class);
				
				this.startActivity(myVenuesIntent);
	
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
