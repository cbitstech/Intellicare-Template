package edu.northwestern.cbits.intellicare.icope;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.icope.CopeContentProvider.Reminder;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		ScheduleManager.getInstance(this).updateSchedule();
	}
	
	protected void onResume()
	{
		super.onResume();
		
		CrashManager.register(this, "ba2344cc1da5b5500fc9b80b5d6abf77", new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_main", payload);

		this.refresh();
	}
	
	protected void onPause()
	{
		super.onPause();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_main", payload);
	}
	
	private void refresh()
	{
		ListView list = (ListView) this.findViewById(R.id.cards_list);
		
		final List<Reminder> reminders = CopeContentProvider.listUpcomingReminders(this);
		
		final MainActivity me = this;
		
		String[] items = new String[2];
		
		if (reminders.size() == 1)
			items = new String[1];
		else if (reminders.size() == 0)
			items = new String[0];
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_reminder, items)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				if (position == 0)
				{
					if (convertView == null)
					{
	    				LayoutInflater inflater = LayoutInflater.from(me);
	    				convertView = inflater.inflate(R.layout.row_reminder, parent, false);
					}
					
					Reminder r = reminders.get(position);
	
					DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(me);
					DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(me);
	
					TextView reminderTime = (TextView) convertView.findViewById(R.id.label_reminder_time);
	
					reminderTime.setText(dateFormat.format(r.date) + " @ " + timeFormat.format(r.date));
					
					String selection = CopeContentProvider.CARD_ID + " = ?";
					String selectionArgs[] = { "" + r.cardId };
					
					Cursor cardCursor = me.getContentResolver().query(CopeContentProvider.CARD_URI, null, selection, selectionArgs, null);
					
					if (cardCursor.moveToNext())
					{
						TextView event = (TextView) convertView.findViewById(R.id.label_reminder_event);
						TextView reminder = (TextView) convertView.findViewById(R.id.label_reminder_reminder);
						TextView type = (TextView) convertView.findViewById(R.id.label_reminder_type);
						
						event.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_EVENT)));
						reminder.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_REMINDER)));
						type.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_TYPE)));
					}
					
					cardCursor.close();
					
					return convertView;
				}
				else if(reminders.size() > 1)
				{
					if (convertView == null)
					{
	    				LayoutInflater inflater = LayoutInflater.from(me);
	    				convertView = inflater.inflate(R.layout.row_reminder_rest, parent, false);
					}
	
					TextView remainder = (TextView) convertView.findViewById(R.id.label_remainder);
					
					if (reminders.size() == 2)
						remainder.setText(R.string.message_single_remainder);
					else
						remainder.setText(me.getString(R.string.message_remainders, reminders.size() - 1));
					
					return convertView;
				}
				
				return null;
			}
		};

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(R.string.app_name);
		
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View row, int position, long id) 
			{
				if (position == 0)
				{
					Reminder r = reminders.get(0);
					
					Intent intent = new Intent(me, ViewCardActivity.class);
					intent.putExtra(ViewCardActivity.REMINDER_ID, r.reminderId);
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("reminder_id", r.reminderId);
					LogManager.getInstance(me).log("viewed_reminder", payload);
					
					me.startActivity(intent);
				}
			}
		});
		
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long id)
			{
				if (position == 0)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder.setTitle(R.string.title_delete_reminder);
					builder.setMessage(R.string.message_delete_reminder);
					
					builder.setPositiveButton(R.string.action_yes, new OnClickListener()
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{
							Reminder r = reminders.get(position);
							
							String where = CopeContentProvider.ID + " = ?";
							String[] args = { "" + r.reminderId };
							
							me.getContentResolver().delete(CopeContentProvider.REMINDER_URI, where, args);

							HashMap<String, Object> payload = new HashMap<String, Object>();
							payload.put("reminder_id", r.reminderId);
							LogManager.getInstance(me).log("deleted_reminder", payload);

							me.refresh();
						}
					});
					
					builder.setNegativeButton(R.string.action_no, new OnClickListener()
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{

						}
					});
					
					builder.create().show();
				}
				
				return true;
			}
		});
		
		list.setEmptyView(this.findViewById(R.id.view_empty));
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_main, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_add_message:
				Intent addIntent = new Intent(this, AddCardActivity.class);
				this.startActivity(addIntent);
				
				break;
			case R.id.action_view_messages:
				Intent libraryIntent = new Intent(this, LibraryActivity.class);
				this.startActivity(libraryIntent);
				
				break;
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				this.startActivity(settingsIntent);
				
				break;
			case R.id.action_feedback:
				this.sendFeedback(this.getString(R.string.app_name));
					
				break;
			case R.id.action_faq:
				this.showFaq(this.getString(R.string.app_name));
					
				break;
			default:
				break;
		}
		
		return true;
	}
}
