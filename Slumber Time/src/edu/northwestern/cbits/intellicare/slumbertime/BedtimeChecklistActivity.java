package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class BedtimeChecklistActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_bedtime_checklist);
		
		this.getSupportActionBar().setTitle(R.string.tool_bedtime_checklist);
		this.getSupportActionBar().setIcon(R.drawable.ic_launcher_plain);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();
		
		ListView checkList = (ListView) this.findViewById(R.id.list_checklist);
		
		String selection = SlumberContentProvider.CHECKLIST_ITEM_ENABLED + " = ?";
		String[] args = { "1" };
		
		Cursor c = this.getContentResolver().query(SlumberContentProvider.CHECKLIST_ITEMS_URI, null, selection, args, "category, name");

		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_checklist_item, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, final Context context, Cursor cursor)
			{
				CheckBox item = (CheckBox) view.findViewById(R.id.check_item);

				item.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton view, boolean checked) 
					{
						
					}
				});

				long now = System.currentTimeMillis();
				long start = now - (1000 * 60 * 60 * 6);

				final long id = cursor.getLong(cursor.getColumnIndex("_id"));
				final String selection = SlumberContentProvider.CHECKLIST_EVENT_ITEM_ID + " = ? AND " + SlumberContentProvider.CHECKLIST_EVENT_TIMESTAMP + " > ?";
				final String[] args = { "" + id, "" + start };
				
				Cursor eventCursor = context.getContentResolver().query(SlumberContentProvider.CHECKLIST_EVENTS_URI, null, selection, args, null);
				item.setChecked(eventCursor.getCount() > 0);
				eventCursor.close();

				item.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton view, boolean checked) 
					{
						Cursor eventCursor = context.getContentResolver().query(SlumberContentProvider.CHECKLIST_EVENTS_URI, null, selection, args, null);
						
						if (checked)
						{
							if (eventCursor.getCount() == 0)
							{
								long now = System.currentTimeMillis();

								ContentValues values = new ContentValues();
								values.put(SlumberContentProvider.CHECKLIST_EVENT_ITEM_ID, id);
								values.put(SlumberContentProvider.CHECKLIST_EVENT_TIMESTAMP, now);
								
								context.getContentResolver().insert(SlumberContentProvider.CHECKLIST_EVENTS_URI, values);
							}
						}
						else
							context.getContentResolver().delete(SlumberContentProvider.CHECKLIST_EVENTS_URI, selection, args);							
						
						eventCursor.close();
					}
				});
				
				item.setText(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.CHECKLIST_ITEM_NAME)));
				
				TextView categoryLabel = (TextView) view.findViewById(R.id.label_category_name);
				categoryLabel.setVisibility(View.GONE);
				
				String category = cursor.getString(cursor.getColumnIndex(SlumberContentProvider.CHECKLIST_ITEM_CATEGORY));
				categoryLabel.setText(category);
				
				if (cursor.moveToPrevious())
				{
					String lastCategory = cursor.getString(cursor.getColumnIndex(SlumberContentProvider.CHECKLIST_ITEM_CATEGORY));
					
					if (category.equals(lastCategory) == false)
						categoryLabel.setVisibility(View.VISIBLE);
					
					cursor.moveToNext();
				}
				else
					categoryLabel.setVisibility(View.VISIBLE);
			}
		};
		
		checkList.setAdapter(adapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_bedtime_checklist, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_edit_list)
		{
			Intent editIntent = new Intent(this, EditBedtimeChecklistActivity.class);
			
			this.startActivity(editIntent);
		}
		
		return true;
	}
}
