package edu.northwestern.cbits.intellicare.dailyfeats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.oauth.FitbitApi;

public class ScheduleManager
{
	public static final String REMINDER_HOUR = "preferred_hour";
	public static final String REMINDER_MINUTE = "preferred_minutes";
	public static final int DEFAULT_HOUR = 18;
	public static final int DEFAULT_MINUTE = 0;
	private static final String LAST_NOTIFICATION = "last_notification";

	private static ScheduleManager _instance = null;

	private Context _context = null;
	
	private long _lastGitHubCheck = 0;
	private long _lastFitbitCheck = 0;
	private long _lastJawboneCheck = 0;

	public ScheduleManager(Context context) 
	{
		this._context  = context.getApplicationContext();
		
		AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);
		
		Intent broadcast = new Intent(this._context, ScheduleHelper.class);
		PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000, pi);
	}

	public static ScheduleManager getInstance(Context context)
	{
		if (ScheduleManager._instance == null)
		{
			ScheduleManager._instance = new ScheduleManager(context.getApplicationContext());
			ScheduleManager._instance.updateSchedule();
		}
		
		return ScheduleManager._instance;
	}
	
	public void updateSchedule()
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		long lastNotification = prefs.getLong(ScheduleManager.LAST_NOTIFICATION, 0);
		long now = System.currentTimeMillis();
		
    	int hour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
        int minutes = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(now);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		long scheduled = c.getTimeInMillis();
		
		if (lastNotification < scheduled && now > scheduled)
		{
			String title = this._context.getString(R.string.note_title);
			String message = this._context.getString(R.string.note_message);
			
			Intent intent = new Intent(this._context, CalendarActivity.class);

			PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

			int oldLevel = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);
			int newLevel = FeatsProvider.checkLevel(this._context, oldLevel);

			Editor e = prefs.edit();
			e.putInt(FeatsProvider.DEPRESSION_LEVEL, newLevel);
			e.commit();

			if (oldLevel > newLevel)
			{
				e.putLong(FeatsProvider.LEVEL_CHANGE_DATE, System.currentTimeMillis());
				e.commit();

				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("user_level", newLevel);
				payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));

				LogManager.getInstance(this._context).log("feats_demoted", payload);

				message = this._context.getString(R.string.note_title_demoted, newLevel);
			}
			else if (newLevel > oldLevel)
			{
				e.putLong(FeatsProvider.LEVEL_CHANGE_DATE, System.currentTimeMillis());
				e.commit();
				HashMap<String, Object> payload = new HashMap<String, Object>();
				
				payload.put("user_level", newLevel);
				payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));

				LogManager.getInstance(this._context).log("feats_promoted", payload);
				
				message = this._context.getString(R.string.note_title_promoted, newLevel);
			}
			
			if (oldLevel != newLevel)
			{
				ContentValues values = new ContentValues();
				values.put("enabled", true);
			
				String where = "feat_level = ?";
				String[] args = { "" + newLevel };
						
				this._context.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);
			}
			
			LogManager.getInstance(this._context).log("feats_checkin_notified", null);

			StatusNotificationManager.getInstance(this._context).notifyBigText(97531, R.drawable.ic_notification_feats, title, message, pi, StartupActivity.URI);

			e = prefs.edit();
			e.putLong(ScheduleManager.LAST_NOTIFICATION, now);
			e.commit();
		}
		
		final ScheduleManager me = this;
		
		if (now - this._lastGitHubCheck > 1800000 && prefs.getBoolean("settings_github_enabled", false))
		{
			Runnable r = new Runnable()
			{
				public void run() 
				{
					JSONArray commits = me.todayCommits();
					
					int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

					String feat = me._context.getString(R.string.feat_github_checkin);

					FeatsProvider.clearFeats(me._context, feat, new Date());
					
					if (commits.length() > 0)
						FeatsProvider.createFeat(me._context, feat, level);
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			
			this._lastGitHubCheck = now;
		}

		if (now - this._lastFitbitCheck > 1800000 && prefs.getBoolean("settings_fitbit_enabled", false))
		{
			Runnable r = new Runnable()
			{
				public void run() 
				{
					Set<String> goals = me.todayFitbitGoals();

					int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

					for (String feat : goals)
					{
						FeatsProvider.clearFeats(me._context, feat, new Date());
						
						FeatsProvider.createFeat(me._context, feat, level);
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			
			this._lastFitbitCheck  = now;
		}
		
		if (now - this._lastJawboneCheck > 1800000 && prefs.getBoolean("settings_jawbone_enabled", false))
		{
			Runnable r = new Runnable()
			{
				public void run() 
				{
					Set<String> goals = me.todayJawboneGoals();

					int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

					for (String feat : goals)
					{
						FeatsProvider.clearFeats(me._context, feat, new Date());
						
						FeatsProvider.createFeat(me._context, feat, level);
					}
				}
			};
			
			Thread t = new Thread(r);
			t.start();
			
			this._lastJawboneCheck  = now;
		}
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private Set<String> todayFitbitGoals()
	{
		Set<String> metGoals = new HashSet<String>();
		
		final ScheduleManager me = this;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		if (prefs.getBoolean("settings_fitbit_enabled", false))
		{
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        	String dateString = sdf.format(new Date());
        	
			String token = prefs.getString("oauth_fitbit_token", "");
			String secret = prefs.getString("oauth_fitbit_secret", "");

			Token accessToken = new Token(token, secret);
        	
			final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/date/" + dateString + ".json");

        	ServiceBuilder builder = new ServiceBuilder();
        	builder = builder.provider(FitbitApi.class);
        	builder = builder.apiKey(FitbitApi.CONSUMER_KEY);
        	builder = builder.apiSecret(FitbitApi.CONSUMER_SECRET);

        	final OAuthService service = builder.build();

			service.signRequest(accessToken, request);

			try
			{
				Response response = request.send();

				JSONObject body = new JSONObject(response.getBody());
				
				JSONObject summary = body.getJSONObject("summary");

				long veryActive = summary.getLong("veryActiveMinutes");
//				long fairlyActive = summary.getLong("fairlyActiveMinutes");
//				long lightlyActive = summary.getLong("lightlyActiveMinutes");

//				long active = veryActive; //  + fairlyActive + lightlyActive;

				long steps = summary.getLong("steps");

				JSONArray activities = summary.getJSONArray("distances");

				long distance = 0;

				for (int i = 0; i < activities.length(); i++)
				{
					JSONObject activity = activities.getJSONObject(i);

					if ("total".equals(activity.getString("activity")))
						distance = activity.getLong("distance");
				}

				JSONObject goals = body.getJSONObject("goals");

				long goalDistance = goals.getLong("distance");
				long goalSteps = goals.getLong("steps");
				long goalMinutes = goals.getLong("activeMinutes");
				
				if (distance >= goalDistance)
					metGoals.add(this._context.getString(R.string.feat_fitbit_distance));

				if (steps >= goalSteps)
					metGoals.add(this._context.getString(R.string.feat_fitbit_steps));

				if (veryActive >= goalMinutes)
					metGoals.add(this._context.getString(R.string.feat_fitbit_minutes));
			} 
			catch (JSONException e) 
			{
     			LogManager.getInstance(me._context).logException(e);
			}
			catch (OAuthException e)
			{
     			LogManager.getInstance(me._context).logException(e);
			}
			catch (IllegalArgumentException e)
			{
     			LogManager.getInstance(me._context).logException(e);
			}
		}
		
		return metGoals;
	}

	private Set<String> todayJawboneGoals()
	{
		Set<String> metGoals = new HashSet<String>();
		
		final ScheduleManager me = this;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		if (prefs.getBoolean("settings_jawbone_enabled", false))
		{
			try
			{
				String token = prefs.getString("oauth_jawbone_token", "");
	
				AndroidHttpClient androidClient = AndroidHttpClient.newInstance("Intellicare", this._context);
	
		        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
	
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	
				SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
				registry.register(new Scheme("https", socketFactory, 443));
	
				HttpParams params = androidClient.getParams();
				HttpConnectionParams.setConnectionTimeout(params, 180000);
				HttpConnectionParams.setSoTimeout(params, 180000);
	
				ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(params, registry);
				HttpClient httpClient = new DefaultHttpClient(mgr, params);
	
				HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
				
				HttpGet httpGet = new HttpGet("https://jawbone.com/nudge/api/v.1.1/users/@me/goals");
				httpGet.addHeader("Authorization", "Bearer " + token);
				httpGet.addHeader("Accept", "application/json");
				httpGet.addHeader("X-Target-URI", "https://jawbone.com");
				httpGet.addHeader("X-HostCommonName", "jawbone.com");
	
				HttpResponse response = httpClient.execute(httpGet);

				HttpEntity httpEntity = response.getEntity();

				String result = EntityUtils.toString(httpEntity);
				
				JSONObject body = new JSONObject(result);
				
				int stepGoal = body.getJSONObject("data").getInt("move_steps");

				httpGet = new HttpGet("https://jawbone.com/nudge/api/v.1.1/users/@me/moves");
				httpGet.addHeader("Authorization", "Bearer " + token);
				httpGet.addHeader("Accept", "application/json");
				httpGet.addHeader("X-Target-URI", "https://jawbone.com");
				httpGet.addHeader("X-HostCommonName", "jawbone.com");
	
				response = httpClient.execute(httpGet);

				httpEntity = response.getEntity();

				result = EntityUtils.toString(httpEntity);
				
				body = new JSONObject(result);
				
				int steps = body.getJSONObject("data").getJSONArray("items").getJSONObject(0).getJSONObject("details").getInt("steps");
				
				Log.e("DF", "JAWBONE: " + steps + " / " + stepGoal);
				
				if (steps >= stepGoal)
					metGoals.add(this._context.getString(R.string.feat_jawbone_steps));
				
				androidClient.close();
			} 
			catch (JSONException e) 
			{
     			LogManager.getInstance(me._context).logException(e);
			}
			catch (OAuthException e)
			{
     			LogManager.getInstance(me._context).logException(e);
			}
			catch (IllegalArgumentException e)
			{
     			LogManager.getInstance(me._context).logException(e);
			} 
			catch (UnsupportedEncodingException e) 
			{
     			LogManager.getInstance(me._context).logException(e);
			} 
			catch (ClientProtocolException e) 
			{
     			LogManager.getInstance(me._context).logException(e);
			} 
			catch (IOException e) 
			{
     			LogManager.getInstance(me._context).logException(e);
			}
		}
		
		return metGoals;
	}

	@SuppressLint("SimpleDateFormat")
	private JSONArray todayCommits()
	{
        JSONArray commits = new JSONArray();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		if (prefs.getBoolean("settings_github_enabled", false))
		{
			if (prefs.contains("oauth_github_token"))
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String today = sdf.format(new Date());
				
				String token = prefs.getString("oauth_github_token", "");
				
				String userUri = "https://api.github.com/user?access_token=" + token;
				
				try 
				{
					URL url = new URL(userUri);
					
			        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			        String inputLine = null;
			        StringBuffer all = new StringBuffer();
			        
			        while ((inputLine = in.readLine()) != null)
			        {
			        	all.append(inputLine);
			        	all.append("\n");
			        }

			        in.close();
			        
			        JSONObject userInfo = new JSONObject(all.toString());
			        
			        String username = userInfo.getString("login");
			        
			        if (username != null)
			        {
			        	url = new URL("https://api.github.com/users/" + username + "/events?access_token=" + token);

				        in = new BufferedReader(new InputStreamReader(url.openStream()));

				        all = new StringBuffer();
				        
				        while ((inputLine = in.readLine()) != null)
				        {
				        	all.append(inputLine);
				        	all.append("\n");
				        }

				        in.close();
				        
				        JSONArray events = new JSONArray(all.toString());
				        
				        for (int i = 0; i < events.length(); i++)
				        {
				        	JSONObject event = events.getJSONObject(i);
				        	
				        	if ("PushEvent".equalsIgnoreCase(event.getString("type")))
				        	{
				        		if (event.getString("created_at").startsWith(today))
				        		{
				        			JSONArray commitObjects = event.getJSONObject("payload").getJSONArray("commits");
				        			
				        			for (int j = 0; j < commitObjects.length(); j++)
				        			{
				        				commits.put(commitObjects.getJSONObject(j));
				        			}
				        		}
				        	}
				        }
			        }
				} 
				catch (MalformedURLException e) 
				{
					LogManager.getInstance(this._context).logException(e);
				} 
				catch (IOException e) 
				{
					LogManager.getInstance(this._context).logException(e);
				} 
				catch (JSONException e) 
				{
					LogManager.getInstance(this._context).logException(e);
				}
			}
		}
		
		return commits;
	}
}
