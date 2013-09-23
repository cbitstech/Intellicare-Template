package edu.northwestern.cbits.intellicare.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.northwestern.cbits.ic_template.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;

public class LogManager 
{
	private static final String EVENT_TYPE = "event_type";
	private static final String TIMESTAMP = "timestamp";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String ALTITUDE = "altitude";
	private static final String TIME_DRIFT = "time_drift";
	
	private static final String LOG_QUEUE = "pending_log_queue";
	private static final String CONTENT_OBJECT = "content_object";
	private static final String USER_ID = "user_id";
	private static final String STACKTRACE = "stacktrace";

	private static LogManager _sharedInstance = null;
	
	private boolean _uploading = false;
	
	private Context _context = null;
	
	public LogManager(Context context) 
	{
		this._context = context;
	}

	public static LogManager getInstance(Context context)
	{
		if (LogManager._sharedInstance != null)
			return LogManager._sharedInstance;
		
		if (context != null)
			LogManager._sharedInstance = new LogManager(context.getApplicationContext());
		
		return LogManager._sharedInstance;
	}
	
	public String getUserId()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		String userId = prefs.getString("config_user_id", null);

		if (userId == null)
		{
			userId = "unknown-user";

			AccountManager manager = (AccountManager) this._context.getSystemService(Context.ACCOUNT_SERVICE);
			Account[] list = manager.getAccountsByType("com.google");

			if (list.length == 0)
				list = manager.getAccounts();

			if (list.length > 0)
				userId = list[0].name;

			Editor e = prefs.edit();
			e.putString("config_user_id", userId);
			e.commit();
		}
		
		return userId;
	}
	
	public boolean log(String event, Map<String, Object> payload)
	{
		long now = System.currentTimeMillis();

		if (payload == null)
			payload = new HashMap<String, Object>();
		
		payload.put("app_package", this._context.getApplicationContext().getPackageName());
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		if (prefs.getBoolean("config_log_location", false))
		{
			LocationManager lm = (LocationManager) this._context.getSystemService(Context.LOCATION_SERVICE);
		
			Location lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			Location backupLocation = null;
		
			if (lastLocation != null && now - lastLocation.getTime() > (1000 * 60 * 60))
			{
				backupLocation = lastLocation;
			
				lastLocation = null;
			}
			
			if (lastLocation == null)
				lastLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			if (lastLocation == null)
				lastLocation = backupLocation;
			
			if (lastLocation != null)
			{
				payload.put(LogManager.LATITUDE, lastLocation.getLatitude());
				payload.put(LogManager.LONGITUDE, lastLocation.getLongitude());
				payload.put(LogManager.ALTITUDE, lastLocation.getAltitude());
				payload.put(LogManager.TIME_DRIFT, now - lastLocation.getTime());
			}
		}

		payload.put(LogManager.EVENT_TYPE, event);
		payload.put(LogManager.TIMESTAMP, now / 1000);
		
		if (payload.containsKey(LogManager.USER_ID) == false)
			payload.put(LogManager.USER_ID, this.getUserId());

		try 
		{
			JSONArray pendingEvents = new JSONArray(prefs.getString(LogManager.LOG_QUEUE, "[]"));
			JSONObject jsonEvent = new JSONObject();
			
			for (String key : payload.keySet())
			{
				jsonEvent.put(key, payload.get(key));
			}

			jsonEvent.put(LogManager.CONTENT_OBJECT, new JSONObject(jsonEvent.toString()));

			pendingEvents.put(jsonEvent);
			
			Editor e = prefs.edit();
			e.putString(LogManager.LOG_QUEUE, pendingEvents.toString());
			e.commit();

			pendingEvents = new JSONArray(prefs.getString(LogManager.LOG_QUEUE, "[]"));
			
			final LogManager me = this;
			
			Runnable r = new Runnable()
			{
				public void run() 
				{
					me.attemptUploads();
				}
			};
			
			Thread t = new Thread(r);
			t.start();

			return true;
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public boolean queueContains(String event, Map<String, Object> payload)
	{
		return false;
	}

	public void attemptUploads() 
	{
		if (this._uploading)
			return;
		
		this._uploading = true;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		String endpointUri = this._context.getString(R.string.log_url);
		try 
		{
			JSONArray pendingEvents = new JSONArray(prefs.getString(LogManager.LOG_QUEUE, "[]"));
			
			try 
			{
				URI siteUri = new URI(endpointUri);
			
				AndroidHttpClient androidClient = AndroidHttpClient.newInstance("Purple Robot", this._context);

				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				
				SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
				
				if (prefs.getBoolean("config_http_liberal_ssl", true))
				{
			        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			        trustStore.load(null, null);

			        socketFactory = new LiberalSSLSocketFactory(trustStore);								
				}

				registry.register(new Scheme("https", socketFactory, 443));
				
				SingleClientConnManager mgr = new SingleClientConnManager(androidClient.getParams(), registry);
				HttpClient httpClient = new DefaultHttpClient(mgr, androidClient.getParams());

				androidClient.close();

				for (int i = 0; i < pendingEvents.length(); i++)
				{
					JSONObject event = pendingEvents.getJSONObject(i);
					
					HttpPost httpPost = new HttpPost(siteUri);
					
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("logJSON", event.toString()));
					nameValuePairs.add(new BasicNameValuePair("json", event.toString()));
					HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs, HTTP.US_ASCII);

					httpPost.setEntity(entity);
					
					httpClient.execute(httpPost);
					HttpResponse response = httpClient.execute(httpPost);

					HttpEntity httpEntity = response.getEntity();
					
					Log.e("PR-LOGGING", "Log upload result: " + EntityUtils.toString(httpEntity));
				}
				
				mgr.shutdown();
			}
			catch (URISyntaxException e) 
			{
				e.printStackTrace();
			} 
			catch (KeyStoreException e) 
			{
				e.printStackTrace();
			} 
			catch (NoSuchAlgorithmException e) 
			{
				e.printStackTrace();
			}
			catch (CertificateException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			catch (KeyManagementException e) 
			{
				e.printStackTrace();
			}
			catch (UnrecoverableKeyException e) 
			{
				e.printStackTrace();
			}
			
			Editor e = prefs.edit();
			e.putString(LogManager.LOG_QUEUE, "[]");
			e.commit();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		this._uploading = false;
	}

	public void logException(Throwable e) 
	{
		e.printStackTrace();

		Map<String, Object> payload = new HashMap<String, Object>();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		
		e.printStackTrace(out);
		
		out.close();
		
		String stacktrace = baos.toString();
		
		payload.put(LogManager.STACKTRACE, stacktrace);
		
		this.log("java_exception", payload);
	}
}
