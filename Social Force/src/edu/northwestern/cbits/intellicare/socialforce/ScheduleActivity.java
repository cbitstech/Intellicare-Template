package edu.northwestern.cbits.intellicare.socialforce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ScheduleActivity extends ConsentedActivity 
{
	protected static final String CONTACT_KEY = "contact_key";

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
        
		actionBar.setTitle("tOdO: Schedule ActiviTY");
        
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
    
    private void scheduleActivity() 
    {
        final ScheduleActivity me = this;
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("What CoULD you MaKE TimE For THis WeeK?");
		
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
		    		builder.setTitle("What WOUlD YOu LIKe To DO?");
		    		
		    		final String[] meetUpActivities = me.getActivities();
		    		
		    		builder.setItems(meetUpActivities, new DialogInterface.OnClickListener()
		    		{
						public void onClick(DialogInterface dialog, int which) 
						{
							if (which == meetUpActivities.length - 1) // Other
							{
								// TODO: Show Other Dialog
							}
							else
							{
								me._meetingActivity = meetUpActivities[which];
								
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
			
			intent.putExtra(Events.TITLE, this._activity.replace(".", ": " + this._contact.name));
			
			if (this._meetingActivity != null)
				intent.putExtra(Events.DESCRIPTION, this._meetingActivity);
			
			this.startActivity(intent);
			
			this.finish();

    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }

	protected String[] getActivities() 
	{
		return this.getResources().getStringArray(R.array.meetup_activities);
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
