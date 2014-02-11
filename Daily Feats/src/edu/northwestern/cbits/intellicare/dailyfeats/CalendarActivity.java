package edu.northwestern.cbits.intellicare.dailyfeats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
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
import edu.emory.mathcs.backport.java.util.Collections;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.dailyfeats.views.CalendarView;

public class CalendarActivity extends ConsentedActivity 
{
	private Date _currentDate = null;
	
	@SuppressLint("SimpleDateFormat")
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
				SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy");
				me.getSupportActionBar().setTitle(sdf.format(date));

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
				int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);
				int streak = FeatsProvider.streakForLevel(me, level);

				me.getSupportActionBar().setSubtitle(me.getString(R.string.title_level_streak, level, streak));
				
				me._currentDate = date;
				
				me.reloadList();
			}
		});

        calendar.setDate(new Date());
    }
	
	public void onResume()
	{
		super.onResume();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);
		
		Log.e("DF", "STREAK: " + FeatsProvider.streakForLevel(this, level));
		
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
			
	        final CalendarView calendar = (CalendarView) this.findViewById(R.id.view_calendar);

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
							
							calendar.setDate(new Date(), false);
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
			
			String[] matrixColumns = { "_id", "feat_name", "feat_level" };

			ArrayList<Object[]> rows = new ArrayList<Object[]>();
			
			while (featsCursor.moveToNext())
			{
				String selection = "feat_name = ?";
				String[] selectionArgs = { featsCursor.getString(featsCursor.getColumnIndex("feat")) };
				
				Cursor itemCursor = this.getContentResolver().query(FeatsProvider.FEATS_URI, matrixColumns, selection, selectionArgs, null);
				
				if (itemCursor.moveToNext())
				{
					Object[] row = { itemCursor.getLong(itemCursor.getColumnIndex("_id")), itemCursor.getString(itemCursor.getColumnIndex("feat_name")), itemCursor.getInt(itemCursor.getColumnIndex("feat_level")) } ;
					
					rows.add(row);
				}
				else
				{
					Object[] row = { System.currentTimeMillis(), selectionArgs[0], 0 } ;
					
					rows.add(row);
				}
				
				itemCursor.close();
			}
			
			featsCursor.close();

			Collections.sort(rows, new Comparator<Object[]>()
			{
				public int compare(Object[] lhs, Object[] rhs) 
				{
					Integer left = (Integer) lhs[2];
					Integer right = (Integer) rhs[2];
					
					return left.compareTo(right);
				}
			});
			
			MatrixCursor cursor = new MatrixCursor(matrixColumns);
			
			for (Object[] row : rows)
				cursor.addRow(row);

			cursor.moveToFirst();
			
			this.startManagingCursor(cursor);

			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_feat_review, cursor, new String[0], new int[0], 0)
			{
				public void bindView(View view, Context context, Cursor cursor)
				{
					TextView feat = (TextView) view.findViewById(R.id.feat_name);
					
					final String featName = cursor.getString(cursor.getColumnIndex("feat_name"));
					
					feat.setText(featName);
					
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
			featsList.setEmptyView(this.findViewById(R.id.empty_list));
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
		else if (itemId == R.id.action_rules)
		{
			Intent rulesIntent = new Intent(this, RulesActivity.class);
			this.startActivity(rulesIntent);
		}
		else if (itemId == R.id.action_settings)
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		
		return true;
	}
}
