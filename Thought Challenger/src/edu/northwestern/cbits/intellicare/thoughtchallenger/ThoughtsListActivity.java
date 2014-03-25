package edu.northwestern.cbits.intellicare.thoughtchallenger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ThoughtsListActivity extends ConsentedActivity 
{
	private int _selectedOption = 0;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_list);
		
		this.getSupportActionBar().setTitle(R.string.title_thoughts_list);
		
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_thoughts_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_thoughts, c.getCount()));

		c.close();
		
		this.refreshList();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_list, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		final ThoughtsListActivity me = this;
		
		switch (itemId)
		{
			case R.id.action_close:
				this.finish();
				break;
			case R.id.action_display:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_display_options);
				
				String[] options = this.getResources().getStringArray(R.array.list_display_options);
				
				builder.setSingleChoiceItems(options, this._selectedOption, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						me._selectedOption = which;
						
						me.refreshList();
					}
				});
				
				builder.setPositiveButton(R.string.action_close, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();

				break;
		}
		
		
		return true;
	}

	protected void refreshList() 
	{
		ListView listView = (ListView) this.findViewById(R.id.list_view);
		
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, ThoughtContentProvider.ID);

		String[] from = { ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT, ThoughtContentProvider.PAIR_RATIONAL_RESPONSE };
		int[] to = { R.id.label_thought, R.id.label_response };

		final ThoughtsListActivity me = this;

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_pair, c, from, to, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				super.bindView(view, context, cursor);
				
				TextView thought = (TextView) view.findViewById(R.id.label_thought);
				TextView response = (TextView) view.findViewById(R.id.label_response);

				thought.setVisibility(View.VISIBLE);
				response.setVisibility(View.VISIBLE);

				switch (me._selectedOption)
				{
					case 1:
						response.setVisibility(View.GONE);
						break;
					case 2:
						thought.setVisibility(View.GONE);
						break;
				}
			}
		};
		
		listView.setAdapter(adapter);
	}
}
