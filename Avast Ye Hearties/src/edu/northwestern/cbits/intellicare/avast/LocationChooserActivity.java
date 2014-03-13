package edu.northwestern.cbits.intellicare.avast;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LocationChooserActivity extends ConsentedActivity 
{
	private boolean _showedDialog = false;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_location_chooser);
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(R.string.title_location_picker);
        actionBar.setSubtitle(R.string.subtitle_location_picker);
    }

	protected void onResume()
	{
		super.onResume();
		
		if (this._showedDialog  == false)
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
	}

/*	private Uri saveLocation(String name, double latitude, double longitude, double radius, long initialDuration, boolean enabled)
	{
		ContentValues values = new ContentValues();
		
		values.put(DataContentProvider.LOCATION_NAME, name);
		values.put(DataContentProvider.LOCATION_LATITUDE, latitude);
		values.put(DataContentProvider.LOCATION_LONGITUDE, longitude);
		values.put(DataContentProvider.LOCATION_RADIUS, radius);
		values.put(DataContentProvider.LOCATION_DURATION, initialDuration);
		values.put(DataContentProvider.LOCATION_ENABLED, enabled);
		
		return this.getContentResolver().insert(DataContentProvider.LOCATION_URI, values);
	}
*/	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_location_chooser, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		final LocationChooserActivity me = this;
		
		switch (itemId)
		{
			case R.id.action_next:
				Intent mainIntent = new Intent(this, MainActivity.class);
				this.startActivity(mainIntent);
				
				break;
			case R.id.action_pin:
				Log.e("AYH", "PIN CENTER POINT AND PROMPT FOR DETAILS...");

				MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
				final GoogleMap map = fragment.getMap();
				
				final CameraPosition position = map.getCameraPosition();
				
				Log.e("AYH", "" + position.target);
				
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
						
						marker.anchor(0.5f, 0.5f);
						marker.position(position.target);
						marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mark_red));
				
						map.addMarker(marker);
					}
				});
				
				builder.create().show();

				break;
		}
		
		return true;
	}

}
