package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class SleepDiaryActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_sleep_diaries);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_diaries);
		this.getSupportActionBar().setIcon(R.drawable.ic_launcher_plain);
	}
	
	public void onResume()
	{
		super.onResume();

		// ListView diaryList = (ListView) this.findViewById(R.id.list_diaries);
		
/*		Cursor c = this.getContentResolver().query(SlumberContentProvider.NOTES_URI, null, null, null, "timestamp DESC");
		
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
		
		final SleepDiaryActivity me = this;
		
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
		
		*/
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_sleep_diaries, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_add)
		{
			Intent intent = new Intent(this, AddSleepDiaryActivity.class);
			
			this.startActivity(intent);
		}

		return true;
	}

}
