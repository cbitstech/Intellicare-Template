package edu.northwestern.cbits.intellicare.icope;

import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AddCardActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_add_card);
		this.getSupportActionBar().setTitle(R.string.title_add_card);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_add_card, menu);
		
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final AddCardActivity me = this;
		
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_save:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_schedule_reminder);
				
				LayoutInflater inflater = LayoutInflater.from(this);
				final View view = inflater.inflate(R.layout.view_schedule, null, false);
				
				builder.setView(view);
				
				builder.setPositiveButton(R.string.action_schedule, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
						TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
						
						int year = datePicker.getYear();
						int month = datePicker.getMonth();
						int day = datePicker.getDayOfMonth();
						
						int hour = timePicker.getCurrentHour();
						int minute = timePicker.getCurrentMinute();
						int second = 0;
						
						long id = me.saveCard();
						
						ContentValues values = new ContentValues();
						values.put(CopeContentProvider.REMINDER_CARD_ID, id);
						values.put(CopeContentProvider.REMINDER_YEAR, year);
						values.put(CopeContentProvider.REMINDER_MONTH, month);
						values.put(CopeContentProvider.REMINDER_DAY, day);
						values.put(CopeContentProvider.REMINDER_HOUR, hour);
						values.put(CopeContentProvider.REMINDER_MINUTE, minute);
						values.put(CopeContentProvider.REMINDER_SECOND, second);
						
						me.getContentResolver().insert(CopeContentProvider.REMINDER_URI, values);
						
						me.finish();
					}
				});

				builder.setNegativeButton(R.string.action_not_now, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me.saveCard();

						me.finish();
					}
				});

				builder.create().show();

				break;
			default:
				break;
		}
		
		return true;
	}

	protected long saveCard() 
	{
		EditText eventField = (EditText) this.findViewById(R.id.card_event);
		EditText reminderField = (EditText) this.findViewById(R.id.card_reminder);
		
		
		ContentValues values = new ContentValues();
		values.put(CopeContentProvider.CARD_EVENT, eventField.getText().toString());
		values.put(CopeContentProvider.CARD_REMINDER, reminderField.getText().toString());
		
		List<String> segments = this.getContentResolver().insert(CopeContentProvider.CARD_URI, values).getPathSegments();
		
		return Long.parseLong(segments.get(segments.size() - 1));
	}
}
