package edu.northwestern.cbits.intellicare.relax;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentActivity;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class IndexActivity extends ConsentedActivity 
{
	private static final String APP_ID = "46b0f12d234c03aadfc1fd4688b22aaf";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_index);
		this.getSupportActionBar().setTitle(R.string.app_name);
		
		UpdateManager.register(this, APP_ID);
		ScheduleManager.getInstance(this);
	}
	
	public void onPause()
	{
		super.onPause();
		
		LogManager.getInstance(this).log("exited_index", null);
	}
	
	public void onResume()
	{
		super.onResume();
		
		LogManager.getInstance(this).log("viewed_index", null);

		final String[] titles = this.getResources().getStringArray(R.array.group_titles);
		final String[] descriptions = this.getResources().getStringArray(R.array.group_descriptions);

		final IndexActivity me = this;

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_group, titles)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					convertView = inflater.inflate(R.layout.row_group, parent, false);
				}
				
				TextView title = (TextView) convertView.findViewById(R.id.group_title);
				TextView description = (TextView) convertView.findViewById(R.id.group_description);
				
				title.setText(titles[position]);
				description.setText(descriptions[position]);
				
				return convertView;
			}
		};

		ListView list = (ListView) this.findViewById(R.id.list_view);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Intent playerIntent = new Intent(me, GroupActivity.class);
				
				playerIntent.putExtra(GroupActivity.GROUP_NAME, titles[position]);
				
				switch (position)
				{
					case 0:
						playerIntent.putExtra(GroupActivity.GROUP_MEDIA, R.array.deep_breathing_media_urls);
						playerIntent.putExtra(GroupActivity.GROUP_TITLES, R.array.deep_breathing_media_url_titles);
						playerIntent.putExtra(GroupActivity.GROUP_TIMES, R.array.deep_breathing_media_url_times);
						playerIntent.putExtra(GroupActivity.GROUP_DESCRIPTIONS, R.array.deep_breathing_descs);
						break;
					case 1:
						playerIntent.putExtra(GroupActivity.GROUP_MEDIA, R.array.muscle_media_urls);
						playerIntent.putExtra(GroupActivity.GROUP_TITLES, R.array.muscle_media_url_titles);
						playerIntent.putExtra(GroupActivity.GROUP_TIMES, R.array.muscle_media_url_times);
						playerIntent.putExtra(GroupActivity.GROUP_DESCRIPTIONS, R.array.muscle_descs);
						break;
					case 2:
						playerIntent.putExtra(GroupActivity.GROUP_MEDIA, R.array.autogenic_media_urls);
						playerIntent.putExtra(GroupActivity.GROUP_TITLES, R.array.autogenic_media_url_titles);
						playerIntent.putExtra(GroupActivity.GROUP_TIMES, R.array.autogenic_media_url_times);
						playerIntent.putExtra(GroupActivity.GROUP_DESCRIPTIONS, R.array.autogenic_descs);
						break;
					case 3:
						playerIntent.putExtra(GroupActivity.GROUP_MEDIA, R.array.visualization_media_urls);
						playerIntent.putExtra(GroupActivity.GROUP_TITLES, R.array.visualization_media_url_titles);
						playerIntent.putExtra(GroupActivity.GROUP_TIMES, R.array.visualization_media_url_times);
						playerIntent.putExtra(GroupActivity.GROUP_DESCRIPTIONS, R.array.visualization_descs);
						break;
					case 4:
						playerIntent.putExtra(GroupActivity.GROUP_MEDIA, R.array.mindfulness_media_urls);
						playerIntent.putExtra(GroupActivity.GROUP_TITLES, R.array.mindfulness_media_url_titles);
						playerIntent.putExtra(GroupActivity.GROUP_TIMES, R.array.mindfulness_media_url_times);
						playerIntent.putExtra(GroupActivity.GROUP_DESCRIPTIONS, R.array.mindfulness_descs);
						break;
				}
				
				me.startActivity(playerIntent);
			}
		});
		
		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});
		
		LinearLayout currentTrack = (LinearLayout) this.findViewById(R.id.current_track);
		
		final AudioFileManager audio = AudioFileManager.getInstance(this);

		if (audio.isPlaying())
		{
			currentTrack.setVisibility(View.VISIBLE);

			ImageButton trackButton = (ImageButton) this.findViewById(R.id.goto_current_track);
			
			trackButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v) 
				{
					Intent playerIntent = audio.launchIntentForCurrentTrack();
					
					if (playerIntent != null)
						me.startActivity(playerIntent);
					else
						Log.e("PC", "NULL INTENT FOR CURRENTLY PLAYING!!!");
				}
			});
			
			TextView trackName = (TextView) this.findViewById(R.id.current_track_name);
			trackName.setText(audio.currentTrackTitle());
		}
		else
			currentTrack.setVisibility(View.GONE);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (ConsentActivity.isConsented() == true && prefs.getBoolean(HelpActivity.HELP_COMPLETED, false) == false)
			this.startActivity(new Intent(this, HelpActivity.class));
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_index, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_help)
		{
			Intent helpIntent = new Intent(this, HelpActivity.class);
			this.startActivity(helpIntent);
		}
		else if (item.getItemId() == R.id.action_settings)
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		
		return true;
	}
}
