package edu.northwestern.cbits.intellicare.slumbertime;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SleepDiaryActivity extends ConsentedActivity
{
	protected static final long DAY_LENGTH = (1000 * 3600 * 24);

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_sleep_diaries);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_diaries);
	}

	@SuppressWarnings("deprecation")
	public void onResume()
	{
		super.onResume();

		ListView diaryList = (ListView) this.findViewById(R.id.list_diaries);
		
		Cursor c = this.getContentResolver().query(SlumberContentProvider.SLEEP_DIARIES_URI, null, null, null, "timestamp DESC");
		
		if (c.getCount() == 0)
		{
			Intent intent = new Intent(this, AddSleepDiaryActivity.class);

			this.startActivity(intent);
		}

		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};
		
		final DecimalFormat f = new DecimalFormat("#.#");
		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_sleep_diary, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(context);
				java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);

				Date now = new Date(cursor.getLong(cursor.getColumnIndex(SlumberContentProvider.DIARY_TIMESTAMP)));

				TextView timeText = (TextView) view.findViewById(R.id.label_time);
				timeText.setText(timeFormat.format(now));
				
				TextView dateText = (TextView) view.findViewById(R.id.label_date);
				dateText.setText(dateFormat.format(now));

				TextView efficiencyText = (TextView) view.findViewById(R.id.label_efficiency);

				efficiencyText.setText(f.format(SlumberContentProvider.scoreSleep(cursor, 100)) + "%");
			}
		};
		
		diaryList.setAdapter(adapter);
		
		final SleepDiaryActivity me = this;
		
		diaryList.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
			{
				final String selection = "_id = ?";
				final String[] selectionArgs = { "" + id };
				
				Cursor c = me.getContentResolver().query(SlumberContentProvider.SLEEP_DIARIES_URI, null, selection, selectionArgs, null);
				
				if (c.moveToNext())
				{
					java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(me);
					java.text.DateFormat timeFormat = DateFormat.getTimeFormat(me);
	
					Date now = new Date(c.getLong(c.getColumnIndex(SlumberContentProvider.DIARY_TIMESTAMP)));

					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					
					builder = builder.setTitle(me.getString(R.string.title_delete_diary));
					builder = builder.setMessage(me.getString(R.string.message_delete_diary, dateFormat.format(now), timeFormat.format(now)));
					
					builder = builder.setPositiveButton(me.getString(R.string.button_delete_diary), new OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							me.getContentResolver().delete(SlumberContentProvider.SLEEP_DIARIES_URI, selection, selectionArgs);
							
							Cursor c = me.getContentResolver().query(SlumberContentProvider.SLEEP_DIARIES_URI, null, null, null, "timestamp DESC");
							Cursor old = adapter.swapCursor(c);
							adapter.notifyDataSetChanged();
							
							old.close();
						}
					});

					builder = builder.setNegativeButton(me.getString(R.string.button_cancel), new OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) 
						{
						}
					});
					
					builder.create().show();
				}
				
				c.close();

				return false;
			}
		});
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_sleep_diaries_activity", payload);
	}
	
	protected void onPause()
	{
		super.onPause();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_sleep_diaries_activity", payload);
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
