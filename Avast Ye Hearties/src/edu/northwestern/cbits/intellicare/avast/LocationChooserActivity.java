package edu.northwestern.cbits.intellicare.avast;

import java.util.HashSet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LocationChooserActivity extends ConsentedActivity 
{
	public static final String ONE_SHOT = "one_shot";

	private boolean _showedDialog = false;
	
	private HashSet<Marker> _markers = new HashSet<Marker>();
	
	private boolean _isOneShot = false;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        this._isOneShot = intent.getBooleanExtra(VenueTypeActivity.ONE_SHOT, false);

        this.setContentView(R.layout.activity_location_chooser);
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(R.string.title_location_picker);
        actionBar.setSubtitle(R.string.subtitle_location_picker);
    }

	protected void onResume()
	{
		super.onResume();
		
		final LocationChooserActivity me = this;
		
		if (this._isOneShot == false && this._showedDialog == false)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_location_picker);
			builder.setMessage(R.string.message_location_picker);
			
			builder.setPositiveButton(R.string.action_continue, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder.create().show();
			
			this._showedDialog = true;
		}
		
		LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		GoogleMap map = fragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setMyLocationEnabled(true);
		
		if (location != null)
		{
			CameraUpdate lookAt = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
			
			map.moveCamera(lookAt);
		}
		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener()
		{
			public void onInfoWindowClick(final Marker marker) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(marker.getTitle());
				
				LatLng location = marker.getPosition();
				
				String message = me.getString(R.string.marker_details, location.latitude, location.longitude);
				builder.setMessage(message);
				
				builder.setNegativeButton(R.string.action_remove, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						me._markers.remove(marker);
						
						marker.remove();
					}
				});
				
				builder.setPositiveButton(R.string.action_close, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
			}
		});
		
		Cursor c = this.getContentResolver().query(DataContentProvider.LOCATION_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			MarkerOptions marker = new MarkerOptions();
			marker.title(c.getString(c.getColumnIndex(AvastContentProvider.LOCATION_NAME)));
			marker.snippet(me.getString(R.string.marker_options));
			marker.anchor(0.5f, 0.5f);

			LatLng position = new LatLng(c.getDouble(c.getColumnIndex(AvastContentProvider.LOCATION_LATITUDE)), c.getDouble(c.getColumnIndex(AvastContentProvider.LOCATION_LONGITUDE)));
			
			marker.position(position);
			marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mark_red));
	
			me._markers.add(map.addMarker(marker));
		}
		
		c.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_location_chooser, menu);
		
	    if (this._isOneShot)
	    {
	    	MenuItem nextItem = menu.findItem(R.id.action_next);
	    	
	    	nextItem.setTitle(R.string.action_save);
	    }

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		final LocationChooserActivity me = this;
		
		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		final GoogleMap map = fragment.getMap();

		switch (itemId)
		{
			case R.id.action_next:
				if (this._markers.size() == 0)
					Toast.makeText(this, R.string.toast_no_locations_error, Toast.LENGTH_LONG).show();
				else
				{
					this.getContentResolver().delete(DataContentProvider.LOCATION_URI, AvastContentProvider.LOCATION_ID + " != -1", null);
					
					for (Marker marker : this._markers)
					{
						ContentValues values = new ContentValues();
						
						LatLng coordinate = marker.getPosition();
						
						values.put(DataContentProvider.LOCATION_NAME, marker.getTitle());
						values.put(DataContentProvider.LOCATION_LATITUDE, coordinate.latitude);
						values.put(DataContentProvider.LOCATION_LONGITUDE, coordinate.longitude);
						values.put(DataContentProvider.LOCATION_RADIUS, AvastContentProvider.DEFAULT_RADIUS);
						values.put(DataContentProvider.LOCATION_DURATION, AvastContentProvider.DEFAULT_INITIAL_DURATION);
						values.put(DataContentProvider.LOCATION_ENABLED, true);
						
						this.getContentResolver().insert(DataContentProvider.LOCATION_URI, values);
					}
					
			        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			        Editor e = prefs.edit();
			        e.putBoolean(WelcomeActivity.INTRO_SHOWN, true);
			        e.commit();
			        
			        if (this._isOneShot == false)
			        {
			        	Intent mainIntent = new Intent(this, MainActivity.class);
			        	this.startActivity(mainIntent);
			        }
			        
					this.finish();
				}
				
				break;
			case R.id.action_pin:
				final CameraPosition position = map.getCameraPosition();
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_location_picker_prompt);
				
				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.view_location_label, null, false);
				
				final EditText nameField = (EditText) view.findViewById(R.id.field_location_name);
				builder.setView(view);
				
				builder.setPositiveButton(R.string.action_continue, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						MarkerOptions marker = new MarkerOptions();
						
						Editable name = nameField.getText();

						if (name != null && name.toString().trim().length() > 0)
							marker.title(name.toString());
						else
							marker.title(me.getString(R.string.marker_unknown));
						
						marker.snippet(me.getString(R.string.marker_options));
						marker.anchor(0.5f, 0.5f);
						marker.position(position.target);
						marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mark_red));
				
						me._markers.add(map.addMarker(marker));
					}
				});
				
				builder.create().show();

				break;
		}
		
		return true;
	}
}
