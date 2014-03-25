package edu.northwestern.cbits.intellicare.icope;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class LibraryActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_library);

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(R.string.title_card_library);
	}
	
	protected void onResume()
	{
		super.onResume();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_library", payload);

		this.refreshList();
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_library", payload);
	}
	
	@SuppressWarnings("deprecation")
	private void refreshList()
	{
		final LibraryActivity me = this;
		
		ListView list = (ListView) this.findViewById(R.id.cards_list);
		
		Cursor c = this.getContentResolver().query(CopeContentProvider.CARD_URI, null, null, null, null);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_card, c, new String[0], new int[0])
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView event = (TextView) view.findViewById(R.id.label_reminder_event);
				TextView reminder = (TextView) view.findViewById(R.id.label_reminder_reminder);
				TextView type = (TextView) view.findViewById(R.id.label_reminder_type);
				
				event.setText(cursor.getString(cursor.getColumnIndex(CopeContentProvider.CARD_EVENT)));
				reminder.setText(cursor.getString(cursor.getColumnIndex(CopeContentProvider.CARD_REMINDER)));
				type.setText(cursor.getString(cursor.getColumnIndex(CopeContentProvider.CARD_TYPE)));
				
				final long id = cursor.getLong(cursor.getColumnIndex(CopeContentProvider.ID));
				
				TextView scheduleLink = (TextView) view.findViewById(R.id.link_schedule);
				
				scheduleLink.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View arg0) 
					{
						AddCardActivity.scheduleCard(me, id, false);
					}
				});
			}
		};

		ActionBar actionBar = this.getSupportActionBar();

		if (c.getCount() == 1)
			actionBar.setSubtitle(R.string.subtitle_library_single);
		else
			actionBar.setSubtitle(this.getString(R.string.subtitle_library, c.getCount()));
		
		list.setAdapter(adapter);
		
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.title_card_actions);
				
				builder.setItems(R.array.list_card_options, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						switch(which)
						{
							case 0:
								AddCardActivity.scheduleCard(me, id, false);

								break;
							case 1:
								Intent editIntent = new Intent(me, AddCardActivity.class);
								editIntent.putExtra(AddCardActivity.CARD_ID, id);
								
								me.startActivity(editIntent);
								
								break;
							case 2:
								AlertDialog.Builder builder = new AlertDialog.Builder(me);
								builder.setTitle(R.string.title_confirm_delete);
								builder.setMessage(R.string.message_confirm_delete);
								
								builder.setNegativeButton(R.string.action_no, new OnClickListener()
								{
									public void onClick(DialogInterface arg0, int arg1) 
									{

									}
								});

								builder.setPositiveButton(R.string.action_yes, new OnClickListener()
								{
									public void onClick(DialogInterface arg0, int arg1) 
									{
										String where = CopeContentProvider.ID + " = ?";
										String[] args = { "" + id };
										
										me.getContentResolver().delete(CopeContentProvider.CARD_URI, where, args);

										where = CopeContentProvider.CARD_ID + " = ?";
										me.getContentResolver().delete(CopeContentProvider.REMINDER_URI, where, args);
										
										HashMap<String, Object> payload = new HashMap<String, Object>();
										payload.put("card_id", id);
										LogManager.getInstance(me).log("deleted_card", payload);

										me.refreshList();
									}
								});

								builder.create().show();
								
								break;
						}
					}
				});
				
				builder.create().show();
				
				return true;
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
			default:
				break;
		}
		
		return true;
	}
}
