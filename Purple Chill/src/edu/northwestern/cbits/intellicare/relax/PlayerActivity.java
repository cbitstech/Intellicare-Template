package edu.northwestern.cbits.intellicare.relax;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayerActivity extends ActionBarActivity 
{
	protected static final String GROUP_NAME = "group_name";	
	protected static final String GROUP_MEDIA = "group_media";
	protected static final String GROUP_TITLES = "group_titles";
	protected static final String GROUP_TIMES = "group_times";
	
	private String _groupName = null;
	
	private int _selectedIndex = -1;
	
	private static MediaPlayer _player = null;
	private static Thread _playerThread = null;

	static String formatTime(String secondsString)
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
		
		this._groupName = this.getIntent().getStringExtra(PlayerActivity.GROUP_NAME);

		this.setContentView(R.layout.activity_player);
		this.getSupportActionBar().setTitle(this._groupName);

		final ArrayList<String> recordings = new ArrayList<String>();
		final ArrayList<String> titles = new ArrayList<String>();
		final ArrayList<String> times = new ArrayList<String>();

		if (this.getIntent().hasExtra(PlayerActivity.GROUP_MEDIA) && this.getIntent().hasExtra(PlayerActivity.GROUP_TITLES)
				&& this.getIntent().hasExtra(PlayerActivity.GROUP_TIMES))
		{
			String[] mediaUrls = this.getResources().getStringArray(this.getIntent().getIntExtra(PlayerActivity.GROUP_MEDIA, 0));
			String[] mediaTitles = this.getResources().getStringArray(this.getIntent().getIntExtra(PlayerActivity.GROUP_TITLES, 0));
			String[] mediaTimes = this.getResources().getStringArray(this.getIntent().getIntExtra(PlayerActivity.GROUP_TIMES, 0));
			
			for (int i = 0; i < mediaUrls.length; i++)
			{
				titles.add(mediaTitles[i]);
				recordings.add(mediaUrls[i]);
				times.add(PlayerActivity.formatTime(mediaTimes[i]));
			}
		}
		
		final PlayerActivity me = this;
		
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

				TextView time = (TextView) convertView.findViewById(R.id.recording_time);
				time.setText(times.get(position));

				Drawable d = me.getResources().getDrawable(R.drawable.ic_action_music_2);
				
				if (position == me._selectedIndex)
					d = me.getResources().getDrawable(R.drawable.ic_action_playback_play);
				
				title.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

				return convertView;
			}
		};
		
		final ListView recordingsList = (ListView) this.findViewById(R.id.recording_list);
		recordingsList.setAdapter(adapter);

		final ImageButton playButton = (ImageButton) this.findViewById(R.id.play_pause);
		playButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				if (PlayerActivity._player.isPlaying())
				{
					playButton.setImageResource(R.drawable.ic_action_playback_play);
					PlayerActivity._player.pause();
				}
				else
				{
					playButton.setImageResource(R.drawable.ic_action_playback_pause);
					PlayerActivity._player.start();
				}
			}
		});
		
		playButton.setEnabled(false);

		final ImageButton previousButton = (ImageButton) this.findViewById(R.id.previous_track);
		previousButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				int position = me._selectedIndex - 1;
				
				if (position < 0)
					position = 0;
				
				recordingsList.performItemClick(null, position, recordingsList.getItemIdAtPosition(position));
			}
		});
		previousButton.setEnabled(false);

		final ImageButton nextButton = (ImageButton) this.findViewById(R.id.next_track);
		nextButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				int position = me._selectedIndex + 1;
				
				if (position > recordings.size() - 1)
					position = recordings.size() - 1;
				
				recordingsList.performItemClick(null, position, recordingsList.getItemIdAtPosition(position));
			}
		});
		nextButton.setEnabled(false);

		final TextView audioTitle = (TextView) this.findViewById(R.id.audio_title);
		
		recordingsList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if (me._selectedIndex != position)
				{
					playButton.setEnabled(true);

					String[] mediaUrls = me.getResources().getStringArray(me.getIntent().getIntExtra(PlayerActivity.GROUP_MEDIA, 0));
					String[] mediaTitles = me.getResources().getStringArray(me.getIntent().getIntExtra(PlayerActivity.GROUP_TITLES, 0));

					me._selectedIndex = position;
					
					audioTitle.setText(mediaTitles[me._selectedIndex]);
					
					if (PlayerActivity._player != null)
					{
						 if (PlayerActivity._player.isPlaying())
							 PlayerActivity._player.stop();
						 
						 PlayerActivity._player.release();
						 
						 PlayerActivity._player = null;
					}
					
					try 
					{
						AssetFileDescriptor afd = me.getAssets().openFd(mediaUrls[position].replace("file:///android_asset/", ""));
						
						PlayerActivity._player = new MediaPlayer();
						PlayerActivity._player.setAudioStreamType(AudioManager.STREAM_MUSIC);
						PlayerActivity._player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
						
						PlayerActivity._player.prepare();
						
						PlayerActivity._player.start();

						playButton.setImageResource(R.drawable.ic_action_playback_pause);
						
						PlayerActivity._player.setOnInfoListener(new MediaPlayer.OnInfoListener()
						{
							public boolean onInfo(MediaPlayer mp, int what, int extra) 
							{
								Log.e("PC", "INFO: " + what + " EXTRA " + extra);
								
								return true;
							}
						});
					} 
					catch (IllegalArgumentException e) 
					{
						e.printStackTrace();
					} 
					catch (SecurityException e) 
					{
						e.printStackTrace();
					} 
					catch (IllegalStateException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				
				if (position > 0)
					previousButton.setEnabled(true);
				else
					previousButton.setEnabled(false);

				if (position == recordings.size() - 1)
					nextButton.setEnabled(false);
				else
					nextButton.setEnabled(true);

				adapter.notifyDataSetChanged();
			}
		});
	}
	
	protected void onResume()
	{
		super.onResume();
		
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
		
		if (PlayerActivity._playerThread == null)
		{
			final ProgressBar progress = (ProgressBar) this.findViewById(R.id.playback_progress);
			final TextView progressText = (TextView) this.findViewById(R.id.playback_text);
			
			final PlayerActivity me = this;

			final Runnable r = new Runnable()
			{
				public void run() 
				{
					try 
					{
						while (PlayerActivity._playerThread != null)
						{
							if (PlayerActivity._player != null)
							{
								final int duration = PlayerActivity._player.getDuration();
								final int position = PlayerActivity._player.getCurrentPosition();
								
								me.runOnUiThread(new Runnable()
								{
									public void run() 
									{
										progress.setMax(duration);
										progress.setProgress(position);
										progressText.setText(PlayerActivity.formatTime("" + (position / 1000)) + " / " + PlayerActivity.formatTime("" + (duration / 1000)));
									}
								});
							}
							else
							{
								progress.setMax(1);
								progress.setProgress(0);
							}
						
							Thread.sleep(250);
						}
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					
					Log.e("PC", "EXITING THREAD");
				}
			};
			
			PlayerActivity._playerThread = new Thread(r);
			PlayerActivity._playerThread.start();
		}
	}
	
	protected void onPause()
	{
		super.onPause();

		if (PlayerActivity._playerThread != null)
		{
			PlayerActivity._playerThread = null;
		}
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

		return R.array.mindful_titles;
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
