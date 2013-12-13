package edu.northwestern.cbits.intellicare.conductor;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AppStoreActivity extends ConsentedActivity 
{
	private BroadcastReceiver _receiver = null; 
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_store);
		
		LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this);

		final AppStoreActivity me = this;
		
		this._receiver = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent) 
			{
				me.runOnUiThread(new Runnable()
				{
					public void run() 
					{
						Toast.makeText(me, R.string.apps_updated_toast, Toast.LENGTH_LONG).show();
						
						me.refreshList();
					}
				});
			}
		};
		
		broadcasts.registerReceiver(this._receiver, new IntentFilter(AppStoreService.APPS_UPDATED));
		
		this.setTitle(R.string.title_app_catalog);
	}
	
	protected void refreshList() 
	{
		final AppStoreActivity me = this;
		
		ListView list = (ListView) this.findViewById(R.id.list);
		
		Cursor c = this.getContentResolver().query(ConductorContentProvider.APPS_URI, null, null, null, "score DESC, name");

        final PackageManager packages = me.getPackageManager();

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(me, R.layout.row_app_store, c, new String[0], new int[0], 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
        		String packageName = cursor.getString(cursor.getColumnIndex("package"));

				final UriImageView icon = (UriImageView) view.findViewById(R.id.icon);

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
				else
					icon.setImageDrawable(me.getResources().getDrawable(R.drawable.ic_app_placeholder));
				
				TextView appName = (TextView) view.findViewById(R.id.app_name);
				appName.setText(cursor.getString(cursor.getColumnIndex("name")));
				
                TextView details = (TextView) view.findViewById(R.id.app_details);
                details.setText(cursor.getString(cursor.getColumnIndex("synopsis")));

                boolean isInstalled = false;
                boolean updateAvailable = false;
                
                try 
                {
            		String version = cursor.getString(cursor.getColumnIndex("version"));

            		PackageInfo info = packages.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                	
                	if (info != null)
                	{
                		isInstalled = true;
                		
                		if (version != null && version.equals(info.versionName) == false)
                			updateAvailable = true;
                	}
                } 
                catch (NameNotFoundException e) 
                {

                }  
                
                ImageButton downloadButton = (ImageButton) view.findViewById(R.id.button_download);
                
                if (isInstalled == false || updateAvailable == true)
                	downloadButton.setVisibility(View.VISIBLE);
                else
                	downloadButton.setVisibility(View.GONE);
                
                String url = cursor.getString(cursor.getColumnIndex("url"));

                if (url != null)
                {
                	downloadButton.setEnabled(true);
                	
                	final Uri appUri = Uri.parse(url);
                			
	                downloadButton.setOnClickListener(new OnClickListener()
	                {
						public void onClick(View view) 
						{
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(appUri);
							
							me.startActivity(intent);
						}
	                });
                }
                else
                	downloadButton.setEnabled(false);
                
            	TextView updateLabel = (TextView) view.findViewById(R.id.update_available);

                if (updateAvailable)
                	updateLabel.setVisibility(View.VISIBLE);
                else
                	updateLabel.setVisibility(View.GONE);
			}
		};

        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> list, View view, int position, long id) 
			{
				CursorWrapper cursor = (CursorWrapper) list.getItemAtPosition(position);
				
        		final String packageName = cursor.getString(cursor.getColumnIndex("package"));

				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder = builder.setTitle(cursor.getString(cursor.getColumnIndex("name")));
				builder = builder.setMessage(cursor.getString(cursor.getColumnIndex("synopsis")));
				
                boolean isInstalled = false;
                boolean updateAvailable = false;
                
                try 
                {
            		String version = cursor.getString(cursor.getColumnIndex("version"));

            		PackageInfo info = packages.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                	
                	if (info != null)
                	{
                		isInstalled = true;
                		
                		if (version != null && version.equals(info.versionName) == false)
                			updateAvailable = true;
                	}
                } 
                catch (NameNotFoundException e) 
                {

                }  

                if (isInstalled == false || updateAvailable == true)
                {
                    String url = cursor.getString(cursor.getColumnIndex("url"));

                    if (url != null)
                    {
                    	final Uri appUri = Uri.parse(url);
                    			
                    	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
                    	{
    						public void onClick(DialogInterface dialog, int which) 
    						{
    							Intent intent = new Intent(Intent.ACTION_VIEW);
    							intent.setData(appUri);
    							
    							me.startActivity(intent);
    						}
                    	};
                    	
                    	if (isInstalled)
                    		builder = builder.setPositiveButton(R.string.upgrade_label, listener);
                    	else
                    		builder = builder.setPositiveButton(R.string.install_label, listener);
                    }
                }
                else
                {
                	builder = builder.setPositiveButton(R.string.open_label, new DialogInterface.OnClickListener()
                	{
						public void onClick(DialogInterface dialog, int which) 
						{
						    Intent intent = packages.getLaunchIntentForPackage(packageName);
						    
						    if (intent != null)
						    {
							    intent.addCategory(Intent.CATEGORY_LAUNCHER);

							    me.startActivity(intent);
						    }
						}
                	});
                }
				
				builder.create().show();
			}
        });
	}

	public void onResume()
	{
		super.onResume();
		
		this.refreshList();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_apps, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_refresh)
		{
			Intent intent = new Intent(AppStoreService.REFRESH_APPS);
			
			this.startService(intent);
		}

		return true;
	}

}
