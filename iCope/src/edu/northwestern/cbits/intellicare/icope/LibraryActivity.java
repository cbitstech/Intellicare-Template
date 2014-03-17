package edu.northwestern.cbits.intellicare.icope;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LibraryActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_library);

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(R.string.title_card_library);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();
		
		ListView list = (ListView) this.findViewById(R.id.cards_list);
		
		Cursor c = this.getContentResolver().query(CopeContentProvider.CARD_URI, null, null, null, null);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_card, c, new String[0], new int[0])
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView event = (TextView) view.findViewById(R.id.label_reminder_event);
				TextView reminder = (TextView) view.findViewById(R.id.label_reminder_reminder);
				
				event.setText(cursor.getString(cursor.getColumnIndex(CopeContentProvider.CARD_EVENT)));
				reminder.setText(cursor.getString(cursor.getColumnIndex(CopeContentProvider.CARD_REMINDER)));
			}
		};

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setSubtitle(this.getString(R.string.subtitle_library, c.getCount()));
		
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{

			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_library, menu);

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
			case R.id.action_view_card:
				Intent cardIntent = new Intent(this, CardActivity.class);
				this.startActivity(cardIntent);
				
				break;
			default:
				break;
		}
		
		return true;
	}
}
