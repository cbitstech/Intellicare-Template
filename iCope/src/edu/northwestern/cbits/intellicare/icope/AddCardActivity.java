package edu.northwestern.cbits.intellicare.icope;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AddCardActivity extends ConsentedActivity 
{
	protected static final String CARD_ID = "card_id";

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
	
	protected void onResume()
	{
		super.onResume();

		AutoCompleteTextView cardType = (AutoCompleteTextView) this.findViewById(R.id.card_event_type);
		
		List<String> cardTypes = CopeContentProvider.listCardTypes(this);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, cardTypes);
		
		cardType.setAdapter(adapter);

		Intent intent = this.getIntent();
		
		if (intent.hasExtra(AddCardActivity.CARD_ID))
		{
			EditText eventField = (EditText) this.findViewById(R.id.card_event);
			EditText reminderField = (EditText) this.findViewById(R.id.card_reminder);
			
			String where = CopeContentProvider.ID + " = ?";
			String[] args = { "" + intent.getLongExtra(AddCardActivity.CARD_ID, 0) };
			
			Cursor c = this.getContentResolver().query(CopeContentProvider.CARD_URI, null, where, args, null);
			
			if (c.moveToNext())
			{
				eventField.setText(c.getString(c.getColumnIndex(CopeContentProvider.CARD_EVENT)));
				reminderField.setText(c.getString(c.getColumnIndex(CopeContentProvider.CARD_REMINDER)));
				cardType.setText(c.getString(c.getColumnIndex(CopeContentProvider.CARD_TYPE)));
			}
			
			c.close();
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final AddCardActivity me = this;
		
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_save:
				long id = me.saveCard();

				AddCardActivity.scheduleCard(me, id, true);

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
		AutoCompleteTextView cardType = (AutoCompleteTextView) this.findViewById(R.id.card_event_type);
		
		ContentValues values = new ContentValues();
		values.put(CopeContentProvider.CARD_EVENT, eventField.getText().toString());
		values.put(CopeContentProvider.CARD_REMINDER, reminderField.getText().toString());
		values.put(CopeContentProvider.CARD_TYPE, cardType.getText().toString());

		Intent intent = this.getIntent();
		
		if (intent.hasExtra(AddCardActivity.CARD_ID))
		{
			long cardId = intent.getLongExtra(AddCardActivity.CARD_ID, 0);
			
			String where = CopeContentProvider.ID + " = ?";
			String[] args = { "" + cardId };
			
			this.getContentResolver().update(CopeContentProvider.CARD_URI, values, where, args);
			
			return cardId;
		}
		else
		{
			List<String> segments = this.getContentResolver().insert(CopeContentProvider.CARD_URI, values).getPathSegments();
		
			return Long.parseLong(segments.get(segments.size() - 1));
		}
	}

	public static void scheduleCard(final Activity activity, final long id, final boolean finish) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_schedule_reminder);
		
		LayoutInflater inflater = LayoutInflater.from(activity);
		final View view = inflater.inflate(R.layout.view_schedule, null, false);
		
		builder.setView(view);
		
		builder.setPositiveButton(R.string.action_schedule, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
				
				CheckBox sunday = (CheckBox) view.findViewById(R.id.check_sun);
				CheckBox monday = (CheckBox) view.findViewById(R.id.check_mon);
				CheckBox tuesday = (CheckBox) view.findViewById(R.id.check_tue);
				CheckBox wednesday = (CheckBox) view.findViewById(R.id.check_wed);
				CheckBox thursday = (CheckBox) view.findViewById(R.id.check_thu);
				CheckBox friday = (CheckBox) view.findViewById(R.id.check_fri);
				CheckBox saturday = (CheckBox) view.findViewById(R.id.check_sat);
				
				int hour = timePicker.getCurrentHour();
				int minute = timePicker.getCurrentMinute();
				
				ContentValues values = new ContentValues();
				values.put(CopeContentProvider.REMINDER_CARD_ID, id);
				values.put(CopeContentProvider.REMINDER_SUNDAY, sunday.isChecked());
				values.put(CopeContentProvider.REMINDER_MONDAY, monday.isChecked());
				values.put(CopeContentProvider.REMINDER_TUESDAY, tuesday.isChecked());
				values.put(CopeContentProvider.REMINDER_WEDNESDAY, wednesday.isChecked());
				values.put(CopeContentProvider.REMINDER_THURSDAY, thursday.isChecked());
				values.put(CopeContentProvider.REMINDER_FRIDAY, friday.isChecked());
				values.put(CopeContentProvider.REMINDER_SATURDAY, saturday.isChecked());
				values.put(CopeContentProvider.REMINDER_HOUR, hour);
				values.put(CopeContentProvider.REMINDER_MINUTE, minute);
				
				activity.getContentResolver().insert(CopeContentProvider.REMINDER_URI, values);
				
				if (finish)
					activity.finish();
			}
		});

		builder.setNegativeButton(R.string.action_not_now, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				if (finish)
					activity.finish();
			}
		});

		builder.create().show();
	}
}
