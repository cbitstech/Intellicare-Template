package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.ArrayList;

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
        	ContentValues feat = new ContentValues();
        	
        	feat.put("feat_name", c.getString(c.getColumnIndex("feat_name")));
        	feat.put("feat_level", c.getInt(c.getColumnIndex("feat_level")));
        	
        	feats.add(feat);
        }
        
        c.close();

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
                countView.setText(feat.getAsString("feat_level").trim());

                TextView featLabel = (TextView) convertView.findViewById(R.id.feat_label);
                featLabel.setText(feat.getAsString("feat_name").trim());

                return convertView;
            }

        };

        ListView featsList = (ListView) this.findViewById(R.id.feats_list);
        featsList.setAdapter(featsAdapter);
    }

    private int todayCount() 
    {
		return -1;
	}

	private int dayStreakCount() 
	{
		return -1;
	}

	/**
     *  getReminderTimeString
     *  duplicated between Setup Activity and HomeActivity
     *  out of expediency, and not sure how to share functions that depend on Android Context
     *  between Activities
     *  -Gabe
     **/
    private String getReminderTimeString() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String amPM;
        String minutes;
        int reminderHour = prefs.getInt(AppConstants.REMINDER_HOUR,    AppConstants.DEFAULT_HOUR);
        int reminderMins = prefs.getInt(AppConstants.REMINDER_MINUTE, AppConstants.DEFAULT_MINUTE);

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
				this.startActivity(new Intent(this, FeatsChecklistActivity.class));
				
				break;
		}

		return true;
	}

}