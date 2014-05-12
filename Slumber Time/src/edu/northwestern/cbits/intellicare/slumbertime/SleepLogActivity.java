package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SleepLogActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_sleep_log);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_log_notes);
	}
	
	@SuppressWarnings("deprecation")
	public void onResume()
	{
		super.onResume();

		ListView logList = (ListView) this.findViewById(R.id.list_sleep_log);
		
		Cursor c = this.getContentResolver().query(SlumberContentProvider.NOTES_URI, null, null, null, "timestamp DESC");
		
		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_sleep_log, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView logText = (TextView) view.findViewById(R.id.label_log_notes);
				logText.setText(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.NOTE_TEXT)));

				java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(context);
				java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
				
				Date now = new Date(cursor.getLong(cursor.getColumnIndex(SlumberContentProvider.NOTE_TIMESTAMP)));
				
				TextView time = (TextView) view.findViewById(R.id.label_log_time);
				time.setText(timeFormat.format(now) + " (" + dateFormat.format(now) + ")");
			}
		};
		
		logList.setAdapter(adapter);
		
		logList.setEmptyView(this.findViewById(R.id.empty_log_list));
		
		final SleepLogActivity me = this;
		
		logList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, final long id) 
			{
				Cursor cursor = (Cursor) parent.getItemAtPosition(position);
				
				java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(me);
				java.text.DateFormat timeFormat = DateFormat.getTimeFormat(me);
				
				Date now = new Date(cursor.getLong(cursor.getColumnIndex(SlumberContentProvider.NOTE_TIMESTAMP)));
				
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder = builder.setTitle(timeFormat.format(now) + " (" + dateFormat.format(now) + ")");
				builder = builder.setMessage(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.NOTE_TEXT)));
				
				builder = builder.setPositiveButton(R.string.button_close, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{

					}
				});
				
				builder = builder.setNegativeButton(R.string.button_delete_entry, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						String selection = "_id = ?";
						String[] args = { "" + id };
						
						me.getContentResolver().delete(SlumberContentProvider.NOTES_URI, selection, args);
						
						me.onResume();
					}
				});

				builder.create().show();
			}
		});
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_sleep_log_activity", payload);
	}
	
	protected void onPause()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_sleep_log_activity", payload);
		
		super.onPause();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_sleep_log, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final SleepLogActivity me = this;
		
		if (item.getItemId() == R.id.action_add)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder = builder.setTitle(R.string.title_clock_log);
			
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.view_clock_log, null, false);
			
			builder = builder.setView(view);
			builder.setNegativeButton(R.string.button_discard, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{

				}
			});
			
			final EditText logField = (EditText) view.findViewById(R.id.field_log_text);
			
			builder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					String logText = logField.getEditableText().toString().trim();
					
					if (logText.length() > 0)
					{
						long now = System.currentTimeMillis();
						
						ContentValues values = new ContentValues();
						values.put(SlumberContentProvider.NOTE_TEXT, logText);
						values.put(SlumberContentProvider.NOTE_TIMESTAMP, now);
						
						me.getContentResolver().insert(SlumberContentProvider.NOTES_URI, values);
						
						dialog.cancel();

						me.onResume();

						Toast.makeText(me, R.string.toast_note_saved, Toast.LENGTH_SHORT).show();
					}
					else
						Toast.makeText(me, R.string.toast_provide_note, Toast.LENGTH_SHORT).show();
				}
			});

			AlertDialog d = builder.create();
			d.show();
		}

		return true;
	}

}
