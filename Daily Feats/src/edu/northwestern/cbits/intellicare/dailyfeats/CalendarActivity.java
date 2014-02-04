package edu.northwestern.cbits.intellicare.dailyfeats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.dailyfeats.views.CalendarView;

public class CalendarActivity extends ConsentedActivity 
{
	private Date _currentDate = null;
	
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_calendar);
        
        final CalendarActivity me = this;
        
        CalendarView calendar = (CalendarView) this.findViewById(R.id.view_calendar);
        
        calendar.setOnDateChangeListener(new CalendarView.DateChangeListener() 
        {
			public void onDateChanged(Date date) 
			{
				Log.e("DF", "DATE: " + date);
				
				SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy");
				me.getSupportActionBar().setTitle(sdf.format(date));
				me.getSupportActionBar().setSubtitle(R.string.app_name);
				
				me._currentDate = date;
				
				me.reloadList();
			}
		});

        calendar.setDate(new Date());
    }
	
	public void onResume()
	{
		super.onResume();
		
		if (this._currentDate == null)
			this._currentDate = new Date();
		
        CalendarView calendar = (CalendarView) this.findViewById(R.id.view_calendar);
        calendar.setDate(this._currentDate);
        
        this.reloadList();
	}
	
	@SuppressWarnings("deprecation")
	protected void reloadList() 
	{
		ListView featsList = (ListView) this.findViewById(R.id.list_feats);
		
		DateFormat formatter = android.text.format.DateFormat.getDateFormat(this);
		
		String todayFormatted = formatter.format(new Date());
		String dateFormatted = formatter.format(this._currentDate);
		
		boolean isToday = todayFormatted.equals(dateFormatted);
		
		final CalendarActivity me = this;
		
		if (isToday)
		{
			String where = "enabled = ?";
			String[] args = { "1" };
			
			Cursor featsCursor = this.getContentResolver().query(FeatsProvider.FEATS_URI, null, where, args, "feat_level, feat_name");
			this.startManagingCursor(featsCursor);

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_feat_checkbox, featsCursor, new String[0], new int[0], 0)
			{
				public void bindView(View view, Context context, Cursor cursor)
				{
					CheckBox check = (CheckBox) view.findViewById(R.id.feat_checkbox);
					
					final String featName = cursor.getString(cursor.getColumnIndex("feat_name"));
					
					check.setText(featName);
					
					check.setOnCheckedChangeListener(new OnCheckedChangeListener()
					{
						public void onCheckedChanged(CompoundButton arg0, boolean checked) 
						{

						}
					});
					
					check.setChecked(FeatsProvider.hasFeatForDate(context, featName, me._currentDate));

					check.setOnCheckedChangeListener(new OnCheckedChangeListener()
					{
						public void onCheckedChanged(CompoundButton arg0, boolean checked) 
						{
							if (checked)
							{
								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

								int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

						        FeatsProvider.createFeat(me, featName, level);
							}
							else
						        FeatsProvider.clearFeats(me, featName, me._currentDate);
						}
					});

					int featLevel = cursor.getInt(cursor.getColumnIndex("feat_level"));
					
					TextView categoryLabel = (TextView) view.findViewById(R.id.label_category_name);

					if (featLevel != 0)
						categoryLabel.setText(context.getString(R.string.label_category, featLevel));
					else
						categoryLabel.setText(R.string.label_category_my_feats);

					categoryLabel.setVisibility(View.GONE);

					if (cursor.moveToPrevious() == false)
						categoryLabel.setVisibility(View.VISIBLE);
					else
					{
						int nextLevel = cursor.getInt(cursor.getColumnIndex("feat_level"));
						
						if (featLevel != nextLevel)
							categoryLabel.setVisibility(View.VISIBLE);
						
						cursor.moveToNext();
					}
				}
			};
			
			featsList.setAdapter(adapter);
		}
		else
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this._currentDate);
			
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			Date start = calendar.getTime();

			calendar.set(Calendar.HOUR, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.MILLISECOND, 999);

			Date end = calendar.getTime();
			
			String where = "recorded >= ? AND recorded <= ?";
			String[] args = { "" + start.getTime(), "" + end.getTime() };
			
			Cursor featsCursor = this.getContentResolver().query(FeatsProvider.RESPONSES_URI, null, where, args, "depression_level, feat");
			this.startManagingCursor(featsCursor);

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_feat_count, featsCursor, new String[0], new int[0], 0)
			{
				public void bindView (View view, Context context, Cursor cursor)
				{
					TextView featName = (TextView) view.findViewById(R.id.feat_label);
					featName.setText(cursor.getString(cursor.getColumnIndex("feat")));
				}
			};
			
			featsList.setAdapter(adapter);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_calendar, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		if (itemId == R.id.action_today)
		{
	        CalendarView calendar = (CalendarView) this.findViewById(R.id.view_calendar);
	        calendar.setDate(new Date());
		}
		else if (itemId == R.id.action_edit_feats)
		{
			Intent editIntent = new Intent(this, EditFeatsChecklistActivity.class);
			this.startActivity(editIntent);
		}
		
		return true;
	}
}
