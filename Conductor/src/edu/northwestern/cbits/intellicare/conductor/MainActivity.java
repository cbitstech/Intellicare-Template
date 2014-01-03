package edu.northwestern.cbits.intellicare.conductor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
	private String _packageFilter = null;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_main);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		long now = System.currentTimeMillis();
				
		if (now - prefs.getLong(AppStoreService.LAST_REFRESH, 0) > 24 * 60 * 60 * 10000)
			this.startService(new Intent(AppStoreService.REFRESH_APPS));

    	this.startService(new Intent(MessagesService.REFRESH_MESSAGES));
	}
	
	private class AppCell
	{
		private Uri _iconUri = null;
		private String _package = null;

		public AppCell()
		{
			
		}

		public AppCell(Uri icon, String packageName) 
		{
			this._iconUri = icon;
			this._package = packageName;
		}

		public Uri iconUri() 
		{
			return this._iconUri;
		}

		public String getPackage() 
		{
			return this._package;
		}
	}

	public void onResume()
	{
		super.onResume();
		
		final MainActivity me = this;

		GridView appsGrid = (GridView) this.findViewById(R.id.apps_grid);
		
		ArrayList<AppCell> apps = new ArrayList<AppCell>();
		
		Cursor installedApps = this.getContentResolver().query(ConductorContentProvider.INSTALLED_APPS_URI, null, null, null, null);
		
		while (installedApps.moveToNext() && apps.size() < 4)
		{
			String icon = installedApps.getString(installedApps.getColumnIndex("icon"));
			String packageName = installedApps.getString(installedApps.getColumnIndex("package"));
			
			apps.add(new AppCell(Uri.parse(icon), packageName));
		}
		
		installedApps.close();

		if (apps.size() <= 1)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder = builder.setTitle(R.string.dialog_no_apps_title);
			builder = builder.setMessage(R.string.dialog_no_apps_message);
			builder = builder.setPositiveButton(R.string.dialog_no_apps_yes, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
        			Intent nativeIntent = new Intent(me, AppStoreActivity.class);
        			me.startActivity(nativeIntent);
				}
			});
			
			builder = builder.setNegativeButton(R.string.dialog_no_apps_no, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{

				}
			});
			
			builder.create().show();
		}

		while (apps.size() < 4)
			apps.add(new AppCell());
		
        ArrayAdapter<AppCell> adapter = new ArrayAdapter<AppCell>(this, R.layout.cell_app, apps)
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                final AppCell app = this.getItem(position);
                
                if (convertView == null)
                    convertView = me.getLayoutInflater().inflate(R.layout.cell_app, parent, false);
                
                final UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon);

                String packageName = app.getPackage();
                
                if (packageName != null)
                {
	                String selection = "package = ?";
	                String[] args = { packageName };
	                
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
	                
	                cursor.close();
                }
                else
                	icon.setImageResource(R.drawable.ic_app_placeholder);
                	
                
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
                	me.filterMessages(app.getPackage());
            }
        });
        
        appsGrid.setOnItemLongClickListener(new OnItemLongClickListener()
        {
			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) 
			{
                final AppCell app = (AppCell) adapter.getItemAtPosition(position);

                if (app.iconUri() != null)
                	me.startActivity(me.getPackageManager().getLaunchIntentForPackage(app._package));

				return true;
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
				
				me.refreshList();

		    	me.startService(new Intent(MessagesService.REFRESH_MESSAGES));
			}
        };

        toggle.setOnClickListener(listener);
        listener.onClick(toggle);
        
        if (this.visibleItems() == 0)
        	this._showAll = false;
        
        this.refreshList();
	}
	
	protected void filterMessages(String packageName) 
	{
		if (this._packageFilter != null && this._packageFilter.equals(packageName))
			this._packageFilter = null;
		else
			this._packageFilter = packageName;
		
		this.refreshList();
	}
	
	private int visibleItems()
	{
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = "responded, date DESC";

        if (this._showAll)
			selection = "responded = 0";
		else
	        sortOrder = "responded, weight, date DESC";
        
        if (this._packageFilter != null)
        {
        	selection = "package = ?";
        	String[] args = { this._packageFilter };
        	
        	selectionArgs = args;
        }
		
		Cursor c = this.getContentResolver().query(ConductorContentProvider.MESSAGES_URI, null, selection, selectionArgs, sortOrder);
		
		int count = c.getCount();
		
		c.close();
		
		return count;
	}

	private void refreshList() 
	{
		final MainActivity me = this;
		
        final ListView messagesList = (ListView) me.findViewById(R.id.messages);
        final TextView toggle = (TextView) this.findViewById(R.id.toggle_label);

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
        
        if (this._packageFilter != null)
        {
        	selection = "package = ?";
        	String[] args = { this._packageFilter };
        	
        	selectionArgs = args;
        }
		
		Cursor c = me.getContentResolver().query(ConductorContentProvider.MESSAGES_URI, null, selection, selectionArgs, sortOrder);

		final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(me, R.layout.row_app_message, c, new String[0], new int[0], 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
                String text = cursor.getString(cursor.getColumnIndex("message"));
                String appName = cursor.getString(cursor.getColumnIndex("name"));
                
                long timestamp = cursor.getLong(cursor.getColumnIndex("date"));
                boolean responded = (0 != cursor.getInt(cursor.getColumnIndex("responded")));		

        		final String packageName = cursor.getString(cursor.getColumnIndex("package"));

        		final UriImageView icon = (UriImageView) view.findViewById(R.id.icon);
                
                Uri imageUri = ConductorContentProvider.iconUri(me, packageName, new Runnable()
                {
					public void run() 
					{
						Uri imageUri = ConductorContentProvider.iconUri(me, packageName, null);
						
						icon.setImageURI(imageUri);
					}
                });

                if (imageUri != null)
    				icon.setImageURI(imageUri);
                
                if (responded)
                {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    matrix.setScale(1, 1, 1, 0.5f);

                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    icon.setColorFilter(filter);
                }
                else
                    icon.setColorFilter(null);
				
				TextView message = (TextView) view.findViewById(R.id.app_message);
				message.setText(text);
				
                TextView details = (TextView) view.findViewById(R.id.app_message_details);
                details.setText(appName + " @ " + df.format(new Date(timestamp)));

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
				Cursor cursor = (Cursor) list.getItemAtPosition(position);
				
				String urlString = cursor.getString(cursor.getColumnIndex("uri"));
				
				if (urlString != null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
					
					try
					{
						me.startActivity(intent);
						
						ContentValues values = new ContentValues();
						values.put("responded", 1);
						
						String selection = "uri = ?";
						String[] args = { urlString };
						
						me.getContentResolver().update(ConductorContentProvider.MESSAGES_URI, values, selection, args);
					}
					catch (ActivityNotFoundException e)
					{
						e.printStackTrace();
						
						String s = me.getString(R.string.no_associated_action, urlString);

						Toast.makeText(me, s, Toast.LENGTH_LONG).show();

					}
				}				
				else
					Toast.makeText(me, R.string.no_available_action, Toast.LENGTH_LONG).show();
			}
        });
        
        messagesList.setBackgroundColor(0xffe0e0e0);
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
