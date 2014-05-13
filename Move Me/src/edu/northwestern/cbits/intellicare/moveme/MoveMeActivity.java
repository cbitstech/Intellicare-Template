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
import android.provider.CalendarContract.Events;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MoveMeActivity extends ConsentedActivity 
{
    protected String _selectedExercise = null;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_move_me);
        
        ActionBar actionBar = this.getSupportActionBar();
        
        actionBar.setTitle(R.string.title_move_me);
        
		final MoveMeActivity me = this;

        TextView howtos = (TextView) this.findViewById(R.id.button_howtos);
        TextView activities = (TextView) this.findViewById(R.id.button_activities);
        TextView motivators = (TextView) this.findViewById(R.id.button_motivators);
        TextView doitnow = (TextView) this.findViewById(R.id.button_doitnow);

        TextView schedule = (TextView) this.findViewById(R.id.button_schedule);
        
        schedule.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v) 
			{
				me.launchSchedule();
			}
        });
    }
    
	@SuppressLint({ "InlinedApi", "NewApi" }) private void launchSchedule() 
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_calendar_error);
			builder.setMessage(R.string.message_calendar_error);
			
			builder.setPositiveButton(R.string.action_close, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder.create().show();
			
			return;
		}

		final MoveMeActivity me = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_schedule);
		
		final String[] items = { this.getString(R.string.item_aerobic_moderate), this.getString(R.string.item_aerobic_vigorous), this.getString(R.string.item_strengthening) };
		
		builder.setItems(items, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);

				builder.setTitle(items[which]);

				switch(which)
				{
					case 0:
						builder.setSingleChoiceItems(R.array.array_moderate_aerobic, -1, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = me.getResources().getStringArray(R.array.array_moderate_aerobic);

								me._selectedExercise  = exercises[which];
							}
						});

						break;
					case 1:
						builder.setSingleChoiceItems(R.array.array_vigorous_aerobic, -1, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = me.getResources().getStringArray(R.array.array_vigorous_aerobic);

								me._selectedExercise  = exercises[which];
							}
						});

						break;
					case 2:
						builder.setSingleChoiceItems(R.array.array_strengthening, -1, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = me.getResources().getStringArray(R.array.array_strengthening);

								me._selectedExercise  = exercises[which];
							}
						});

						break;
				}

				builder.setPositiveButton(R.string.action_schedule, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						Intent intent = new Intent(Intent.ACTION_INSERT);
						intent.setData(Events.CONTENT_URI);
						
						if (me._selectedExercise == null)
							me._selectedExercise = me.getString(R.string.exercise_unknown);
						
						intent.putExtra(Events.TITLE, me._selectedExercise);
						intent.putExtra(Events.DESCRIPTION, me.getString(R.string.event_description));
						
						me.startActivity(intent);
					}
				});

				builder.create().show();
			}
		});
		
		builder.create().show();
	}

	private void launchLessons() 
	{
		final MoveMeActivity me = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_learn_more);
		
		String[] items = { this.getString(R.string.item_lessons), this.getString(R.string.item_videos) };
		
		builder.setItems(items, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				switch(which)
				{
					case 0:
						Intent lessonsIntent = new Intent(me, LessonsActivity.class);
						
						me.startActivity(lessonsIntent);

						break;
					case 1:
						Toast.makeText(me, "ToDO: List ViDeOS", Toast.LENGTH_LONG).show();
						break;
				}
			}
		});
		
		builder.create().show();
	}

	@SuppressLint({ "InlinedApi", "NewApi" }) 
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
