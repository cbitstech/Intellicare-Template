package edu.northwestern.cbits.intellicare.moveme;

import java.util.HashMap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public abstract class ContentIndexActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_content_entries);
	}
	
	public void onResume()
	{
		super.onResume();

		final ContentIndexActivity me = this;
		
		ListView entriesList = (ListView) this.findViewById(R.id.list_entries);
		
		final String[] titles = this.getResources().getStringArray(this.titlesArrayId());
		final String[] urls = this.getResources().getStringArray(this.urlsArrayId());
		
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_lesson, titles)
		{
			public View getView (int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
    				LayoutInflater inflater = LayoutInflater.from(me);
    				convertView = inflater.inflate(R.layout.row_lesson, parent, false);
				}
				
				String title = titles[position];
				String url = urls[position];

				TextView entryName = (TextView) convertView.findViewById(R.id.lesson_name);
				entryName.setText(title);

				TextView entrySection = (TextView) convertView.findViewById(R.id.lesson_category);
				entrySection.setText(title);
				
				if (url.length() > 0)
				{
					entrySection.setVisibility(View.GONE);
					entryName.setVisibility(View.VISIBLE);
				}
				else
				{
					entrySection.setVisibility(View.VISIBLE);
					entryName.setVisibility(View.GONE);
				}
				
				return convertView;
			}
		};
		
		entriesList.setAdapter(adapter);
		
		entriesList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				String url = urls[position];
				
				me.openUri(Uri.parse(url), titles[position]);
			}
		});
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_content_list_activity", payload);
	}
	
	protected void openUri(Uri uri, String title) 
	{
		Intent intent = new Intent(this, LessonActivity.class);
		intent.putExtra(LessonActivity.TITLE, title);
		intent.putExtra(LessonActivity.URL, uri.toString());
		
		this.startActivity(intent);
	}

	protected abstract int titlesArrayId();
	protected abstract int urlsArrayId();

	public void onPause()
	{
		/*
		ListView entriesList = (ListView) this.findViewById(R.id.list_entries);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) entriesList.getAdapter();

		adapter.swapCursor(null).close();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("close_content_list_activity", payload);
		*/

		super.onPause();
	}
	
/*	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = this.getMenuInflater();
	    inflater.inflate(R.menu.menu_content, menu);
	    
	    this._menu  = menu;

	    SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    
	    final ContentIndexActivity me = this;
	    
	    searchView.setOnQueryTextListener(new OnQueryTextListener()
	    {
			public boolean onQueryTextChange(String query) 
			{
				return this.onQueryTextSubmit(query);
			}

			public boolean onQueryTextSubmit(String query) 
			{
	    		ListView entriesList = (ListView) me.findViewById(R.id.list_entries);
	    		SimpleCursorAdapter adapter = (SimpleCursorAdapter) entriesList.getAdapter();

	    		if (query.trim().length() > 0)
	    		{
		    		String where = "content_entries MATCH ?";
		    		String[] args = { query + "*" };
	
		    		Cursor c = me.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, where, args, null);
	
		    		adapter.swapCursor(c).close();
	    		}
	    		else
	    		{
					Cursor c = me.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, null, null, EntriesContentProvider.TITLE);
		    		adapter.swapCursor(c).close();
	    		}

	    		adapter.notifyDataSetChanged();
	            
	    		HashMap<String, Object> payload = new HashMap<String, Object>();
	    		payload.put("query", query);
	    		
	    		LogManager.getInstance(me).log("content_search", payload);

				return true;
			}
	    });
	    
	    searchView.setOnCloseListener(new OnCloseListener()
	    {
			public boolean onClose() 
			{
	    		ListView entriesList = (ListView) me.findViewById(R.id.list_entries);

				Cursor c = me.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, null, null, EntriesContentProvider.TITLE);

	    		SimpleCursorAdapter adapter = (SimpleCursorAdapter) entriesList.getAdapter();

	    		adapter.swapCursor(c).close();
	    		
	    		adapter.notifyDataSetChanged();

				return false;
			}
	    });

	    searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));

	    return true;
	} */
}
