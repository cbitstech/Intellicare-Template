package edu.northwestern.cbits.intellicare.relax;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentActivity;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class IndexActivity extends ConsentedActivity 
{
	private static final String APP_ID = "c704c1c38bc29d37aca8ac17f94f6b94";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_index);
		this.getSupportActionBar().setTitle(R.string.app_name);
		
		UpdateManager.register(this, APP_ID);
	}
	
	public void onResume()
	{
		super.onResume();
		
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
				Intent playerIntent = new Intent(me, PlayerActivity.class);
				
				playerIntent.putExtra(PlayerActivity.GROUP_NAME, titles[position]);
				
				switch (position)
				{
					case 0:
						playerIntent.putExtra(PlayerActivity.GROUP_MEDIA, R.array.deep_breathing_media_urls);
						playerIntent.putExtra(PlayerActivity.GROUP_TITLES, R.array.deep_breathing_media_url_titles);
						playerIntent.putExtra(PlayerActivity.GROUP_TIMES, R.array.deep_breathing_media_url_times);
						break;
					case 1:
						playerIntent.putExtra(PlayerActivity.GROUP_MEDIA, R.array.muscle_media_urls);
						playerIntent.putExtra(PlayerActivity.GROUP_TITLES, R.array.muscle_media_url_titles);
						playerIntent.putExtra(PlayerActivity.GROUP_TIMES, R.array.muscle_media_url_times);
						break;
					case 2:
						playerIntent.putExtra(PlayerActivity.GROUP_MEDIA, R.array.autogenic_media_urls);
						playerIntent.putExtra(PlayerActivity.GROUP_TITLES, R.array.autogenic_media_url_titles);
						playerIntent.putExtra(PlayerActivity.GROUP_TIMES, R.array.autogenic_media_url_times);
						break;
//					case 3:
//						playerIntent.putExtra(PlayerActivity.GROUP_MEDIA, R.array.);
//						break;
					case 4:
						playerIntent.putExtra(PlayerActivity.GROUP_MEDIA, R.array.mindfulness_media_urls);
						playerIntent.putExtra(PlayerActivity.GROUP_TITLES, R.array.mindfulness_media_url_titles);
						playerIntent.putExtra(PlayerActivity.GROUP_TIMES, R.array.mindfulness_media_url_times);
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
		
		return true;
	}
}
