package edu.northwestern.cbits.intellicare.moveme;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.moveme.MoveProvider.CalendarEvent;

public class CalendarActivity extends ConsentedActivity 
{
    protected String _selectedExercise = null;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_calendar);
        
        this.getSupportActionBar().setTitle(R.string.title_calendar);
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	List<CalendarEvent> events = MoveProvider.events(this);
    	
    	if (events.size() != 1)
    		this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_calendar, events.size()));
    	else
    		this.getSupportActionBar().setSubtitle(R.string.subtitle_calendar_single);
    	
    	final CalendarActivity me = this;
    	
    	ListView list = (ListView) this.findViewById(R.id.list_view);
    	
        final ArrayAdapter<CalendarEvent> adapter = new ArrayAdapter<CalendarEvent>(this, R.layout.row_scheduled, events)
        {
        	public View getView (int position, View convertView, ViewGroup parent)
        	{
        		if (convertView == null)
        		{
    				LayoutInflater inflater = LayoutInflater.from(me);
    				convertView = inflater.inflate(R.layout.row_scheduled, parent, false);
        		}
        		
        		CalendarEvent event = this.getItem(position);

				TextView exerciseName = (TextView) convertView.findViewById(R.id.label_exercise);
				TextView exerciseTime = (TextView) convertView.findViewById(R.id.label_time);

				Date start = new Date(event.start);
				DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(me);				

				exerciseName.setText(timeFormat.format(start) + ": " + event.title);
				
				DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(me);

				exerciseTime.setText(dateFormat.format(start));

        		return convertView;
        	}
        };
        
        list.setAdapter(adapter);
        
        list.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> arg0, View arg1, int which, long arg3) 
			{
				CalendarEvent event = adapter.getItem(which);
				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.withAppendedPath(Events.CONTENT_URI, "" + event.id));  
				
				me.startActivity(intent);
			}
        });      
        
        TextView schedule = (TextView) this.findViewById(R.id.field_schedule_exercise);
        
        schedule.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				me.launchSchedule();
			}
        });
        
        list.setEmptyView(schedule);
    }
    
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_calendar, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
		if (item.getItemId() == R.id.action_calendar)
			this.launchSchedule();

		return super.onOptionsItemSelected(item);
    }

    
	@SuppressLint({ "InlinedApi", "NewApi" }) private void launchSchedule() 
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_calendar_error);
			builder.setMessage(R.string.message_calendar_error);
			
			builder.setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder.create().show();
			
			return;
		}

		final CalendarActivity me = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_schedule);
		
		final String[] items = { this.getString(R.string.item_aerobic_moderate), this.getString(R.string.item_aerobic_vigorous), this.getString(R.string.item_strengthening) };
		
		builder.setItems(items, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);

				builder.setTitle(items[which]);

				switch(which)
				{
					case 0:
						builder.setSingleChoiceItems(R.array.array_moderate_aerobic, -1, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = me.getResources().getStringArray(R.array.array_moderate_aerobic);

								me._selectedExercise   = exercises[which];
							}
						});

						break;
					case 1:
						builder.setSingleChoiceItems(R.array.array_vigorous_aerobic, -1, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = me.getResources().getStringArray(R.array.array_vigorous_aerobic);

								me._selectedExercise  = exercises[which];
							}
						});

						break;
					case 2:
						builder.setSingleChoiceItems(R.array.array_strengthening, -1, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface arg0, int which) 
							{
								String[] exercises = me.getResources().getStringArray(R.array.array_strengthening);

								me._selectedExercise  = exercises[which];
							}
						});

						break;
				}

				builder.setPositiveButton(R.string.action_schedule, new DialogInterface.OnClickListener()
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
}
