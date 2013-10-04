package edu.northwestern.cbits.intellicare.messages;

import java.util.ArrayList;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import edu.northwestern.cbits.intellicare.ConsentActivity;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.DemographicActivity;
import edu.northwestern.cbits.intellicare.RecruitmentActivity;

public class LessonsActivity extends ConsentedActivity 
{
	public static final String LESSON_LEVEL = "LESSON_LEVEL";
	public static final String LESSON_READ_PREFIX = "lesson_read_";

	private static final String APP_ID = "455953ac6a6eb7c89be9af9848731279";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_lessons);
		this.getSupportActionBar().setTitle(R.string.title_lessons);
		
		ScheduleManager.getInstance(this);
		
		UpdateManager.register(this, APP_ID);
	}
	
	public void onResume()
	{
		super.onResume();

		final ArrayList<String> titles = new ArrayList<String>();
		final ArrayList<String> descriptions = new ArrayList<String>();
		final ArrayList<Integer> ids = new ArrayList<Integer>();

		Cursor c = this.getContentResolver().query(ContentProvider.LESSONS_URI, null, null, null, "lesson_order");

		while (c.moveToNext())
		{
			int id = c.getInt(c.getColumnIndex("id"));
			
			ids.add(Integer.valueOf(id));
			
			switch(id)
			{
				case 1:
					titles.add(this.getString(R.string.title_lesson_one));
					descriptions.add(this.getString(R.string.desc_lesson_one));
					
					break;
				case 2:
					titles.add(this.getString(R.string.title_lesson_two));
					descriptions.add(this.getString(R.string.desc_lesson_two));
					
					break;
				case 3:
					titles.add(this.getString(R.string.title_lesson_three));
					descriptions.add(this.getString(R.string.desc_lesson_three));
					
					break;
				case 4:
					titles.add(this.getString(R.string.title_lesson_four));
					descriptions.add(this.getString(R.string.desc_lesson_four));
					
					break;
				case 5:
					titles.add(this.getString(R.string.title_lesson_five));
					descriptions.add(this.getString(R.string.desc_lesson_five));
					
					break;
			}
		}
		
		c.close();

		final LessonsActivity me = this;

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

				String selection = "id = ?";
				String[] args = { "" + ids.get(position) };
				
				Cursor c = me.getContentResolver().query(ContentProvider.LESSONS_URI, null, selection, args, null);
				
				if (c.moveToNext())
				{
					if (c.getInt(c.getColumnIndex("complete")) == 0)
						lockIcon.setImageResource(R.drawable.ic_action_lock_closed);
					else
						lockIcon.setImageResource(R.drawable.ic_action_lock_open);
				}
				
				c.close();
				
				return convertView;
			}
		};

		ListView list = (ListView) this.findViewById(R.id.list_view);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				String selection = "id = ?";
				String[] args = { "" + ids.get(position) };
				
				Cursor c = me.getContentResolver().query(ContentProvider.LESSONS_URI, null, selection, args, null);
				
				if (c.moveToNext())
				{
					if (c.getInt(c.getColumnIndex("complete")) == 0)
						Toast.makeText(me, R.string.toast_lesson_incomplete, Toast.LENGTH_LONG).show();
					else
					{
						Intent lessonIntent = new Intent(me, LessonActivity.class);
						
						int lessonId = ids.get(position).intValue();

						if (position < ids.size() - 1)
							lessonIntent.putExtra(LessonsActivity.LESSON_LEVEL, ids.get(position).intValue());
						
						switch(lessonId)
						{
							case 1:
								lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.one_titles);
								lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.one_urls);
								break;
							case 2:
								lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.two_titles);
								lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.two_urls);
								break;
							case 3:
								lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.three_titles);
								lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.three_urls);
								break;
							case 4:
								lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.four_titles);
								lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.four_urls);
								break;
							case 5:
								lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.five_titles);
								lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.five_urls);
								break;
						}

						me.startActivity(lessonIntent);
					}
				}
				
				c.close();
			}
		});
		
		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (ConsentActivity.isConsented() == true && prefs.getBoolean(HelpActivity.HELP_COMPLETED, false) == false)
			this.startActivity(new Intent(this, HelpActivity.class));
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
		else if (item.getItemId() == R.id.action_phq_four)
		{
			Intent phqIntent = new Intent(this, RecruitmentActivity.class);
			this.startActivity(phqIntent);
		}
		else if (item.getItemId() == R.id.action_demographic)
		{
			Intent phqIntent = new Intent(this, DemographicActivity.class);
			this.startActivity(phqIntent);
		}

		/* else if (item.getItemId() == R.id.action_test)
		{
			Intent testIntent = new Intent(this, TestActivity.class);
			this.startActivity(testIntent);
		} */
		
		return true;
	}
}
