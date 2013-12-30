package edu.northwestern.cbits.intellicare.conductor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public class AppStoreService extends IntentService 
{
	public static final String REFRESH_APPS = "conductor_refresh_apps";
	public static final String APPS_UPDATED = "apps_updated";
	public static final String LAST_REFRESH = "last_refresh";

	public AppStoreService(String name) 
	{
		super(name);
	}

	public AppStoreService() 
	{
		super("Intellicare App Catalog Service");
	}

	protected void onHandleIntent(Intent intent) 
	{
		String action = intent.getAction();
		
		final AppStoreService me = this;
		
		if (AppStoreService.REFRESH_APPS.equals(action))
		{
			Runnable r = new Runnable()
			{
				public void run() 
				{
					try 
					{
						URL u = new URL(me.getString(R.string.app_store_json));

	                    URLConnection conn = u.openConnection();
	                    InputStream in = conn.getInputStream();
	                    
	                    ByteArrayOutputStream out = new ByteArrayOutputStream();

	                    byte[] buffer = new byte[8192];
	                    int read = 0;
	                    
	                    while ((read = in.read(buffer, 0, buffer.length)) != -1)
	                    {
	                        out.write(buffer, 0, read);
	                    }
	                    
	                    out.close();

	                    String json = out.toString("utf8");
	                    
	                    JSONArray apps = new JSONArray(json);

                    	String selection = "_id != -1";
                    	String[] selectionArgs = {};
                    	
                    	me.getContentResolver().delete(ConductorContentProvider.APPS_URI, selection, selectionArgs);

	                    for (int i = 0; i < apps.length(); i++)
	                    {
	                    	JSONObject app = apps.getJSONObject(i);
	                    	
	                    	String packageName = app.getString("package");

	                    	ContentValues values = new ContentValues();
	                    	values.put("package", packageName);
	                    	values.put("name", app.getString("name"));
	                    	values.put("recommendation", "[To be implemented]");
	                    	values.put("synopsis", app.getString("synopsis"));
	                    	values.put("score", 10);

	                    	values.put("icon", app.getString("icon"));

	                    	JSONArray versions = app.getJSONArray("versions");
	                    	
	                    	if (versions.length() > 0)
	                    	{
	                    		JSONObject version = versions.getJSONObject(0);
	                    		
	                    		values.put("date", version.getLong("updated") * 1000);

	                    		values.put("version", version.getString("name"));
	                    		values.put("changelog", version.getString("changelog"));
	                    		values.put("url", version.getString("url"));
	                    	}	                    	
	                    	
	                    	me.getContentResolver().insert(ConductorContentProvider.APPS_URI, values);
	                    }
					}
					catch (MalformedURLException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					} 
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
					
					LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(me);
					broadcast.sendBroadcast(new Intent(AppStoreService.APPS_UPDATED));
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
					
					Editor e = prefs.edit();
					e.putLong(AppStoreService.LAST_REFRESH, System.currentTimeMillis());
					e.commit();
				}
			};
			
			Thread t = new Thread(r);
			t.start();
		}
	}
}
