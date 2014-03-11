package edu.northwestern.cbits.intellicare.avast;

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Category;
import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Venue;

public class MainActivity extends ActionBarActivity 
{
	private static final String LAST_LATITUDE = "last_known_latitude";
	private static final String LAST_LONGITUDE = "last_known_longitude";
	
	private final HashSet<String> _selectedIds = new HashSet<String>();

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
		
		final MainActivity me = this;
		
		final ListView categories = (ListView) this.findViewById(R.id.categories_list);
		
		AvastContentProvider.fetchCategories(this, new Runnable()
		{
			public void run() 
			{
				List<Category> categoryList = AvastContentProvider.getLastCategories();
				
				ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(me, R.layout.row_checkbox, categoryList)
				{
					public View getView (int position, View convertView, ViewGroup parent)
					{
						if (convertView == null)
						{
							LayoutInflater inflater = LayoutInflater.from(me);
							convertView = inflater.inflate(R.layout.row_checkbox, null, false);
						}
						
						convertView = super.getView(position, convertView, parent);
						
						final Category category = this.getItem(position);
						
						Log.e("AYH", "GOT: " + category.name);
						
						CheckBox check = (CheckBox) convertView.findViewById(R.id.checkbox);

						check.setOnCheckedChangeListener(new OnCheckedChangeListener()
						{
							public void onCheckedChanged(CompoundButton arg0, boolean checked) 
							{

							}
						});

						check.setChecked(me._selectedIds.contains(category.id));
						
						check.setOnCheckedChangeListener(new OnCheckedChangeListener()
						{
							public void onCheckedChanged(CompoundButton arg0, boolean checked) 
							{
								if (checked)
									me._selectedIds.add(category.id);
								else
									me._selectedIds.remove(category.id);
								
								me.redrawMap();
							}
						});
						
						check.setText(category.name);
						
						return convertView;
					}
				};
				
				categories.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				categories.invalidate();
			}
		});
	}
	
	private void redrawMap()
	{
		MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
		
		final GoogleMap map = fragment.getMap();
		
		StringBuffer sb = new StringBuffer();
		
		for (String selected : this._selectedIds)
		{
			if (sb.length() > 0)
				sb.append(",");
			
			sb.append(selected);
		}
		
		Log.e("AYH", "LOC QUERY: " + sb.toString());
		
		if (sb.length() > 0)
		{		
			AvastContentProvider.fetchLocations(this, map.getCameraPosition().target, sb.toString(), new Runnable()
			{
				public void run() 
				{
					map.clear();
	
					for (Venue venue : AvastContentProvider.getLastLocations())
					{
						MarkerOptions marker = new MarkerOptions();
						marker.position(venue.location);
						marker.title(venue.name);
						
						map.addMarker(marker);
					}
				}
			});
		}
		else
			map.clear();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
