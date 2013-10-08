package edu.northwestern.cbits.intellicare.relax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
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
	protected static final String GROUP_TRACK = "group_track";
	
	private String _groupName = null;
	
	private int _selectedIndex = -1;
	
	private static MediaPlayer _player = null;
	private static Thread _playerThread = null;

	private static String _currentGroupName = null;
	private static int _currentGroupTimes = -1;
	private static int _currentGroupTitles = -1;
	private static int _currentGroupMedia = -1;
	private static int _currentGroupTrack = -1;

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
		
		final int titlesId = this.getIntent().getIntExtra(PlayerActivity.GROUP_TITLES, -1);
		final int mediaId = this.getIntent().getIntExtra(PlayerActivity.GROUP_MEDIA, -1);
		final int timesId = this.getIntent().getIntExtra(PlayerActivity.GROUP_TIMES, -1);

		if (titlesId != -1 && mediaId != -1 && timesId != -1)
		{
			String[] mediaUrls = this.getResources().getStringArray(mediaId);
			String[] mediaTitles = this.getResources().getStringArray(titlesId);
			String[] mediaTimes = this.getResources().getStringArray(timesId);
			
			for (int i = 0; i < mediaUrls.length; i++)
			{
				titles.add(mediaTitles[i]);
				recordings.add(mediaUrls[i]);
				times.add(PlayerActivity.formatTime(mediaTimes[i]));
			}
		}
		
		final PlayerActivity me = this;

		final TextView audioTitle = (TextView) this.findViewById(R.id.audio_title);

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
				if (PlayerActivity._player == null)
					return;
					
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
		
		recordingsList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if (me._groupName.equals(PlayerActivity._currentGroupName) && position == PlayerActivity._currentGroupTrack) 
				{
					playButton.setImageResource(R.drawable.ic_action_playback_pause);

					return;
				}
				else
				{
					playButton.setEnabled(true);

					final String[] mediaUrls = me.getResources().getStringArray(me.getIntent().getIntExtra(PlayerActivity.GROUP_MEDIA, 0));
					final String[] mediaTitles = me.getResources().getStringArray(me.getIntent().getIntExtra(PlayerActivity.GROUP_TITLES, 0));
					
					if (position >= mediaTitles.length)
						return;

					me._selectedIndex = position;
					
					audioTitle.setText(mediaTitles[me._selectedIndex]);
					
					HashMap<String,Object> payload = new HashMap<String, Object>();
					payload.put(PlayerActivity.GROUP_NAME, me._groupName);
					payload.put("track_name", mediaTitles[me._selectedIndex]);
					LogManager.getInstance(me).log("track_completed", payload);
					
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
						
						PlayerActivity._player.setOnCompletionListener(new OnCompletionListener()
						{
							public void onCompletion(MediaPlayer player) 
							{
								HashMap<String,Object> payload = new HashMap<String, Object>();
								payload.put(PlayerActivity.GROUP_NAME, me._groupName);
								payload.put("track_finshed", mediaTitles[me._selectedIndex]);
								LogManager.getInstance(me).log("selected_track", payload);

								
								if (me._selectedIndex < mediaUrls.length - 1)
									recordingsList.performItemClick(null, me._selectedIndex + 1, recordingsList.getItemIdAtPosition(me._selectedIndex + 1));
								else
								{
									PlayerActivity._player.release();
									PlayerActivity._player = null;

									PlayerActivity._currentGroupName = null;
									PlayerActivity._currentGroupMedia = -1;
									PlayerActivity._currentGroupTitles = -1;
									PlayerActivity._currentGroupTimes = -1;
									PlayerActivity._currentGroupTrack = -1;
								}
							}
						});
						
						PlayerActivity._currentGroupName = me._groupName;
						PlayerActivity._currentGroupMedia = mediaId;
						PlayerActivity._currentGroupTitles = titlesId;
						PlayerActivity._currentGroupTimes = timesId;
						PlayerActivity._currentGroupTrack = me._selectedIndex;
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
		
		int existingTrack = this.getIntent().getIntExtra(PlayerActivity.GROUP_TRACK, -1);
		
		if (existingTrack == -1)
			existingTrack = PlayerActivity._currentGroupTrack;
		
		if (existingTrack != -1)
		{
			if (PlayerActivity.isPlaying())
			{
				this._selectedIndex = existingTrack;
				
				audioTitle.setText(titles.get(this._selectedIndex));

				playButton.setEnabled(true);
				
				if (existingTrack > 0)
					previousButton.setEnabled(true);
				else
					previousButton.setEnabled(false);

				if (existingTrack == recordings.size() - 1)
					nextButton.setEnabled(false);
				else
					nextButton.setEnabled(true);
			}
			else
				recordingsList.performItemClick(null, existingTrack, recordingsList.getItemIdAtPosition(existingTrack));
		}
	}
	
	public static boolean isPlaying()
	{
		if (PlayerActivity._player != null)
			return PlayerActivity._player.isPlaying();
		
		return false;
	}
	
	public static String playerTitle(Context context)
	{
		if (PlayerActivity.isPlaying())
		{
			String[] titles = context.getResources().getStringArray(PlayerActivity._currentGroupTitles);
			
			return titles[PlayerActivity._currentGroupTrack];
		}
		
		return null;
	}

	public static String playerSubtitle(Context context)
	{
		if (PlayerActivity.isPlaying())
			return PlayerActivity._currentGroupName;		
		
		return null;
	}

	public static Intent launchIntentForCurrentTrack(Context context)
	{
		if (PlayerActivity.isPlaying())
		{
			Intent intent = new Intent(context, PlayerActivity.class);
			
			intent.putExtra(PlayerActivity.GROUP_NAME, PlayerActivity._currentGroupName);
			intent.putExtra(PlayerActivity.GROUP_MEDIA, PlayerActivity._currentGroupMedia);
			intent.putExtra(PlayerActivity.GROUP_TITLES, PlayerActivity._currentGroupTitles);
			intent.putExtra(PlayerActivity.GROUP_TIMES, PlayerActivity._currentGroupTimes);
			intent.putExtra(PlayerActivity.GROUP_TRACK, PlayerActivity._currentGroupTrack);
			
			return intent;
		}
		
		return null;
	}
	
	protected void onResume()
	{
		super.onResume();
	
		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(PlayerActivity.GROUP_NAME, this._groupName);
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
		
		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(PlayerActivity.GROUP_NAME, this._groupName);
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
