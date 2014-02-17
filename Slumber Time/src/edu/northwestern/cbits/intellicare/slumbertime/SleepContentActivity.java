package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class SleepContentActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_content_entries);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_content);
	}
	
	@SuppressWarnings("deprecation")
	public void onResume()
	{
		super.onResume();

		final SleepContentActivity me = this;
		
		ListView entriesList = (ListView) this.findViewById(R.id.list_entries);
		
		Cursor c = this.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, null, null, EntriesContentProvider.TITLE);

		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};
		
		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_content_entry, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView entryName = (TextView) view.findViewById(R.id.entry_name);
				entryName.setText(cursor.getString(cursor.getColumnIndex(EntriesContentProvider.TITLE)));
			}
		};
		
		entriesList.setAdapter(adapter);
		
		entriesList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Cursor c = adapter.getCursor();
				
				c.moveToPosition(position);

				Uri u = Uri.parse("intellicare://slumber/content/" + c.getString(c.getColumnIndex(EntriesContentProvider.SLUG)));
				
				Intent intent = new Intent(Intent.ACTION_VIEW, u);
				me.startActivity(intent);
			}
		});
	}
}
