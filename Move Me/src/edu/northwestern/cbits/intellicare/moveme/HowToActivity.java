package edu.northwestern.cbits.intellicare.moveme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class HowToActivity extends ContentIndexActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		this.getSupportActionBar().setTitle(R.string.title_howto);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_howto);
	}

	protected int titlesArrayId() 
	{
		return R.array.array_howto_titles;
	}

	protected int urlsArrayId() 
	{
		return R.array.array_howto_urls;
	}
	
	protected void openUri(Uri uri, String title) 
	{
		if ("how_much.dialog".equalsIgnoreCase(uri.getLastPathSegment()))
			this.showHowMuchDialog();
		else if ("what_kind_exercise.dialog".equalsIgnoreCase(uri.getLastPathSegment()))
			this.showWhatKindExerciseDialog();
		else if ("when_active.dialog".equalsIgnoreCase(uri.getLastPathSegment()))
			this.showWhenActiveDialog();
		else
			super.openUri(uri, title);
	}

	private void showWhenActiveDialog() 
	{
		final HowToActivity me = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.title_when_active_1);

		builder.setItems(R.array.array_when_active, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				MoveMeActivity.launchSchedule(me);
			}
		});
		
		builder.create().show();
	}

	private void showWhatKindExerciseDialog() 
	{
		final HowToActivity me = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.title_what_kind_exercise_1);
		builder.setMessage(R.string.message_what_kind_exercise_1);
		
		builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.title_what_kind_exercise_1);
				builder.setMessage(R.string.message_what_kind_exercise_2);
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(me);
						
						builder.setTitle(R.string.prompt_physical_activity);
						
						builder.setItems(R.array.array_physical_endurance, new DialogInterface.OnClickListener() 
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
		});
		
		builder.create().show();
	}

	private void showHowMuchDialog() 
	{
		final HowToActivity me = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.title_how_much_1);
		builder.setMessage(R.string.message_how_much_1);
		
		builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.prompt_active_minutes);
				
				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			    View view = inflater.inflate(R.layout.view_exercise_active, null);
				
				final NumberPicker activeMinutes = (NumberPicker) view.findViewById(R.id.picker_active_minutes); 
				
				final NumberPicker.Formatter formatter = new NumberPicker.Formatter()
				{
					public String format(int value) 
					{
						if (value == 1)
							return me.getString(R.string.format_picker_minute);

						return me.getString(R.string.format_picker_minutes, value);
					}
				};
				
				activeMinutes.setMinValue(0);
				activeMinutes.setMaxValue(840);
				activeMinutes.setFormatter(formatter);

				builder.setView(view);

				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(me);
						
						builder.setTitle(R.string.prompt_future_minutes);
						
						LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

					    View view = inflater.inflate(R.layout.view_exercise_future, null);
						
						final NumberPicker futureMinutes = (NumberPicker) view.findViewById(R.id.picker_future_minutes); 
						
						futureMinutes.setMinValue(0);
						futureMinutes.setMaxValue(840);
						futureMinutes.setFormatter(formatter);

						builder.setView(view);

						builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								int active = activeMinutes.getValue();
								// int future = futureMinutes.getValue();
								
								AlertDialog.Builder builder = new AlertDialog.Builder(me);
								
								builder.setTitle(R.string.title_how_much_1);

								if (active <= 50)
									builder.setMessage(R.string.message_exercise_more);
								else
									builder.setMessage(R.string.message_exercise_okay);
								
								builder.setPositiveButton(R.string.action_schedule_now, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{
										MoveMeActivity.launchSchedule(me);
									}
								});
								
								builder.create().show();
							}
						});
						
						builder.create().show();
					}
				});
				
				builder.create().show();
			}
		});
		
		builder.create().show();
	}
}
