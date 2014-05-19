package edu.northwestern.cbits.intellicare.aspire;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class EditActivity extends ConsentedActivity 
{
	protected static final String CARD_ID = "card_id";

	private long _cardId = -1;
	private String _cardName = null;
	private String _cardDescription = null;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_edit);
		
		this._cardId = this.getIntent().getLongExtra(EditActivity.CARD_ID, -1);
		
		if (this._cardId != -1)
		{
			String where = AspireContentProvider.ID + " = ?";
			String[] args = { "" + this._cardId };
			
			Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, where, args, null);
			
			if (c.moveToNext())
			{
				this._cardName = c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME));
				this._cardDescription = c.getString(c.getColumnIndex(AspireContentProvider.CARD_DESCRIPTION));

				this.getSupportActionBar().setTitle(this._cardName);
				
				TextView description = (TextView) this.findViewById(R.id.card_description);
				description.setText(this.getString(R.string.description_path, this._cardDescription));
			}
		}
		else
			this.finish();
		
		this.getSupportActionBar().setSubtitle(R.string.subtitle_editor);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_editor", payload);

		this.refreshList();
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_editor", payload);
	}
	
	private void refreshList() 
	{
		final EditActivity me = this;

		String where = AspireContentProvider.PATH_CARD_ID + " = ?";
		String[] args = { "" + this._cardId };
		
		String[] from = { AspireContentProvider.PATH_PATH };
		int[] to = { android.R.id.text1 };
		
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, where, args, null);
		
		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, from, to, 0);	

		ListView list = (ListView) this.findViewById(R.id.list_view);
		
		list.setEmptyView(this.findViewById(R.id.label_empty_paths));
		
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, final long id) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.title_rename_task);

				LayoutInflater inflater = LayoutInflater.from(me);
				View contentView = inflater.inflate(R.layout.view_rename_path, null, false);
				
				builder.setView(contentView);
				
				final TextView rename = (TextView) contentView.findViewById(R.id.field_new_path);
				rename.setText(((TextView) view.findViewById(android.R.id.text1)).getText().toString());
				
				builder.setPositiveButton(R.string.action_rename, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						String where = AspireContentProvider.ID + " = ?";
						String[] args = { "" + id };
						
						ContentValues values = new ContentValues();
						values.put(AspireContentProvider.PATH_PATH, rename.getText().toString());
						
						me.getContentResolver().update(AspireContentProvider.ASPIRE_PATH_URI, values, where, args);
								
						HashMap<String, Object> payload = new HashMap<String, Object>();
						LogManager.getInstance(me).log("renamed_path", payload);

						me.refreshList();
					}
				});
				
				builder.setNegativeButton(R.string.action_cancel, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
			}
		});
		
		list.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, final long id) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.title_delete_task);
				builder.setMessage(R.string.message_delete_task);
				
				builder.setPositiveButton(R.string.action_delete, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						String where = AspireContentProvider.ID + " = ?";
						String[] args = { "" + id };
						
						me.getContentResolver().delete(AspireContentProvider.ASPIRE_PATH_URI, where, args);
								
						HashMap<String, Object> payload = new HashMap<String, Object>();
						LogManager.getInstance(me).log("deleted_path", payload);

						me.refreshList();
					}
				});
				
				builder.setNegativeButton(R.string.action_cancel, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
				
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("opened_edit_options", payload);

				return true;
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_edit, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_add_path:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_new_task);
				
				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.view_add_task, null, false);
				
				builder.setView(view);
				
				final EditActivity me = this;
				
				final EditText taskField = (EditText) view.findViewById(R.id.field_new_task);
				
				builder.setPositiveButton(R.string.action_add_task, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						ContentValues values = new ContentValues();
						values.put(AspireContentProvider.PATH_CARD_ID, me._cardId);
						values.put(AspireContentProvider.PATH_PATH, taskField.getText().toString().trim());
						
						me.getContentResolver().insert(AspireContentProvider.ASPIRE_PATH_URI, values);
						
						me.refreshList();
						
						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("path", taskField.getText().toString().trim());
						LogManager.getInstance(me).log("added_path", payload);
					}
				});
				
				builder.create().show();

				break;
			case R.id.action_close:
				this.finish();

				break;
		}
		
		return true;
	}
}
