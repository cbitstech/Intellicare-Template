package edu.northwestern.cbits.intellicare.messages;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LessonsActivity extends ConsentedActivity 
{
	public static final String LESSON_LEVEL = "LESSON_LEVEL";
	public static final String LESSON_READ_PREFIX = "lesson_read_";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_test);
		this.getSupportActionBar().setTitle(R.string.title_lessons);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.getBoolean(HelpActivity.HELP_COMPLETED, false) == false)
			this.startActivity(new Intent(this, HelpActivity.class));
		
		String startDay = prefs.getString("config_week_start", null);
		
		Log.e("IM", "START DAY: " + startDay);
		
		if (startDay == null)
		{
			Calendar calendar = Calendar.getInstance();
			
			startDay = "" + calendar.get(Calendar.DAY_OF_WEEK);
			
			Editor e = prefs.edit();
			e.putString("config_week_start", startDay);
			e.commit();
			
			Log.e("IM", "2 START DAY: " + startDay);
		}
		
		ScheduleManager.getInstance(this);
	}
	
	public void onResume()
	{
		super.onResume();
		
		final ArrayList<String> titles = new ArrayList<String>();
		titles.add(this.getString(R.string.title_lesson_one));
		titles.add(this.getString(R.string.title_lesson_two));
		titles.add(this.getString(R.string.title_lesson_three));
		titles.add(this.getString(R.string.title_lesson_four));
		titles.add(this.getString(R.string.title_lesson_five));

		final ArrayList<String> descriptions = new ArrayList<String>();
		descriptions.add(this.getString(R.string.desc_lesson_one));
		descriptions.add(this.getString(R.string.desc_lesson_two));
		descriptions.add(this.getString(R.string.desc_lesson_three));
		descriptions.add(this.getString(R.string.desc_lesson_four));
		descriptions.add(this.getString(R.string.desc_lesson_five));
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final int lessonLevel = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_lesson, titles)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					convertView = inflater.inflate(R.layout.row_lesson, parent, false);
				}
				
				TextView title = (TextView) convertView.findViewById(R.id.lesson_title);
				TextView description = (TextView) convertView.findViewById(R.id.lesson_description);
				
				title.setText(titles.get(position));
				description.setText(descriptions.get(position));
				
				ImageView lockIcon = (ImageView) convertView.findViewById(R.id.lock_icon);
				
				if (position > lessonLevel)
					lockIcon.setImageResource(R.drawable.ic_action_lock_closed);
				else
					lockIcon.setImageResource(R.drawable.ic_action_lock_open);
				
				return convertView;
			}
		};

		ListView list = (ListView) this.findViewById(R.id.list_view);
		list.setAdapter(adapter);
		
		final LessonsActivity me = this;
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if (position > lessonLevel)
					Toast.makeText(me, R.string.toast_lesson_incomplete, Toast.LENGTH_LONG).show();
				else
				{
					Intent lessonIntent = new Intent(me, LessonActivity.class);
					lessonIntent.putExtra(LessonActivity.UNLOCK_LEVEL, position + 1);
					
					switch(position)
					{
						case 0:
							lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.one_titles);
							lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.one_urls);
							break;
						case 1:
							lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.two_titles);
							lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.two_urls);
							break;
						case 2:
							lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.three_titles);
							lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.three_urls);
							break;
						case 3:
							lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.four_titles);
							lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.four_urls);
							break;
						case 4:
							lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.five_titles);
							lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.five_urls);
							break;
					}

					me.startActivity(lessonIntent);
				}
			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_start, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_settings)
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		else if (item.getItemId() == R.id.action_help)
		{
			Intent helpIntent = new Intent(this, HelpActivity.class);
			this.startActivity(helpIntent);
		}
		else if (item.getItemId() == R.id.action_test)
		{
			Intent testIntent = new Intent(this, TestActivity.class);
			this.startActivity(testIntent);
		}
		else if (item.getItemId() == R.id.action_schedule)
		{
			Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
			this.startActivity(scheduleIntent);
		}
		
		return true;
	}
}
