package edu.northwestern.cbits.intellicare.conductor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
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
						URL u = new URL(me.getString(R.string.app_store_json_release));
						
						if (me.isDebugBuild())
							u = new URL(me.getString(R.string.app_store_json_debug));

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

	                    		me.getContentResolver().insert(ConductorContentProvider.APPS_URI, values);
	                    	}	                    	
	                    	else
	                    	{
	                    		String where = "package = ?";
	                    		String[] args = { packageName };
	                    		
	                    		me.getContentResolver().delete(ConductorContentProvider.APPS_URI, where, args);
	                    	}
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
					
					me.updateVersionMessages();

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

	protected boolean isDebugBuild() 
	{
		// Via: http://stackoverflow.com/questions/7085644/how-to-check-if-apk-is-signed-or-debug-build
		
		X500Principal debugDn = new X500Principal("CN=Android Debug,O=Android,C=US");

		try
		{
	        PackageInfo pinfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
	        Signature signatures[] = pinfo.signatures;

	        CertificateFactory cf = CertificateFactory.getInstance("X.509");

	        for ( int i = 0; i < signatures.length;i++)
	        {   
	            ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());

	            X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);       
	            
	            if (cert.getSubjectX500Principal().equals(debugDn))
	            	return true;
	        }
	    }
	    catch (NameNotFoundException e)
	    {
	        //debuggable variable will remain false
	    }
	    catch (CertificateException e)
	    {
	        //debuggable variable will remain false
	    }
		
		return false;
	}

	private void updateVersionMessages() 
	{
		Cursor c = this.getContentResolver().query(ConductorContentProvider.APPS_URI, null, null, null, "date DESC, name");
		
		HashMap<String, HashMap<String, String>> apps = new HashMap<String, HashMap<String, String>>();
		
		while(c.moveToNext())
		{
			String name = c.getString(c.getColumnIndex("name"));
			
			if (apps.containsKey(name) == false)
			{
				HashMap<String, String> app = new HashMap<String, String>();
				
				app.put("name", name);
				app.put("version", c.getString(c.getColumnIndex("version")));
				app.put("package", c.getString(c.getColumnIndex("package")));
				app.put("url", c.getString(c.getColumnIndex("url")));
				
				apps.put(name, app);
			}
		}
		
		for (String key : apps.keySet())
		{
			HashMap<String, String> app = apps.get(key);
		
			String packageName = app.get("package");
			String version = app.get("version");
			
			String message = this.getString(R.string.msg_update_available, key, version);
			
			String selection = "package = ? AND message = ?";
			String[] args = { packageName, message };
			
			Cursor msgCursor = this.getContentResolver().query(ConductorContentProvider.MESSAGES_URI, null, selection, args, null);

			if (AppStoreService.updateAvailable(this, version, packageName))
			{
				if (msgCursor.getCount() > 0)
				{
					ContentValues update = new ContentValues();
					update.put("date", System.currentTimeMillis());
					update.put("responded", false);
					
					this.getContentResolver().update(ConductorContentProvider.MESSAGES_URI, update, selection, args);
				}
				else
				{
					ContentValues insert = new ContentValues();
					insert.put("date", System.currentTimeMillis());
					insert.put("package", packageName);
					insert.put("message", message);
					insert.put("name", key);
					insert.put("uri", app.get("url"));
					
					this.getContentResolver().insert(ConductorContentProvider.MESSAGES_URI, insert);
				}
			}
			else
			{
				if (msgCursor.getCount() > 0)
				{
					ContentValues update = new ContentValues();
					update.put("responded", true);
					
					this.getContentResolver().update(ConductorContentProvider.MESSAGES_URI, update, selection, args);
				}
			}

			msgCursor.close();
		}
		
		c.close();
	}

	public static boolean updateAvailable(Context context, String version, String packageName) 
	{
		if (version == null)
			return false;
		
		PackageManager packages = context.getPackageManager();

		try 
        {
    		PackageInfo info = packages.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        	
        	if (info != null)
        	{
        		if (version.equals(info.versionName) == false)
        			return true;
        	}
        } 
        catch (NameNotFoundException e) 
        {

        }  
		
		return false;
	}

	public static boolean isInstalled(Context context, String packageName) 
	{
		PackageManager packages = context.getPackageManager();

		try 
        {
    		PackageInfo info = packages.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        	
        	if (info != null)
        		return true;
        } 
        catch (NameNotFoundException e) 
        {

        }  
		
		return false;
	}
}
