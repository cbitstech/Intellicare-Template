package edu.northwestern.cbits.intellicare.moveme;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class TimerActivity extends ConsentedActivity 
{
    protected String _selectedExercise = null;

    private static boolean _started = false;
    private static boolean _playing = false;
    private static long _elapsed = 0;

    private static int _preMood = 0;
	private static int _postMood = 0;

	private static Thread _timerThread = null;
	private Thread _refreshThread = null;

	protected static long _lastTick = 0;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_timer);
        
        ActionBar actionBar = this.getSupportActionBar();
        
        actionBar.setTitle(R.string.title_exercise_timer);
        
        final TimerActivity me = this;
        
        Button finish = (Button) this.findViewById(R.id.action_finish_exercise);
        finish.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View arg0) 
			{
				TimerActivity._started = false;
				TimerActivity._playing = false;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.title_post_mood);
				builder.setSingleChoiceItems(R.array.array_moods, 3, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						TimerActivity._postMood  = which - 3;
					}
				});
				
				builder.setPositiveButton(R.string.action_finish, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me.finish();
					}
				});
				
				builder.create().show();
			}
		});
    }
	
	public void onResume()
	{
		if (TimerActivity._started == false)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_pre_mood);
			builder.setSingleChoiceItems(R.array.array_moods, 3, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int which) 
				{
					TimerActivity._preMood = which - 3;
				}
			});
			
			builder.setPositiveButton(R.string.action_continue, new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{

				}
			});
			
			builder.create().show();

			TimerActivity._started = true;
		}
		
		final TimerActivity me = this;

		Runnable r = new Runnable()
		{
			public void run() 
			{
				try 
				{
					while (true)
					{
						me.runOnUiThread(new Runnable()
						{
							public void run() 
							{
								me.refreshTimer();
							}
						});
						
						Thread.sleep(250);
					} 
				}
				catch (InterruptedException e) 
				{

				}				
			}
		};
		
		if (this._refreshThread == null)
		{
			this._refreshThread = new Thread(r);
			this._refreshThread.start();
		}
		
		super.onResume();
	}
	
	public void onPause()
	{
		super.onPause();
		
		if (this._refreshThread != null)
		{
			this._refreshThread.interrupt();
			this._refreshThread = null;
		}
	}
	
    private void refreshTimer() 
    {
		long elapsed = TimerActivity._elapsed / 1000;
		
		TextView minutes = (TextView) this.findViewById(R.id.label_minutes);
		TextView seconds = (TextView) this.findViewById(R.id.label_seconds);
		
		if (elapsed / 60 < 10)
			minutes.setText("0" + elapsed / 60);
		else
			minutes.setText("" + elapsed / 60);

		if (elapsed % 60 < 10)
			seconds.setText("0" + elapsed % 60);
		else
			seconds.setText("" + elapsed % 60);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_timer, menu);

        MenuItem item = menu.findItem(R.id.action_toggle);

		if (TimerActivity._playing)
			item.setIcon(R.drawable.ic_action_playback_pause);
		else
			item.setIcon(R.drawable.ic_action_playback_play);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	int itemId = item.getItemId();
    	
    	if (itemId == R.id.action_music)
    		this.launchMusicPlayer();
    	else if (itemId == R.id.action_toggle)
    	{
    		TimerActivity._playing = (TimerActivity._playing == false);
    		
    		if (TimerActivity._playing)
    		{
    			item.setIcon(R.drawable.ic_action_playback_pause);
    			
    			TimerActivity._lastTick = System.currentTimeMillis();
    			
    			if (TimerActivity._timerThread == null)
    			{
    				Runnable r = new Runnable()
    				{
						public void run() 
						{
							TimerActivity._elapsed = 0;
							
							TimerActivity._lastTick  = System.currentTimeMillis();
							
							while (TimerActivity._started)
							{
								if (TimerActivity._playing)
								{
									long now = System.currentTimeMillis();

									TimerActivity._elapsed += (now - TimerActivity._lastTick);
								
									TimerActivity._lastTick = now;
								}
								
								try 
								{
									Thread.sleep(500);
								} 
								catch (InterruptedException e) 
								{
									TimerActivity._started = false;
								}
							}
							
							TimerActivity._timerThread = null;
						}
    				};
    				
    				TimerActivity._timerThread = new Thread(r);
    				TimerActivity._timerThread.start();
    			}
    		}
    		else
    			item.setIcon(R.drawable.ic_action_playback_play);
    	}
    	
        return true;
    }

	@SuppressLint("InlinedApi") 
	@SuppressWarnings("deprecation")
	private void launchMusicPlayer() 
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String player = prefs.getString(SettingsActivity.SETTING_PLAYER, SettingsActivity.DEFAULT_PACKAGE);
		
		if (player.equals(SettingsActivity.DEFAULT_PACKAGE))
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
			{
				Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
				this.startActivity(intent);
			}
			else
			{
				Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
				startActivity(intent);
			}
		}
		else
		{
			PackageManager packages = this.getPackageManager();
			
			Intent intent = packages.getLaunchIntentForPackage(player);
			startActivity(intent);
		}
	}
}
