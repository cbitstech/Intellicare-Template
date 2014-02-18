package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class EditBedtimeChecklistActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_bedtime_checklist);
		
		this.getSupportActionBar().setTitle(R.string.tool_edit_bedtime_checklist);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_edit_bedtime_checklist);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();

		final EditBedtimeChecklistActivity me = this;

		final ListView checkList = (ListView) this.findViewById(R.id.list_checklist);
		
		Cursor c = this.getContentResolver().query(SlumberContentProvider.CHECKLIST_ITEMS_URI, null, null, null, "category, name");

		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_checklist_item, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, final Context context, Cursor cursor)
			{
				final SimpleCursorAdapter meAdapter = this;
				
				CheckBox item = (CheckBox) view.findViewById(R.id.check_item);
				
				final long id = cursor.getLong(cursor.getColumnIndex("_id"));
				
				item.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton button, boolean checked) 
					{

					}
				});
				
				item.setChecked(cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.CHECKLIST_ITEM_ENABLED)) != 0);

				item.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton button, boolean checked) 
					{
						ContentValues values = new ContentValues();
						values.put(SlumberContentProvider.CHECKLIST_ITEM_ENABLED, checked);
						
						String where = "_id = ?";
						String[] args = { "" + id };
						
						context.getContentResolver().update(SlumberContentProvider.CHECKLIST_ITEMS_URI, values, where, args);

						meAdapter.getCursor().requery();
					}
				});
				
				item.setOnLongClickListener(new OnLongClickListener()
				{
					public boolean onLongClick(View view) 
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(me);
						builder = builder.setTitle(R.string.title_delete_checklist_item);
						
						final CheckBox item = (CheckBox) view.findViewById(R.id.check_item);				
						
						builder = builder.setMessage(me.getString(R.string.message_delete_checklist_item, item.getText()));
						
						builder = builder.setPositiveButton(R.string.button_remove, new OnClickListener()
						{
							public void onClick(DialogInterface arg0, int arg1)
							{
								String selection = "_id = ?";
								String[] args = { "" + id };
								
								me.getContentResolver().delete(SlumberContentProvider.CHECKLIST_ITEMS_URI, selection, args);

								Toast.makeText(me, R.string.toast_checklist_item_removed, Toast.LENGTH_SHORT).show();

								HashMap<String, Object> payload = new HashMap<String, Object>();
								payload.put("item", item.getText());
								LogManager.getInstance(me).log("removed_checklist_item", payload);
								
								meAdapter.getCursor().requery();
								meAdapter.notifyDataSetChanged();
							}
						});

						builder = builder.setNegativeButton(R.string.button_cancel, new OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which) 
							{

							}
						});
						
						builder.show();

						return true;
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

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_checklist_edit_activity", payload);
	}
	
	protected void onPause()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_checklist_edit_activity", payload);
		
		super.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_edit_bedtime_checklist, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_add_item)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.view_add_item, null, false);
			
			final AutoCompleteTextView categories = (AutoCompleteTextView) view.findViewById(R.id.field_category);
			final EditText checklistItem = (EditText) view.findViewById(R.id.field_item);
			
			builder = builder.setTitle(R.string.title_add_checklist_item);
			builder = builder.setView(view);
			
			ArrayList<String> allCategories = new ArrayList<String>();
			
			String[] projection = { SlumberContentProvider.CHECKLIST_ITEM_CATEGORY };
 			
			Cursor c = this.getContentResolver().query(SlumberContentProvider.CHECKLIST_ITEMS_URI, projection, null, null, SlumberContentProvider.CHECKLIST_ITEM_CATEGORY);
			
			while (c.moveToNext())
			{
				String category = c.getString(0);
				
				if (allCategories.contains(category) == false)
					allCategories.add(category);
			}
			
			c.close();
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, allCategories);
			categories.setAdapter(adapter);
			categories.setThreshold(0);
			
			final EditBedtimeChecklistActivity me = this;
			
			builder = builder.setPositiveButton(R.string.button_save, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					String newCategory = categories.getText().toString().trim();
					String newItem = checklistItem.getText().toString().trim();
					
					ContentValues values = new ContentValues();
					values.put(SlumberContentProvider.CHECKLIST_ITEM_NAME, newItem);
					values.put(SlumberContentProvider.CHECKLIST_ITEM_CATEGORY, newCategory);
					
					me.getContentResolver().insert(SlumberContentProvider.CHECKLIST_ITEMS_URI, values);
					
					Toast.makeText(me, R.string.toast_checklist_item_added, Toast.LENGTH_SHORT).show();

					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("item", newItem);
					payload.put("category", newCategory);
					LogManager.getInstance(me).log("added_checklist_item", payload);

					me.onResume();
				}
			});

			builder = builder.setNegativeButton(R.string.button_discard, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder.create().show();
		}

		return true;
	}
}
