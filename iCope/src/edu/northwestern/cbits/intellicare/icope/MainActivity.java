package edu.northwestern.cbits.intellicare.icope;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();
		
		ListView list = (ListView) this.findViewById(R.id.cards_list);
		
		Cursor c = this.getContentResolver().query(CopeContentProvider.REMINDER_URI, null, null, null, null);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_reminder, c, new String[0], new int[0])
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView reminderTime = (TextView) view.findViewById(R.id.label_reminder_time);
				
				Calendar c = Calendar.getInstance();
				c.set(Calendar.YEAR, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_YEAR)));
				c.set(Calendar.MONTH, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MONTH)));
				c.set(Calendar.DAY_OF_MONTH, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_DAY)));
				c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_HOUR)));
				c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MINUTE)));
				c.set(Calendar.SECOND, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_SECOND)));
				
				reminderTime.setText(c.getTime().toString());
				
				String selection = CopeContentProvider.CARD_ID + " = ?";
				String selectionArgs[] = { cursor.getString(cursor.getColumnIndex(CopeContentProvider.REMINDER_CARD_ID)) };
				
				Cursor cardCursor = context.getContentResolver().query(CopeContentProvider.CARD_URI, null, selection, selectionArgs, null);
				
				if (cardCursor.moveToNext())
				{
					TextView event = (TextView) view.findViewById(R.id.label_reminder_event);
					TextView reminder = (TextView) view.findViewById(R.id.label_reminder_reminder);
					
					event.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_EVENT)));
					reminder.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_REMINDER)));
				}
				
				cardCursor.close();
			}
		};

		java.text.DateFormat todayFormat = DateFormat.getMediumDateFormat(this);

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(todayFormat.format(new Date()));
		actionBar.setSubtitle(this.getString(R.string.subtitle_today_messages, c.getCount()));
		
		list.setAdapter(adapter);
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
			default:
				break;
		}
		
		return true;
	}
}
