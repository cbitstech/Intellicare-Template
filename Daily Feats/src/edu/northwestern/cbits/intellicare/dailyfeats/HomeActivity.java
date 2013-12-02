package edu.northwestern.cbits.intellicare.dailyfeats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

/**
 * Created by Gabe on 9/16/13.
 */
public class HomeActivity extends ConsentedActivity 
{
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_home);
    }

    @SuppressLint("SimpleDateFormat")
	protected void onResume() 
    {
        super.onResume();

        TextView days = (TextView) this.findViewById(R.id.days_count);
        days.setText("" + this.dayStreakCount());
        
        TextView today = (TextView) this.findViewById(R.id.today_count);
        today.setText("" + this.todayCount());
        
        this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_next_reminder, this.getReminderTimeString()));

        ArrayList<ContentValues> feats = new ArrayList<ContentValues>();
        
        Cursor c = this.getContentResolver().query(FeatsProvider.FEATS_URI, null, null, null, "feat_level, feat_name");
        
        while (c.moveToNext())
        {
        	String featName = c.getString(c.getColumnIndex("feat_name"));

        	ContentValues feat = new ContentValues();
        	
        	feat.put("feat_name", featName);
        	feat.put("feat_level", c.getInt(c.getColumnIndex("feat_level")));
        	
        	String featSelection = "feat = ?";
        	String[] featArgs = { featName };
        	
        	Cursor featCursor = this.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, featSelection, featArgs, null);
        	
        	feat.put("feat_count", featCursor.getCount());
        	
        	featCursor.close();
        	
        	feats.add(feat);
        }
        
        c.close();
        
        Collections.sort(feats, new Comparator<ContentValues>()
		{
			public int compare(ContentValues one, ContentValues two) 
			{
				int count = two.getAsInteger("feat_count").compareTo(one.getAsInteger("feat_count"));
				
				if (count != 0)
					return count;
				
				return one.getAsString("feat_name").compareTo(two.getAsString("feat_name"));
			}
		});

        ArrayAdapter<ContentValues> featsAdapter = new ArrayAdapter<ContentValues>(this, R.layout.row_feat_count, feats) 
		{
            public View getView (int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.row_feat_count, null);
                }

                ContentValues feat = this.getItem(position);

                TextView countView = (TextView) convertView.findViewById(R.id.feat_count);
                countView.setText(feat.getAsString("feat_count").trim());

                TextView featLabel = (TextView) convertView.findViewById(R.id.feat_label);
                featLabel.setText(feat.getAsString("feat_name").trim());

                return convertView;
            }

        };

        ListView featsList = (ListView) this.findViewById(R.id.feats_list);
        featsList.setAdapter(featsAdapter);
        
        TextView featText = (TextView) this.findViewById(R.id.most_recent_feat_text);
        
        Feat feat = this.mostRecentFeat();
        
        if (feat != null)
        {
        	SimpleDateFormat sdf = new SimpleDateFormat("EEEE, h:mm a");
        	
        	featText.setText(this.getString(R.string.recent_feat, feat.name, sdf.format(new Date(feat.recorded))));
        }
        else
        	featText.setText(R.string.no_feats_yet);
    }

	protected Feat mostRecentFeat() 
    {
    	Cursor c = this.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, null, null, "recorded DESC");

    	Feat f = null;
    	
    	if (c.moveToNext())
    	{
    		f = new Feat();
    		f.recorded = c.getLong(c.getColumnIndex("recorded"));
    		f.name = c.getString(c.getColumnIndex("feat"));
    	}

    	c.close();
    	
    	return f;
    }

    private int todayCount() 
    {
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.HOUR_OF_DAY, 0);
    	c.set(Calendar.MINUTE, 0);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
    	
    	String selection = "recorded >= ?";
    	String[] args = { "" + c.getTimeInMillis() };
    	
    	int count = 0;
    	
    	Cursor cursor = this.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, selection, args, null);
    	
    	count = cursor.getCount();
    	
    	cursor.close();
 
		return count;
	}

	private int dayStreakCount() 
	{
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.HOUR_OF_DAY, 0);
    	c.set(Calendar.MINUTE, 0);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
    	
    	String selection = "recorded >= ? AND recorded <= ?";
    	
    	long time = c.getTimeInMillis();
    	
    	String[] args = { "" + time, "" + (time  + (24 * 60 * 60 * 1000))};
    	
    	int count = 0;
    	
    	Cursor cursor = this.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, selection, args, null);
    	
    	while (cursor.getCount() > 0)
    	{
    		cursor.close();

    		count += 1;
    		
    		time -= (24 * 60 * 60 * 1000);
    		
    		args[0] = "" + time;
    		args[1] = "" + (time  + (24 * 60 * 60 * 1000));

    		cursor = this.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, selection, args, null);
    	}
    	
    	cursor.close();
 
		return count;
	}

	/**
     *  getReminderTimeString
     *  duplicated between Setup Activity and HomeActivity
     *  out of expediency, and not sure how to share functions that depend on Android Context
     *  between Activities
     *  -Gabe
     **/
    private String getReminderTimeString() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String amPM;
        String minutes;
        int reminderHour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
        int reminderMins = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

        if (reminderHour > 12) {
            amPM = "PM";
            reminderHour = reminderHour - 12;
        }
        else {
            amPM = "AM";
        }

        if (reminderMins < 10) {
            minutes = "0"+String.valueOf(reminderMins);
        }
        else {
            minutes = String.valueOf(reminderMins);
        }

        return String.valueOf(reminderHour)+":"+minutes+" "+amPM;
    }

    
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_home, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_time:
				final HomeActivity me = this;
				
		        DialogFragment timeFragment = new TimePickerFragment(new OnDismissListener()
		        {
					public void onDismiss(DialogInterface dialog) 
					{
				        me.getSupportActionBar().setSubtitle(me.getString(R.string.subtitle_next_reminder, me.getReminderTimeString()));
					}
		        });
		        
		        timeFragment.show(this.getSupportFragmentManager(), "timePicker");

				break;
			case R.id.action_checkin:
				this.startActivity(new Intent(this, ChecklistActivity.class));
				
				break;
		}

		return true;
	}

	private class Feat
	{
		public String name = null;
		public long recorded = 0;
	}
}