package edu.northwestern.cbits.intellicare.avast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Venue;

public class MyVenuesActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_my_venues);
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(R.string.title_my_venues);
    }
	
	protected void onResume()
	{
        super.onResume();

        final MyVenuesActivity me = this;
        
        ListView venuesList = (ListView) this.findViewById(R.id.venues_list);

        ArrayList<Venue> venues = new ArrayList<Venue>();
        
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        Location here = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if (here == null)
        	lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);        

        Cursor c = this.getContentResolver().query(AvastContentProvider.VENUE_URI, null, null, null, null);
        
        while (c.moveToNext())
        {
        	Venue v = new Venue();
        	
        	v.foursquareId = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_FOURSQUARE_ID));
        	v.name = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_NAME));
        	v.address = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_ADDRESS));
        	v.typeId = c.getString(c.getColumnIndex(AvastContentProvider.VENUE_CATEGORY_ID));
        	v.location = new LatLng(c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LATITUDE)), c.getDouble(c.getColumnIndex(AvastContentProvider.VENUE_LONGITUDE)));
        	
        	Location venueLocation = new Location(here.getProvider());
        	venueLocation.setLatitude(v.location.latitude);
        	venueLocation.setLongitude(v.location.longitude);
        	
        	v.distance = here.distanceTo(venueLocation);
        	
        	venues.add(v);
        }
        
        c.close();
        
        ActionBar actionBar = this.getSupportActionBar();
        
        if (venues.size() != 1)
        	actionBar.setSubtitle(this.getString(R.string.subtitle_my_venues, venues.size()));
        else
        	actionBar.setSubtitle(this.getString(R.string.subtitle_my_venues_single));
        
        Collections.sort(venues, new Comparator<Venue>()
		{
			public int compare(Venue v, Venue w) 
			{
				if (v.distance > w.distance)
					return 1;
				else if (v.distance < w.distance)
					return -1;

				return 0;
			}
		});

		final ArrayAdapter<Venue> adapter = new ArrayAdapter<Venue>(this, R.layout.row_checkbox, venues)
		{
			public View getView (int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(me);
					convertView = inflater.inflate(R.layout.row_venue, null, false);
				}
				
				final Venue venue = this.getItem(position);
				
				TextView venueLabel = (TextView) convertView.findViewById(R.id.label_venue);
				venueLabel.setText(venue.name);

				TextView distanceLabel = (TextView) convertView.findViewById(R.id.label_distance);
				distanceLabel.setText(me.getString(R.string.label_distance, (venue.distance / 1609.34)));

				return convertView;
			}
		};
		
		venuesList.setAdapter(adapter);
		
		venuesList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Venue venue = adapter.getItem(position);
				
				MainActivity.showOptions(me, venue);
			}
		});
		
		venuesList.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
			{
				final Venue venue = adapter.getItem(position);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(venue.name);
				builder.setMessage(R.string.message_delete_venue);
				
				builder.setPositiveButton(R.string.action_yes, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						String selection = AvastContentProvider.VENUE_FOURSQUARE_ID + " = ?";
						String[] args = { "" + venue.foursquareId };
						
						me.getContentResolver().delete(AvastContentProvider.VENUE_URI, selection, args);
						
						me.onResume();
					}
				});
				
				builder.setNegativeButton(R.string.action_no, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{

					}
				});

				builder.create().show();
				
				return true;
			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_my_venues, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_cancel:
				this.finish();
				
				break;
			default:
				break;
		}
		
		return true;
	}

}
