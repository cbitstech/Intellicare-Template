package edu.northwestern.cbits.intellicare.conductor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
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
	private boolean _showAll = true;
	
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

	@SuppressLint("SimpleDateFormat")
	public void onResume()
	{
		super.onResume();
		
		final MainActivity me = this;
		final SimpleDateFormat sdf = new SimpleDateFormat(this.getString(R.string.message_date_format));
		
		GridView appsGrid = (GridView) this.findViewById(R.id.apps_grid);

		ArrayList<AppCell> apps = new ArrayList<AppCell>();

		String[] appNames = this.getResources().getStringArray(R.array.app_names);
		String[] appIcons = this.getResources().getStringArray(R.array.app_icons);
		String[] appMessages = this.getResources().getStringArray(R.array.app_messages);
		String[] appBadges = this.getResources().getStringArray(R.array.app_badges);
		
		for (int i = 0; i < appNames.length; i++)
		{
			long time = System.currentTimeMillis() - (9 * 1000 * 60 * 60 * i);
			
			AppCell cell = new AppCell(appNames[i], null, appMessages[i], Integer.parseInt(appBadges[i]), time);

			if (i > 2)
				cell.setExpired(true);
			
			apps.add(cell);
		}
		
		apps = new ArrayList<AppCell>(apps.subList(0, 3));
		
		while (apps.size() < 4)
			apps.add(new AppCell("none", null, null, 0, System.currentTimeMillis()));
		
        ArrayAdapter<AppCell> adapter = new ArrayAdapter<AppCell>(this, R.layout.cell_app, apps)
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                final AppCell app = this.getItem(position);
                
                if (convertView == null)
                    convertView = me.getLayoutInflater().inflate(R.layout.cell_app, parent, false);
                
                final UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon);

                String selection = "name = ?";
                String[] args = { app.getName() };
                
				icon.setImageDrawable(me.getResources().getDrawable(R.drawable.ic_app_placeholder));

                Cursor cursor = me.getContentResolver().query(ConductorContentProvider.APPS_URI, null, selection, args, null);
                
                if (cursor.moveToNext())
                {
                    final Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex("icon")));

                    Uri cachedUri = ConductorContentProvider.fetchCachedUri(me, imageUri, new Runnable()
                    {
    					public void run() 
    					{
    						me.runOnUiThread(new Runnable()
    						{
    							public void run() 
    							{
    				                Uri cachedUri = ConductorContentProvider.fetchCachedUri(me, imageUri, null);
    								
    				                if (cachedUri != null)
    									icon.setImageURI(cachedUri);
    							}
    						});
    					}		                
                    });

    				if (cachedUri != null)
    					icon.setImageURI(cachedUri);
                }
                
                return convertView;
            }
        };

        appsGrid.setAdapter(adapter);

        appsGrid.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
            {
                final AppCell app = (AppCell) adapter.getItemAtPosition(position);
                
                if (app.iconUri() == null)
                {
        			Intent nativeIntent = new Intent(me, AppStoreActivity.class);
        			me.startActivity(nativeIntent);
                }
                else
                	Toast.makeText(me, app.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        ImageView avatar = (ImageView) this.findViewById(R.id.avatar_view);
        avatar.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent nativeIntent = new Intent(me, AvatarActivity.class);
				me.startActivity(nativeIntent);
			}
        });
        
        final TextView toggle = (TextView) this.findViewById(R.id.toggle_label);
        
        OnClickListener listener = new OnClickListener()
        {
			public void onClick(View view) 
			{
				me._showAll = (me._showAll == false);
				
		        final ListView messagesList = (ListView) me.findViewById(R.id.messages);

		        String selection = null;
		        String[] selectionArgs = null;
		        String sortOrder = "responded, date DESC";

		        if (me._showAll)
				{
					toggle.setText(R.string.label_show_all);
					selection = "responded = 0";
				}
				else
				{
					toggle.setText(R.string.label_show_important);
					
			        sortOrder = "responded, weight, date DESC";
				}
				
				Cursor c = me.getContentResolver().query(ConductorContentProvider.MESSAGES_URI, null, selection, selectionArgs, sortOrder);

				SimpleCursorAdapter adapter = new SimpleCursorAdapter(me, R.layout.row_app_message, c, new String[0], new int[0], 0)
				{
					public void bindView (View view, Context context, Cursor cursor)
					{
		                String text = cursor.getString(cursor.getColumnIndex("message"));
		                String appName = cursor.getString(cursor.getColumnIndex("name"));
		                
		                long timestamp = cursor.getLong(cursor.getColumnIndex("date"));
		                boolean responded = (0 != cursor.getInt(cursor.getColumnIndex("responded")));		

                		String packageName = cursor.getString(cursor.getColumnIndex("package"));
		                
		                Uri imageUri = Uri.parse(ConductorContentProvider.iconUri(packageName));
		                
						UriImageView icon = (UriImageView) view.findViewById(R.id.icon);
						icon.setImageURI(imageUri);

						
						TextView message = (TextView) view.findViewById(R.id.app_message);
						message.setText(text);
						
		                TextView details = (TextView) view.findViewById(R.id.app_message_details);
		                details.setText("tOdO: " + appName + " -- " + timestamp);

						if (responded)
						{
							view.setBackgroundColor(0xffe0e0e0);
							details.setTextColor(0xa0323232);
							message.setTextColor(0xa0323232);
						}
						else
						{
							view.setBackgroundColor(0xffffffff);
							details.setTextColor(0xff323232);
							message.setTextColor(0xff323232);
						}
					}
				};

		        messagesList.setAdapter(adapter);
		        messagesList.setOnItemClickListener(new OnItemClickListener()
		        {
					public void onItemClick(AdapterView<?> list, View view, int position, long id) 
					{
						Toast.makeText(me, "TODO: Launch associated app intent.", Toast.LENGTH_LONG).show();
					}
		        });
			}
        };

        toggle.setOnClickListener(listener);
        listener.onClick(toggle);
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
