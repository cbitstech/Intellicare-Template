package edu.northwestern.cbits.intellicare.moveme;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MapActivity extends ConsentedActivity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_map);
        
        ActionBar actionBar = this.getSupportActionBar();
        
        actionBar.setTitle(R.string.title_my_map);

		LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Location last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		if (last != null)
		{
			double latitude = last.getLatitude();
			double longitude = last.getLongitude();

			MapFragment fragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
			GoogleMap map = fragment.getMap();
			map.setMyLocationEnabled(true);
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
		}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_map, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if (item.getItemId() == R.id.action_close)
    	{
    		this.finish();
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }

}
