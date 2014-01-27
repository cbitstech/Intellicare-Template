package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class SleepLogActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_sleep_log);
		
		this.getSupportActionBar().setTitle(R.string.title_sleep_log_activity);
	}
	
	public void onResume()
	{
		super.onResume();

		ListView logList = (ListView) this.findViewById(R.id.list_sleep_log);
		
		Cursor c = this.getContentResolver().query(SlumberContentProvider.NOTES_URI, null, null, null, "timestamp DESC");
		
		Log.e("ST", "CURSOR COUNT: " + c.getCount());
		
		int[] emptyInts = {};
		String[] emptyStrings = {};
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_sleep_log, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				Log.e("ST", "BIND VIEW");
				
				for (int i = 0; i < cursor.getColumnCount(); i++)
				{
					Log.e("PR", cursor.getColumnName(i) + " -> " + cursor.getString(i));
				}
				
				TextView logText = (TextView) view.findViewById(R.id.label_log_notes);
				logText.setText(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.NOTE_TEXT)));

				TextView time = (TextView) view.findViewById(R.id.label_log_time);
				time.setText("" + cursor.getString(cursor.getColumnIndex(SlumberContentProvider.NOTE_TIMESTAMP)));
			}
		};
		
		logList.setAdapter(adapter);
	}
}
