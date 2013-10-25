package edu.northwestern.cbits.intellicare.relax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PlayerActivity extends ActionBarActivity 
{
	protected static final String GROUP_NAME = "group_name";	
	protected static final String GROUP_MEDIA = "group_media";
	protected static final String GROUP_TITLES = "group_titles";
	protected static final String GROUP_TIMES = "group_times";
	protected static final String GROUP_TRACK = "group_track";
	
	private String _groupName = null;
	
	private static MediaPlayer _player = null;
	private static Thread _playerThread = null;

	private static String _currentGroupName = null;
	private static int _currentGroupTimes = -1;
	private static int _currentGroupTitles = -1;
	private static int _currentGroupMedia = -1;
	private static int _currentGroupTrack = -1;
	private static int _currentStressLevel = -1;
	
	private int _selectedIndex = -1;

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

		this.setContentView(R.layout.activity_player);
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
	
		this._groupName = this.getIntent().getStringExtra(PlayerActivity.GROUP_NAME);
		
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
		
		final TextView ratingNumber = (TextView) this.findViewById(R.id.rating_number);
		final SeekBar ratingBar = (SeekBar) this.findViewById(R.id.stress_rating);
		
		ratingBar.setMax(9);
		
		final ImageButton playButton = (ImageButton) this.findViewById(R.id.play_pause);

		ratingBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				progress += 1;
				
				PlayerActivity._currentStressLevel = progress;
				
				ratingNumber.setText("" + progress);
				playButton.setEnabled(true);
			}

			public void onStartTrackingTouch(SeekBar seekBar) 
			{

			}

			public void onStopTrackingTouch(SeekBar seekBar) 
			{

			}
		});
		
		Log.e("PC", "STRESS: " + PlayerActivity._currentStressLevel);
		
		if (PlayerActivity._currentStressLevel != -1)
			ratingBar.setProgress(PlayerActivity._currentStressLevel - 1);

		final TextView audioTitle = (TextView) this.findViewById(R.id.label_selected_track_title);
		final ImageButton trackButton = (ImageButton) this.findViewById(R.id.choose_track);
		
		trackButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
			    AlertDialog.Builder builder = new AlertDialog.Builder(me);
			    
			    builder = builder.setTitle(R.string.select_track_label);
			    builder = builder.setItems(titles.toArray(new String[0]), new DialogInterface.OnClickListener() 
			    {
			    	public void onClick(DialogInterface dialog, int which) 
			    	{
			    		me._selectedIndex = which;

						audioTitle.setText(titles.get(which));
			    		
			    		Typeface font = audioTitle.getTypeface();
			    		
			    		audioTitle.setTypeface(Typeface.create(font, Typeface.NORMAL));
			    		
						ratingBar.setProgress(0);
						
						if (PlayerActivity._player != null)
						{
							if (PlayerActivity._player.isPlaying())
								PlayerActivity._player.stop();
							
							PlayerActivity._player = null;
						}
			    	}
			    });
			    
			    builder.create().show();
			}
		});

		playButton.setEnabled(false);
		
		Log.e("PC", PlayerActivity._currentGroupName  + " -- " +  PlayerActivity._currentGroupTrack);

		if (PlayerActivity._currentGroupName != null && PlayerActivity._currentGroupTrack >= 0)
		{
			if (PlayerActivity._currentGroupTrack < titles.size())
			{
	    		me._selectedIndex = PlayerActivity._currentGroupTrack;
	
	    		audioTitle.setText(titles.get(me._selectedIndex));
	    		
	    		Typeface font = audioTitle.getTypeface();
	    		audioTitle.setTypeface(Typeface.create(font, Typeface.NORMAL));
	    		
	    		playButton.setEnabled(true);
				playButton.setImageResource(R.drawable.ic_action_playback_pause);
				
				ratingBar.setEnabled(false);
				trackButton.setEnabled(false);
			}
		}

		playButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				Log.e("PC", "CLICK");
				
				if (PlayerActivity._player == null)
				{
					final String[] mediaUrls = me.getResources().getStringArray(me.getIntent().getIntExtra(PlayerActivity.GROUP_MEDIA, 0));
					final String[] mediaTitles = me.getResources().getStringArray(me.getIntent().getIntExtra(PlayerActivity.GROUP_TITLES, 0));
					
					try 
					{
						AssetFileDescriptor afd = me.getAssets().openFd(mediaUrls[me._selectedIndex].replace("file:///android_asset/", ""));
						
						PlayerActivity._player = new MediaPlayer();
						PlayerActivity._player.setAudioStreamType(AudioManager.STREAM_MUSIC);
						PlayerActivity._player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

						PlayerActivity._player.prepare();

						playButton.setImageResource(R.drawable.ic_action_playback_pause);
						
						PlayerActivity._player.setOnCompletionListener(new OnCompletionListener()
						{
							public void onCompletion(MediaPlayer player) 
							{
								Log.e("PC", "COMPLETE");
								
								HashMap<String,Object> payload = new HashMap<String, Object>();
								payload.put(PlayerActivity.GROUP_NAME, me._groupName);
								payload.put("track_finshed", mediaTitles[PlayerActivity._currentGroupTrack]);
								LogManager.getInstance(me).log("selected_track", payload);
								
								PlayerActivity._player.release();
								PlayerActivity._player = null;

								PlayerActivity._currentGroupName = null;
								PlayerActivity._currentGroupMedia = -1;
								PlayerActivity._currentGroupTitles = -1;
								PlayerActivity._currentGroupTimes = -1;
								PlayerActivity._currentGroupTrack = -1;
								PlayerActivity._currentStressLevel = -1;
							}
						});
						
						PlayerActivity._currentGroupName = me._groupName;
						PlayerActivity._currentGroupMedia = mediaId;
						PlayerActivity._currentGroupTitles = titlesId;
						PlayerActivity._currentGroupTimes = timesId;
						PlayerActivity._currentGroupTrack = me._selectedIndex;
						
						ratingBar.setEnabled(false);
						trackButton.setEnabled(false);
						
						Runnable r = new Runnable()
						{
							public void run() 
							{
								Log.e("PC", "CLICK AUTO");

								try 
								{
									Thread.sleep(250);

									me.runOnUiThread(new Runnable()
	                                {
	                                    public void run() 
	                                    {
	                                    	PlayerActivity._player.seekTo(0);

	                    					playButton.setImageResource(R.drawable.ic_action_playback_pause);
	                    					PlayerActivity._player.start();
	                    					
	                    					ratingBar.setEnabled(false);
	                    					trackButton.setEnabled(false);
	                                    }
	                                });
								}
								catch (InterruptedException e) 
								{
									e.printStackTrace();
								}
							}
						};
						
						Thread t = new Thread(r);
						t.start();
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
				else if (PlayerActivity._player.isPlaying())
				{
					Log.e("PC", "PAUSE");
					playButton.setImageResource(R.drawable.ic_action_playback_play);
					PlayerActivity._player.pause();

					ratingBar.setEnabled(true);
					trackButton.setEnabled(true);
				}
				else
				{
					Log.e("PC", "PLAY");
					
					playButton.setImageResource(R.drawable.ic_action_playback_pause);
					PlayerActivity._player.start();
					
					ratingBar.setEnabled(false);
					trackButton.setEnabled(false);
				}
			}
		});
		
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
			final TextView progressText = (TextView) this.findViewById(R.id.track_progress);
			
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
			PlayerActivity._playerThread = null;
		
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
