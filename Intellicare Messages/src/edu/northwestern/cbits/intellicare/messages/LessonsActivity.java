package edu.northwestern.cbits.intellicare.messages;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LessonsActivity extends ConsentedActivity 
{
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_lessons);
		
		ListView list = (ListView) this.findViewById(R.id.list_view);
		
		Cursor cursor = this.getContentResolver().query(ContentProvider.LESSONS_URI, null, null, null, "lesson_order");
		
		ListAdapter adapter = new CursorAdapter(this, cursor)
		{
			public void bindView(View view, Context context, Cursor cursor) 
			{
				TextView title = (TextView) view.findViewById(R.id.lesson_title);
				
				title.setText(cursor.getString(cursor.getColumnIndex("title")));
			}

			public View newView(Context context, Cursor cursor, ViewGroup parent) 
			{
				LayoutInflater inflater = LayoutInflater.from(context);
				
				View view = inflater.inflate(R.layout.row_lesson, parent, false);
				
				this.bindView(view, context, cursor);
				
				return view;
			}
		};
		
		list.setAdapter(adapter);
		
		final LessonsActivity me = this;
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Intent lessonIntent = new Intent(me, LessonActivity.class);
				lessonIntent.putExtra(LessonActivity.LESSON_ID, id);
				
				String selection = "_id = ?";
				String[] selectionArgs = { "" + id };

				Cursor c = me.getContentResolver().query(ContentProvider.LESSONS_URI, null, selection, selectionArgs, null);
				
				if (c.moveToNext())
					lessonIntent.putExtra(LessonActivity.LESSON_TITLE, c.getString(c.getColumnIndex("title")));
				
				c.close();
				
				me.startActivity(lessonIntent);
			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_start, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_settings)
		{
			Toast.makeText(this, "TODO: Show Settings", Toast.LENGTH_LONG).show();
		}
		else if (item.getItemId() == R.id.action_help)
		{
			Toast.makeText(this, "TODO: Show Help", Toast.LENGTH_LONG).show();
		}
		else if (item.getItemId() == R.id.action_schedule)
		{
			Toast.makeText(this, "TODO: Show Schedule", Toast.LENGTH_LONG).show();
		}
		
		return true;
	}
}
