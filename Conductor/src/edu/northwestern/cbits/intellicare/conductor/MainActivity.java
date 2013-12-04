package edu.northwestern.cbits.intellicare.conductor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_main);

//		UpdateManager.register(this, APP_ID);
	}
	
	private class AppCell
	{
		private Uri _iconUri = null;
		private String _message = null;
		private String _name = null;
		private long _time = 0;
		private boolean _expired = false;

		public AppCell(String name, Uri icon, String message, int badgeCount, long time) 
		{
			this._iconUri = icon;
			this._message = message;
			this._name = name;
			
			this._time = time;
		}

		public Uri iconUri() 
		{
			return this._iconUri;
		}

		public String getMessage() 
		{
			return this._message;
		}
		
		public long getTime()
		{
			return this._time ;
		}

		public String getName() 
		{
			return this._name ;
		}

		public boolean getExpired() 
		{
			return this._expired;
		}

		public void setExpired(boolean expired) 
		{
			this._expired  = expired;
		}
	}

	public void onResume()
	{
		super.onResume();
		
		final MainActivity me = this;
		final SimpleDateFormat sdf = new SimpleDateFormat(this.getString(R.string.message_date_format));
		
		GridView appsGrid = (GridView) this.findViewById(R.id.apps_grid);

		final ArrayList<AppCell> apps = new ArrayList<AppCell>();

		String[] appNames = this.getResources().getStringArray(R.array.app_names);
		String[] appIcons = this.getResources().getStringArray(R.array.app_icons);
		String[] appMessages = this.getResources().getStringArray(R.array.app_messages);
		String[] appBadges = this.getResources().getStringArray(R.array.app_badges);
		
		for (int i = 0; i < appNames.length; i++)
		{
			long time = System.currentTimeMillis() - (9 * 1000 * 60 * 60 * i);
			
			AppCell cell = new AppCell(appNames[i], Uri.parse(appIcons[i]), appMessages[i], Integer.parseInt(appBadges[i]), time);

			if (i > 2)
				cell.setExpired(true);
			
			apps.add(cell);
		}
		
        ArrayAdapter<AppCell> adapter = new ArrayAdapter<AppCell>(this, R.layout.cell_app, apps.subList(0, 3))
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                final AppCell app = this.getItem(position);
                
                if (convertView == null)
                    convertView = me.getLayoutInflater().inflate(R.layout.cell_app, parent, false);
                
                UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon);
                
                icon.setCachedImageUri(app.iconUri());
                
                return convertView;
            }
        };

        appsGrid.setAdapter(adapter);

        appsGrid.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
            {
                final AppCell app = apps.get(position);
                
                Toast.makeText(me, app.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        ArrayList<AppCell> messages = new ArrayList<AppCell>();
        
        for (AppCell app : apps)
        {
        	String message = app.getMessage();
        	
        	if (message != null && message.trim().length() > 0)
        		messages.add(app);
        }
        
        Collections.sort(messages, new Comparator<AppCell>()
		{
			public int compare(AppCell one, AppCell two) 
			{
				return (int) (two.getTime() - one.getTime());
			}
		});
        
        ArrayAdapter<AppCell> messagesAdapter = new ArrayAdapter<AppCell>(this, R.layout.row_app_message, messages) 
		{
            public View getView (int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.row_app_message, null);
                }

                AppCell app = this.getItem(position);

                TextView titleView = (TextView) convertView.findViewById(R.id.app_message);
                TextView subtitleView = (TextView) convertView.findViewById(R.id.app_message_details);
                
                titleView.setText(app.getMessage());
                subtitleView.setText(me.getString(R.string.message_subtitle, app.getName(), sdf.format(new Date(app.getTime()))));

                UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon);
                
                if (app.getExpired())
                {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    matrix.setScale(1, 1, 1, 0.5f);

                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    icon.setColorFilter(filter);
                    
                    titleView.setTextColor(Color.argb(128, 0, 0, 0));
                    subtitleView.setTextColor(Color.argb(128, 0, 0, 0));
                    
                    convertView.setBackgroundColor(Color.argb(255, 240, 240, 240));
                }
                else
                {
                	icon.setColorFilter(null);
                    titleView.setTextColor(Color.argb(255, 0, 0, 0));
                    subtitleView.setTextColor(Color.argb(255, 0, 0, 0));

                    convertView.setBackgroundColor(Color.argb(255, 255, 255, 255));
                }
                
                icon.setCachedImageUri(app.iconUri());

                return convertView;
            }
        };

        ListView messagesList = (ListView) this.findViewById(R.id.messages);
        messagesList.setAdapter(messagesAdapter);
        
        ImageView avatar = (ImageView) this.findViewById(R.id.avatar_view);
        avatar.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent nativeIntent = new Intent(me, AvatarActivity.class);
				me.startActivity(nativeIntent);
			}
        });
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_settings)
		{
			Intent nativeIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(nativeIntent);
		}
		else if (item.getItemId() == R.id.action_store)
		{
			Intent nativeIntent = new Intent(this, AppStoreActivity.class);
			this.startActivity(nativeIntent);
		}

		return true;
	}
}
