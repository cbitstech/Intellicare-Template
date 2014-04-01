package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class EditFeatsChecklistActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_feats_checklist);
		
		this.getSupportActionBar().setTitle(R.string.tool_edit_feats_checklist);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_edit_feats_checklist);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();

		LogManager.getInstance(this).log("opened_feats_editor", null);

		final EditFeatsChecklistActivity me = this;

		final ListView checkList = (ListView) this.findViewById(R.id.list_checklist);
		
		Cursor c = this.getContentResolver().query(FeatsProvider.FEATS_URI, null, null, null, "feat_level, feat_name");

		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
		final int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 2);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_feat_checkbox, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, final Context context, Cursor cursor)
			{
				final SimpleCursorAdapter meAdapter = this;
				
				final CheckBox item = (CheckBox) view.findViewById(R.id.feat_checkbox);
				
				final long id = cursor.getLong(cursor.getColumnIndex("_id"));
				
				item.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton button, boolean checked) 
					{

					}
				});
				
				item.setChecked(cursor.getInt(cursor.getColumnIndex("enabled")) != 0);

				item.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton button, boolean checked) 
					{
						ContentValues values = new ContentValues();
						values.put("enabled", checked);
						
						String where = "_id = ?";
						String[] args = { "" + id };
						
						context.getContentResolver().update(FeatsProvider.FEATS_URI, values, where, args);

						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("feat", button.getText().toString());
						
						if (checked)
							LogManager.getInstance(me).log("enabled_feat", payload);
						else
							LogManager.getInstance(me).log("disabled_feat", payload);

						meAdapter.getCursor().requery();
					}
				});
				
				int featLevel = cursor.getInt(cursor.getColumnIndex("feat_level"));

				item.setEnabled(true);

				if (featLevel == 0)
				{
					item.setOnLongClickListener(new OnLongClickListener()
					{
						public boolean onLongClick(View view) 
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(me);
							builder = builder.setTitle(R.string.title_delete_feat);
							
							final CheckBox item = (CheckBox) view.findViewById(R.id.feat_checkbox);				
							
							builder = builder.setMessage(me.getString(R.string.message_delete_feat, item.getText()));
							
							builder = builder.setPositiveButton(R.string.button_remove, new OnClickListener()
							{
								public void onClick(DialogInterface arg0, int arg1)
								{
									String selection = "_id = ?";
									String[] args = { "" + id };
									
									me.getContentResolver().delete(FeatsProvider.FEATS_URI, selection, args);

									Toast.makeText(me, R.string.toast_feat_removed, Toast.LENGTH_SHORT).show();

									meAdapter.getCursor().requery();
									meAdapter.notifyDataSetChanged();
									
									HashMap<String, Object> payload = new HashMap<String, Object>();
									payload.put("feat", item.getText().toString());

									LogManager.getInstance(me).log("deleted_feat", payload);
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
				}
				else if (featLevel == 99)
				{
					item.setOnLongClickListener(null);
					item.setEnabled(false);
				}
				else
					item.setOnLongClickListener(null);

				item.setText(cursor.getString(cursor.getColumnIndex("feat_name")));
				
				LinearLayout categoryRow = (LinearLayout) view.findViewById(R.id.label_category_row);

				TextView categoryLabel = (TextView) view.findViewById(R.id.label_category_name);
				
				if (featLevel == 0)
					categoryLabel.setText(R.string.label_category_my_feats);
				else if (featLevel == 99)
					categoryLabel.setText(R.string.label_category_automatic);
				else
					categoryLabel.setText(context.getString(R.string.label_category, featLevel));

				categoryRow.setVisibility(View.GONE);

				if (cursor.moveToPrevious() == false)
					categoryRow.setVisibility(View.VISIBLE);
				else
				{
					int nextLevel = cursor.getInt(cursor.getColumnIndex("feat_level"));
					
					if (featLevel != nextLevel)
						categoryRow.setVisibility(View.VISIBLE);
					
					cursor.moveToNext();
				}
				
				ImageView recommended = (ImageView) view.findViewById(R.id.icon_recommended);
				
				if (featLevel == level || featLevel == 0)
					recommended.setVisibility(View.VISIBLE);
				else
					recommended.setVisibility(View.GONE);
				
				recommended.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v) 
					{
						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("feat", item.getText().toString());

						LogManager.getInstance(me).log("tapped_recommendation", payload);

						Toast.makeText(me, R.string.toast_recommended_feat, Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		
		checkList.setAdapter(adapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_edit_feats_checklist, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_add_item)
		{
			LogManager.getInstance(this).log("started_add_feat", null);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.view_add_item, null, false);
			
			final EditText checklistItem = (EditText) view.findViewById(R.id.field_item);
			
			builder = builder.setTitle(R.string.title_add_feat_item);
			builder = builder.setView(view);
			
			final EditFeatsChecklistActivity me = this;
			
			builder = builder.setPositiveButton(R.string.button_save, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					String newItem = checklistItem.getText().toString().trim();
					
					ContentValues values = new ContentValues();
					values.put("feat_name", newItem);
					values.put("feat_level", 0);
					
					me.getContentResolver().insert(FeatsProvider.FEATS_URI, values);
					
					Toast.makeText(me, R.string.toast_feat_added, Toast.LENGTH_SHORT).show();

					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("feat", newItem);

					LogManager.getInstance(me).log("added_feat", payload);

					me.onResume();
				}
			});

			builder = builder.setNegativeButton(R.string.button_cancel, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					LogManager.getInstance(me).log("cancelled_add", null);
				}
			});
			
			builder.create().show();
		}

		return true;
	}
}
