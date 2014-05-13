package edu.northwestern.cbits.intellicare.relax;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class GroupActivity extends ConsentedActivity
{
	protected static final String GROUP_NAME = "group_name";	
	protected static final String GROUP_MEDIA = "group_media";
	protected static final String GROUP_TITLES = "group_titles";
	protected static final String GROUP_TIMES = "group_times";
	protected static final String GROUP_TRACK = "group_track";
	protected static final String STRESS_RATING = "stress_rating";
	protected static final String GROUP_DESCRIPTIONS = "group_descriptions";
	protected static final String TRACK_END = "track_end";
	
	private String _groupName = null;
	
    private static String formatTime(String secondsString)
    {
        int seconds = Integer.parseInt(secondsString);
        
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        String formatted = minutes + ":";
        
        if (seconds < 10)
                formatted += "0";
        
        formatted += seconds;
        
        return formatted;
    }

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_group);
	}

	public void onResume()
	{
		super.onResume();
	
        final ArrayList<String> recordings = new ArrayList<String>();
        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> times = new ArrayList<String>();
        final ArrayList<String> descriptions = new ArrayList<String>();
        
        final int titlesId = this.getIntent().getIntExtra(GroupActivity.GROUP_TITLES, -1);
        final int mediaId = this.getIntent().getIntExtra(GroupActivity.GROUP_MEDIA, -1);
        final int timesId = this.getIntent().getIntExtra(GroupActivity.GROUP_TIMES, -1);
        final int groupsId = this.getIntent().getIntExtra(GroupActivity.GROUP_DESCRIPTIONS, -1);

        if (titlesId != -1 && mediaId != -1 && timesId != -1)
        {
            String[] mediaFiles = this.getResources().getStringArray(mediaId);
            String[] mediaTitles = this.getResources().getStringArray(titlesId);
            String[] mediaTimes = this.getResources().getStringArray(timesId);
            String[] mediaDescs = this.getResources().getStringArray(groupsId);
            
            for (int i = 0; i < mediaFiles.length; i++)
            {
                titles.add(mediaTitles[i]);
                recordings.add(mediaFiles[i]);
                times.add(GroupActivity.formatTime(mediaTimes[i]));
                descriptions.add(mediaDescs[i]);
            }
        }

		this._groupName = this.getIntent().getStringExtra(GroupActivity.GROUP_NAME);
		
		this.getSupportActionBar().setTitle(this._groupName);
		
		final GroupActivity me = this;

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_recording, recordings)
        {
        	public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.row_recording, parent, false);
                }
                
                TextView title = (TextView) convertView.findViewById(R.id.recording_title);
                title.setText(titles.get(position));

                TextView description = (TextView) convertView.findViewById(R.id.recording_description);
                description.setText(descriptions.get(position));

                TextView time = (TextView) convertView.findViewById(R.id.recording_time);
                time.setText(times.get(position));

                Drawable d = me.getResources().getDrawable(R.drawable.ic_action_playback_play);
                
                String url = recordings.get(position);

                if (AudioFileManager.getInstance(me).isPlaceholder(Uri.parse(url)))
                    d = me.getResources().getDrawable(R.drawable.ic_action_playback_play);
                
                title.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

                return convertView;
            }
        };
        
        TextView groupDesc = (TextView) this.findViewById(R.id.group_desc);
        
        String[] allGroups = this.getResources().getStringArray(R.array.group_titles);
        String[] allDescs = this.getResources().getStringArray(R.array.group_descriptions);
        
        for (int i = 0; i < allGroups.length && i < allDescs.length; i++)
        {
        	if (this._groupName.equals(allGroups[i]))
        		groupDesc.setText(allDescs[i]);
        }
        
        final ListView recordingsList = (ListView) this.findViewById(R.id.recording_list);
        recordingsList.setAdapter(adapter);
        
        recordingsList.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long id) 
			{
				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				View body = inflater.inflate(R.layout.view_stress_rating, null);
                
                final TextView ratingNumber = (TextView) body.findViewById(R.id.rating_number);

                final OnItemClickListener meListener = this;
                
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder = builder.setTitle(R.string.title_rate_stress);
				builder = builder.setPositiveButton(R.string.button_continue, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						try
						{
							int stressLevel = Integer.parseInt(ratingNumber.getText().toString());
							
	                		HashMap<String,Object> payload = new HashMap<String, Object>();
	                		payload.put(GroupActivity.STRESS_RATING, stressLevel);
	                		LogManager.getInstance(me).log("rated_stress", payload);
	                		
	                		String filename = recordings.get(i);
	                		
	                		if (filename.endsWith(".html"))
	                		{
	                			Intent htmlIntent = new Intent(me, HtmlActivity.class);
	                			htmlIntent.putExtra(HtmlActivity.FILENAME, filename);
	                			
	                			me.startActivity(htmlIntent);
	                		}
	                		else
	                		{
		                		File f = new File(me.getFilesDir(), filename);
		                		
								Uri u = Uri.fromFile(f);
								
								String title = titles.get(i);
								String description = descriptions.get(i);
	
								me.startActivity(AudioFileManager.getInstance(me).launchIntentForUri(u, title, description));
	                		}
						}
						catch (NumberFormatException e)
						{
							Toast.makeText(me, R.string.toast_rate_stress, Toast.LENGTH_LONG).show();
							
							meListener.onItemClick(adapterView, view, i, id);
						}
					}
				});

                final SeekBar ratingBar = (SeekBar) body.findViewById(R.id.stress_rating);
                
                ratingBar.setMax(9);
                
                ratingBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
                {
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
                        {
                            progress += 1;
                            
                            ratingNumber.setText("" + progress);
                        }

                        public void onStartTrackingTouch(SeekBar seekBar) 
                        {

                        }

                        public void onStopTrackingTouch(SeekBar seekBar) 
                        {

                        }
                });

				
				builder = builder.setView(body);
				
				builder.create().show();
			}
        });
		
		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(GroupActivity.GROUP_NAME, this._groupName);
		LogManager.getInstance(this).log("viewed_group", payload);
		
		String completedKey = this._groupName + "_completed";
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.getBoolean(completedKey, false) == false)
		{
			Intent introIntent = new Intent(this, IntroActivity.class);
			
			introIntent.putExtra(IntroActivity.SEQUENCE_URLS, this.getIntroUrls());
			introIntent.putExtra(IntroActivity.SEQUENCE_TITLES, this.getIntroTitles());
			introIntent.putExtra(IntroActivity.SEQUENCE_KEY, completedKey);
			
			this.startActivity(introIntent);
		}
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(GroupActivity.GROUP_NAME, this._groupName);
		LogManager.getInstance(this).log("exited_group", payload);	
	}

	private int getIntroUrls() 
	{
		if (this._groupName.equals(this.getString(R.string.breathing_title)))
			return R.array.breathing_urls;
		else if (this._groupName.equals(this.getString(R.string.muscle_title)))
			return R.array.muscle_urls;
		else if (this._groupName.equals(this.getString(R.string.autogenic_title)))
			return R.array.autogenic_urls;
		else if (this._groupName.equals(this.getString(R.string.visualization_title)))
			return R.array.visualization_urls;
		else if (this._groupName.equals(this.getString(R.string.sleep_title)))
			return R.array.sleep_urls;

		return R.array.mindful_urls;
	}

	private int getIntroTitles() 
	{
		if (this._groupName.equals(this.getString(R.string.breathing_title)))
			return R.array.breathing_titles;
		else if (this._groupName.equals(this.getString(R.string.muscle_title)))
			return R.array.muscle_titles;
		else if (this._groupName.equals(this.getString(R.string.autogenic_title)))
			return R.array.autogenic_titles;
		else if (this._groupName.equals(this.getString(R.string.visualization_title)))
			return R.array.visualization_titles;
		else if (this._groupName.equals(this.getString(R.string.sleep_title)))
			return R.array.sleep_titles;

		return R.array.mindful_titles;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_group, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_help)
		{
			String completedKey = this._groupName + "_completed";

			Intent introIntent = new Intent(this, IntroActivity.class);
			
			introIntent.putExtra(IntroActivity.SEQUENCE_URLS, this.getIntroUrls());
			introIntent.putExtra(IntroActivity.SEQUENCE_TITLES, this.getIntroTitles());
			introIntent.putExtra(IntroActivity.SEQUENCE_KEY, completedKey);
			
			this.startActivity(introIntent);
		}
		
		return true;
	}
}
