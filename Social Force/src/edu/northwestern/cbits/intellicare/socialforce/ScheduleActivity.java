package edu.northwestern.cbits.intellicare.socialforce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ScheduleActivity extends ConsentedActivity 
{
	protected static final String CONTACT_KEY = "contact_key";
	protected static final String SAVED_ACTIVITIES = "saved_activities";
	private static final int ADD_CALENDAR = 51465;

	private Menu _menu = null;
	
	private String _activity = null;
	private String _meetingActivity = null;
	private ContactRecord _contact = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_schedule);
        
        final ScheduleActivity me = this;
        
        final ActionBar actionBar = this.getSupportActionBar();
        
		actionBar.setTitle(R.string.title_schedule);
        
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        pager.setOffscreenPageLimit(0);
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 2;
			}

			public boolean isViewFromObject(View view, Object content) 
			{
				return view.getTag().equals(content);
			}
			
			public void destroyItem (ViewGroup container, int position, Object content)
			{
				int toRemove = -1;
				
				for (int i = 0; i < container.getChildCount(); i++)
				{
					View child = container.getChildAt(i);
					
					if (this.isViewFromObject(child, content))
						toRemove = i;
				}
				
				if (toRemove >= 0)
					container.removeViewAt(toRemove);
			}
			
			public Object instantiateItem (ViewGroup container, int position)
			{
				View view = null;
				
				switch (position)
				{
					case 0:
						WebView webViewZero = new WebView(container.getContext());
						
						webViewZero.loadUrl("file:///android_asset/www/schedule_0.html");

						view = webViewZero;
						break;
					case 1:
						WebView webViewOne = new WebView(container.getContext());

						webViewOne.setId(Integer.MAX_VALUE);

						webViewOne.setTag("1");

						view = webViewOne;

						break;
				}

				view.setTag("" + position);

				container.addView(view);

				LayoutParams layout = (LayoutParams) view.getLayoutParams();
				layout.height = LayoutParams.MATCH_PARENT;
				layout.width = LayoutParams.MATCH_PARENT;
				
				view.setLayoutParams(layout);

				return view.getTag();
			}
		};
		
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(new OnPageChangeListener()
		{
			public void onPageScrollStateChanged(int arg0) 
			{

			}

			public void onPageScrolled(int arg0, float arg1, int arg2) 
			{

			}

			public void onPageSelected(int page) 
			{
				if (me._menu != null)
				{
			        MenuItem schedule = me._menu.findItem(R.id.action_schedule);

					switch(page)
					{
						case 0:
					        schedule.setVisible(false);
							break;
						case 1:
							WebView webView = (WebView) me.findViewById(Integer.MAX_VALUE);
							
							webView.loadDataWithBaseURL("file:///android_asset/", me.generateSummary(), "text/html", null, null);

					        schedule.setVisible(true);
							break;
					}
				}
			}
		});
		
		pager.setCurrentItem(0, false);
		
        Intent intent = this.getIntent();
        
        if (intent.hasExtra(ScheduleActivity.CONTACT_KEY))
		{
        	String key = intent.getStringExtra(ScheduleActivity.CONTACT_KEY);
        	
        	List<ContactRecord> records = ContactCalibrationHelper.fetchContactRecords(this);
        	
        	for (ContactRecord record : records)
        	{
        		if (key.equals(record.key))
        			this._contact = record;
        	}
		}
        
        if (this._contact != null)
        	this.scheduleActivity();
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	if (this._contact == null)
    	{
	    	final ScheduleActivity me = this;
	    	
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle(R.string.title_select_supporter);
			
			List<ContactRecord> contacts = ContactCalibrationHelper.fetchContactRecords(this);
			
			final List<ContactRecord> positives = new ArrayList<ContactRecord>();
			
			for (ContactRecord record : contacts)
			{
				if (record.level >= 0 && record.level < 3)
					positives.add(record);
			}
			
			Collections.sort(positives, new Comparator<ContactRecord>()
			{
				public int compare(ContactRecord one, ContactRecord two) 
				{
					if (one.level < two.level)
						return -1;
					else if (one.level > two.level)
						return 1;
					
					return one.name.compareTo(two.name);
				}
			});
	
			String[] names = new String[positives.size()];
			
			for (int i = 0; i < positives.size(); i++)
			{
				ContactRecord record = positives.get(i);
				
				names[i] = record.name;
			}
			
			builder.setItems(names, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					me._contact = positives.get(which);
					
					me.scheduleActivity();
				}
			});
			
			builder.setOnCancelListener(new OnCancelListener()
			{
				public void onCancel(DialogInterface arg0) 
				{
					me.finish();
				}
			});
			
			builder.create().show();
    	}
    }
    
    private void scheduleActivity() 
    {
        final ScheduleActivity me = this;
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
		builder.setTitle(R.string.title_make_time);
		
		final String[] activities = this.getResources().getStringArray(R.array.positive_activities);
		
		builder.setItems(activities, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, final int which) 
			{
				me._activity = activities[which];
				
				if (which != 0)
					pager.setCurrentItem(1);
				else
				{
		    		AlertDialog.Builder builder = new AlertDialog.Builder(me);
		    		builder.setTitle(R.string.title_select_activity);
		    		
		    		final String[] meetUpActivities = me.getActivities();
		    		
		    		builder.setItems(meetUpActivities, new DialogInterface.OnClickListener()
		    		{
						public void onClick(DialogInterface dialog, int which) 
						{
							if (which == meetUpActivities.length - 1) // Other
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(me);
								builder.setTitle(R.string.title_new_activity);

								LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								View view = inflater.inflate(R.layout.view_new_activity, null);
								
								final AutoCompleteTextView activity = (AutoCompleteTextView) view.findViewById(R.id.new_activity);
								
								String[] activities = me.getActivities();
								
								String[] newActivities = new String[activities.length - 1];
								
								for (int i = 0; i < newActivities.length; i++)
								{
									newActivities[i] = activities[i];
								}
								
								ArrayAdapter<String> adapter = new ArrayAdapter<String>(me, android.R.layout.simple_dropdown_item_1line, newActivities);
								activity.setAdapter(adapter);
								
								builder.setView(view);
								
								builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{		
										String newActivity = activity.getText().toString();
										
										if (newActivity.trim().length() > 0)
										{
											SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
											
											String json = prefs.getString(ScheduleActivity.SAVED_ACTIVITIES, "[]");

											JSONArray activityArray = new JSONArray();

											JSONObject foundObj = null;
											
											try 
											{
												activityArray = new JSONArray(json);

												for (int i = 0; i < activityArray.length() && foundObj == null; i++)
												{
													JSONObject obj = activityArray.getJSONObject(i);
													
													if (obj.get("name").equals(newActivity))
														foundObj = obj;
												}

												if (foundObj == null)
												{
													foundObj = new JSONObject();
													foundObj.put("name", newActivity);
													
													activityArray.put(foundObj);
												}
												
												foundObj.put("last_used", System.currentTimeMillis());
											}
											catch (JSONException e) 
											{
												LogManager.getInstance(me).logException(e);
											}
											
											Editor e = prefs.edit();
											e.putString(ScheduleActivity.SAVED_ACTIVITIES, activityArray.toString());
											e.commit();
											
											me._meetingActivity = newActivity;
										
											pager.setCurrentItem(1);
										}
									}
								});
								
								builder.create().show();
							}
							else
							{
								me._meetingActivity = meetUpActivities[which];
								
								pager.setCurrentItem(1);

								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
								
								String json = prefs.getString(ScheduleActivity.SAVED_ACTIVITIES, "[]");

								JSONArray activityArray = new JSONArray();

								JSONObject foundObj = null;
								
								try 
								{
									activityArray = new JSONArray(json);

									for (int i = 0; i < activityArray.length() && foundObj == null; i++)
									{
										JSONObject obj = activityArray.getJSONObject(i);
										
										if (obj.get("name").equals(me._meetingActivity))
											foundObj = obj;
									}

									if (foundObj == null)
									{
										foundObj = new JSONObject();
										foundObj.put("name", me._meetingActivity);
										
										activityArray.put(foundObj);
									}
									
									foundObj.put("last_used", System.currentTimeMillis());
								}
								catch (JSONException e) 
								{
									LogManager.getInstance(me).logException(e);
								}
								
								Editor e = prefs.edit();
								e.putString(ScheduleActivity.SAVED_ACTIVITIES, activityArray.toString());
								e.commit();
								
								pager.setCurrentItem(1);
							}							
						}
		    		});
		    		
		    		builder.create().show();
				}
			}
		});
		
		builder.create().show();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_schedule, menu);
        
        this._menu = menu;
        
        MenuItem schedule = this._menu.findItem(R.id.action_schedule);
        schedule.setVisible(false);

        return true;
    }

    @SuppressLint("InlinedApi") public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if (item.getItemId() == R.id.action_next)
    	{
    		this.scheduleActivity();
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_schedule)
    	{
    		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    		{
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setTitle(R.string.title_calendar_error);
    			builder.setMessage(R.string.message_calendar_error);
    			
    			builder.setPositiveButton(R.string.action_close, new OnClickListener()
    			{
    				public void onClick(DialogInterface arg0, int arg1) 
    				{

    				}
    			});
    			
    			builder.create().show();
    			
    			return true;
    		}
    		
			Intent intent = new Intent(Intent.ACTION_INSERT);
			intent.setData(Events.CONTENT_URI);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			
			intent.putExtra(Events.TITLE, this._activity.replace(".", ": " + this._contact.name));
			
			if (this._meetingActivity != null)
				intent.putExtra(Events.DESCRIPTION, this._meetingActivity + " " + this.getString(R.string.suffix_via_social_force));
			else
				intent.putExtra(Events.DESCRIPTION, this.getString(R.string.suffix_via_social_force));
			
			this.startActivityForResult(intent, ScheduleActivity.ADD_CALENDAR);

    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
		String selection = CalendarContract.Events.DESCRIPTION + " LIKE ?";
		String[] args = { "%" + this.getString(R.string.suffix_via_social_force) + "%" };

		Cursor c = this.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, args, CalendarContract.Events._ID + " DESC");
		
		if (c.moveToNext())
		{
			ContentValues values = new ContentValues();
			
			values.put(CalendarContract.Attendees.EVENT_ID, c.getLong(c.getColumnIndex(CalendarContract.Events._ID)));
			values.put(CalendarContract.Attendees.ATTENDEE_NAME, this._contact.name);
			values.put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_NONE);
			values.put(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP, CalendarContract.Attendees.RELATIONSHIP_NONE);
			values.put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED);
			values.put(CalendarContract.Attendees.ATTENDEE_EMAIL, "");
			
			this.getContentResolver().insert(CalendarContract.Attendees.CONTENT_URI, values);
		}
		
		c.close();
		
		this.finish();

    	super.onActivityResult(requestCode, resultCode, data);
    }

	protected String[] getActivities() 
	{
		final ScheduleActivity me = this;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String json = prefs.getString(ScheduleActivity.SAVED_ACTIVITIES, "[]");

		JSONArray activityArray = new JSONArray();
		
		ArrayList<String> activityNames = new ArrayList<String>();
		
		try 
		{
			activityArray = new JSONArray(json);
			
			if (activityArray.length() == 0)
			{
				String[] activities = this.getResources().getStringArray(R.array.meetup_activities);

				for (String activity : activities)
				{
					JSONObject jsonObj = new JSONObject();

					jsonObj.put("name", activity);
					jsonObj.put("last_used", 0L);
					
					activityArray.put(jsonObj);
				}
				
				Editor e = prefs.edit();
				e.putString(ScheduleActivity.SAVED_ACTIVITIES, activityArray.toString());
				e.commit();
			}
			
			ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
			
			for (int i = 0; i < activityArray.length(); i++)
			{
				objs.add(activityArray.getJSONObject(i));
			}
			
			Collections.sort(objs, new Comparator<JSONObject>()
			{
				public int compare(JSONObject one, JSONObject two) 
				{
					try 
					{
						if (one.getLong("last_used") > two.getLong("last_used"))
							return -1;
						else if (one.getLong("last_used") < two.getLong("last_used"))
							return 1;
					
						return one.getString("name").compareToIgnoreCase(two.getString("name"));
					} 
					catch (JSONException e) 
					{
						LogManager.getInstance(me).logException(e);
					}
					
					return one.toString().compareToIgnoreCase(two.toString());
				}
			});
			
			for (JSONObject obj : objs)
			{
				activityNames.add(obj.getString("name"));
			}
		}
		catch (JSONException e) 
		{
			LogManager.getInstance(this).logException(e);
		}
		
		activityNames.add(this.getString(R.string.activity_other));
		
		return activityNames.toArray(new String[0]);
	}
	
	private String generateSummary() 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = this.getAssets().open("www/schedule_1.html");

		    BufferedReader in = new BufferedReader(new InputStreamReader(html));

		    String str = null;

		    while ((str = in.readLine()) != null) 
		    {
		    	buffer.append(str);
		    	buffer.append(System.getProperty("line.separator"));
		    }

		    in.close();
		} 
		catch (IOException e) 
		{
			LogManager.getInstance(this).logException(e);
		}

		String summary = buffer.toString();
		
		summary = summary.replace("[[ CONTACT ]]", this._contact.name);
		
		if (this._activity != null)
		{
			if (this._meetingActivity == null)
				summary = summary.replace("[[ ACTIVITY ]]", this._activity);
			else
				summary = summary.replace("[[ ACTIVITY ]]", this._meetingActivity);
		}
		
		return summary;
	}

}
