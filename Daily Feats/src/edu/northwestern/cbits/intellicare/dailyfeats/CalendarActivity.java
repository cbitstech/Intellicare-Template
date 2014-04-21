package edu.northwestern.cbits.intellicare.dailyfeats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.dailyfeats.views.CalendarView;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class CalendarActivity extends ConsentedActivity 
{
	private static final String APP_ID = "b1fd44d7db88602fa8185ac896a153b1";
	private Date _currentDate = null;
	
	@SuppressLint("SimpleDateFormat")
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_calendar);
        
        final CalendarActivity me = this;

        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
//        pager.setOffscreenPageLimit(0);

		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return FeatsProvider.fetchMonthCount(me);
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
			
			public Object instantiateItem(ViewGroup container, int position)
			{
				Date date = FeatsProvider.dateForMonthIndex(me, position);
				
				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View view = inflater.inflate(R.layout.view_calendar_all, null, false);
				
		        CalendarView calendar = (CalendarView) view.findViewById(R.id.view_calendar);
		        calendar.setDate(date);

		        calendar.setOnDateChangeListener(new CalendarView.DateChangeListener() 
		        {
					public void onDateChanged(Date date) 
					{
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
						
						me._currentDate = date;
						
						me.reloadList(view);

						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("date", date.getTime());
						payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
						
						LogManager.getInstance(me).log("changed_date", payload);
					}
				});
				
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

			public void onPageSelected(final int page) 
			{
				final Date date = FeatsProvider.dateForMonthIndex(me, page);

				final ViewPager pager = (ViewPager) me.findViewById(R.id.pager_content);
				View content = pager.findViewWithTag("" + pager.getCurrentItem());
				
				SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy");
				me.getSupportActionBar().setTitle(sdf.format(date));
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
				int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);
				int streak = FeatsProvider.streakForLevel(me, level);
				
				me.getSupportActionBar().setSubtitle(me.getString(R.string.title_level_streak, level, streak));

				if (content != null)
				{
					CalendarView calendar = (CalendarView) content.findViewById(R.id.view_calendar);
			        calendar.setDate(date);
				}
				else
				{
					Thread t = new Thread(new Runnable()
					{
						public void run() 
						{
							try 
							{
								Thread.sleep(250);
							} 
							catch (InterruptedException e) 
							{

							}
							
							me.runOnUiThread(new Runnable()
							{
								public void run() 
								{
									View content = pager.findViewWithTag("" + pager.getCurrentItem());

									if (content != null)
									{
										CalendarView calendar = (CalendarView) content.findViewById(R.id.view_calendar);
								        calendar.setDate(date);
									}
								}
							});
						}
					});
					
					t.start();
				}
			}
		});
    }
	
	public void onResume()
	{
		super.onResume();

		if (this._currentDate == null)
		{
			this._currentDate = new Date();

	        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

			pager.setCurrentItem(pager.getAdapter().getCount() - 2, true);
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("date", this._currentDate.getTime());
		payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
		LogManager.getInstance(this).log("opened_calendar", payload);

		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});
	}
	
	public void onPause()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
		LogManager.getInstance(this).log("closed_calendar", payload);
		
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	protected void reloadList(View content) 
	{
		ListView listView = (ListView) content.findViewById(R.id.list_feats);
		DateFormat formatter = android.text.format.DateFormat.getDateFormat(this);
		
		String todayFormatted = formatter.format(new Date());
		String dateFormatted = formatter.format(this._currentDate);
		
		boolean isToday = todayFormatted.equals(dateFormatted);
		
		final CalendarActivity me = this;
		
		if (isToday)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			String where = "enabled = ?";
			String[] args = { "1" };
			
			if (prefs.getBoolean("settings_full_mode", true) == false)
			{
				int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

				args[0] = "3";

				if (level < 3)
					where = "feat_level < ?";
				else if (level == 3)
					where = "feat_level = ?";
				else
					where = "feat_level >= ?";
			}
						
			Cursor featsCursor = this.getContentResolver().query(FeatsProvider.FEATS_URI, null, where, args, "feat_level, feat_name");
			this.startManagingCursor(featsCursor);
			
	        final CalendarView calendar = (CalendarView) content.findViewById(R.id.view_calendar);

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
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
							
							if (checked)
							{
								int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

						        FeatsProvider.createFeat(me, featName, level);
						        
								HashMap<String, Object> payload = new HashMap<String, Object>();
								payload.put("feat", featName);
								payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
								LogManager.getInstance(me).log("checked_feat", payload);
							}
							else
							{
								HashMap<String, Object> payload = new HashMap<String, Object>();
								payload.put("feat", featName);
								payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
								LogManager.getInstance(me).log("unchecked_feat", payload);

								FeatsProvider.clearFeats(me, featName, me._currentDate);
							}

							int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);
							int streak = FeatsProvider.streakForLevel(me, level);
							
							me.getSupportActionBar().setSubtitle(me.getString(R.string.title_level_streak, level, streak));

							calendar.setDate(me._currentDate, false);
						}
					});

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
					int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

					int featLevel = cursor.getInt(cursor.getColumnIndex("feat_level"));
					
					LinearLayout categoryRow = (LinearLayout) view.findViewById(R.id.label_category_row);
					TextView categoryLabel = (TextView) view.findViewById(R.id.label_category_name);
					TextView levelLabel = (TextView) view.findViewById(R.id.label_category_level);
					
					if (level == featLevel)
						levelLabel.setVisibility(View.VISIBLE);
					else
						levelLabel.setVisibility(View.GONE);

					check.setEnabled(true);
					
					if (featLevel == 0)
						categoryLabel.setText(R.string.label_category_my_feats);
					else if (featLevel == 99)
					{
						categoryLabel.setText(R.string.label_category_automatic);
						check.setEnabled(false);
					}
					else
						categoryLabel.setText(context.getString(R.string.label_category, featLevel));

					categoryRow.setVisibility(View.GONE);

					if (cursor.moveToPrevious() == false)
						categoryRow.setVisibility(View.VISIBLE);
					else
					{
						int nextLevel = cursor.getInt(cursor.getColumnIndex("feat_level"));
						
						if (featLevel != nextLevel)
							categoryRow.setVisibility(View.VISIBLE);
						
						cursor.moveToNext();
					}
				}
			};
			
			listView.setAdapter(adapter);
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
			
			long now = System.currentTimeMillis();
			
			while (featsCursor.moveToNext())
			{
				String selection = "feat_name = ?";
				String[] selectionArgs = { featsCursor.getString(featsCursor.getColumnIndex("feat")) };
				
				Cursor itemCursor = this.getContentResolver().query(FeatsProvider.FEATS_URI, matrixColumns, selection, selectionArgs, null);
				
				if (itemCursor.moveToNext())
				{
					boolean contains = false;
					
					String featName = itemCursor.getString(itemCursor.getColumnIndex("feat_name"));
					
					for (Object[] row : rows)
					{
						if (row[1].equals(featName))
							contains = true;
					}
					
					if (contains == false)
					{
						Object[] row = { itemCursor.getLong(itemCursor.getColumnIndex("_id")), featName, itemCursor.getInt(itemCursor.getColumnIndex("feat_level")) } ;
						rows.add(row);
					}
				}
				else
				{
					Object[] row = { now, selectionArgs[0], 0 } ;
					
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

					if (featLevel == 0)
						categoryLabel.setText(R.string.label_category_my_feats);
					else if (featLevel == 99)
						categoryLabel.setText(R.string.label_category_automatic);
					else
						categoryLabel.setText(context.getString(R.string.label_category, featLevel));

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
			
			listView.setAdapter(adapter);
			listView.setEmptyView(content.findViewById(R.id.empty_list));
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_calendar, menu);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.getBoolean("settings_full_mode", true) == false)
			menu.removeItem(R.id.action_edit_feats);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		if (itemId == R.id.action_today)
		{
	        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
	        pager.setCurrentItem(pager.getAdapter().getCount() - 2, true);
		}
		else if (itemId == R.id.action_edit_feats)
		{
			Intent editIntent = new Intent(this, EditFeatsChecklistActivity.class);
			this.startActivity(editIntent);
		}
		else if (itemId == R.id.action_active_streaks)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_active_streaks);
			
			ArrayList<FeatsProvider.FeatCount> feats = FeatsProvider.activeStreaks(this);
			
			if (feats.size() > 0)
			{
				ListView list = new ListView(this);
				
				ArrayAdapter<FeatsProvider.FeatCount> adapter = new ArrayAdapter<FeatsProvider.FeatCount>(this, R.layout.row_feat_counts, feats)
				{
		    		public View getView (int position, View convertView, ViewGroup parent)
		    		{
		    			Context context = parent.getContext();
		    			if (convertView == null)
		    			{
		    				LayoutInflater inflater = LayoutInflater.from(context);
		    				convertView = inflater.inflate(R.layout.row_feat_counts, parent, false);
		    			}
		    			
		    			FeatsProvider.FeatCount count = this.getItem(position);
		    			
		    			TextView title = (TextView) convertView.findViewById(android.R.id.text1);
		    			TextView subtitle = (TextView) convertView.findViewById(android.R.id.text2);
		    			
		    			title.setText(count.feat);
		    			
		    			if (count.count.intValue() == 1)
		    				subtitle.setText(context.getString(R.string.item_single_streak_count));
		    			else
		    				subtitle.setText(context.getString(R.string.item_streak_count, count.count.intValue()));

		    			return convertView;
		    		}
				};
				
				list.setAdapter(adapter);
				
				builder.setView(list);
			}
			else
				builder.setMessage(R.string.message_no_active_streaks);
			
			builder.setPositiveButton(R.string.action_close, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder.create().show();
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
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));
		
		return true;
	}
}
