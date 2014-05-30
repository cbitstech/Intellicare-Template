package edu.northwestern.cbits.intellicare.moveme;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MoveMeActivity extends ConsentedActivity 
{
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
				MoveMeActivity.launchSchedule(me);
			}
        });
        
        motivators.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View v) 
			{
				Intent intent = new Intent(me, MotivatorsActivity.class);
				me.startActivity(intent);
			}
		});

        howtos.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View v) 
			{
				Intent intent = new Intent(me, HowToActivity.class);
				me.startActivity(intent);
			}
		});

        doitnow.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View v) 
			{
				Intent intent = new Intent(me, DoItNowActivity.class);
				me.startActivity(intent);
			}
		});

        activities.setOnClickListener(new View.OnClickListener() 
        {
			public void onClick(View v) 
			{
				me.showActivitiesDialog();
			}
		});
    }
    
	protected void showActivitiesDialog() 
	{
		final MoveMeActivity me = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.title_activity_dialog);
		builder.setMessage(R.string.message_activity_dialog);
		
		builder.setNegativeButton(R.string.actions_activities_enjoy, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.title_activity_enjoy);
				builder.setMultiChoiceItems(R.array.array_activities, null, new DialogInterface.OnMultiChoiceClickListener()
				{
					public void onClick(DialogInterface dialog, int which, boolean checked) 
					{

					}
				});
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
			}
		});

		builder.setPositiveButton(R.string.actions_activities_try, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.title_activity_enjoy);
				builder.setMultiChoiceItems(R.array.array_activities, null, new DialogInterface.OnMultiChoiceClickListener()
				{
					public void onClick(DialogInterface dialog, int which, boolean checked) 
					{

					}
				});
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
			}
		});

		builder.create().show();
	}

	@SuppressLint({ "InlinedApi", "NewApi" }) 
	protected static void launchSchedule(final Activity activity) 
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
		
		final StringBuffer selected = new StringBuffer();

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_schedule);
		
		final String[] items = { activity.getString(R.string.item_aerobic_moderate), activity.getString(R.string.item_aerobic_vigorous), activity.getString(R.string.item_strengthening) };
		
		builder.setItems(items, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);

				builder.setTitle(items[which]);

				switch(which)
				{
					case 0:
						builder.setSingleChoiceItems(R.array.array_moderate_aerobic, -1, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = activity.getResources().getStringArray(R.array.array_moderate_aerobic);

								selected.delete(0, selected.length() - 1);
								selected.append(exercises[which]);
							}
						});

						break;
					case 1:
						builder.setSingleChoiceItems(R.array.array_vigorous_aerobic, -1, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = activity.getResources().getStringArray(R.array.array_vigorous_aerobic);

								selected.delete(0, selected.length() - 1);
								selected.append(exercises[which]);
							}
						});

						break;
					case 2:
						builder.setSingleChoiceItems(R.array.array_strengthening, -1, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = activity.getResources().getStringArray(R.array.array_strengthening);

								selected.delete(0, selected.length() - 1);
								selected.append(exercises[which]);
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
						
						intent.putExtra(Events.TITLE, selected.toString());
						intent.putExtra(Events.DESCRIPTION, activity.getString(R.string.event_description));
						
						activity.startActivity(intent);
					}
				});

				builder.create().show();
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
