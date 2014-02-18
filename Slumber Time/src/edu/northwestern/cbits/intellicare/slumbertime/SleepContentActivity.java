package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SleepContentActivity extends ConsentedActivity
{
	private Menu _menu = null;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_content_entries);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_content);
	}
	
	public void onResume()
	{
		super.onResume();

		final SleepContentActivity me = this;
		
		ListView entriesList = (ListView) this.findViewById(R.id.list_entries);
		
		Cursor c = this.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, null, null, EntriesContentProvider.TITLE);

		// this.startManagingCursor(c);
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
		
		if (this._menu != null)
		{
		    SearchView searchView = (SearchView) this._menu.findItem(R.id.menu_search).getActionView();
		    
		    searchView.setQuery(searchView.getQuery(), true);
		}
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_content_list_activity", payload);
	}
	
	public void onPause()
	{
		ListView entriesList = (ListView) this.findViewById(R.id.list_entries);
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) entriesList.getAdapter();

		adapter.swapCursor(null).close();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("close_content_list_activity", payload);

		super.onPause();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = this.getMenuInflater();
	    inflater.inflate(R.menu.menu_content, menu);
	    
	    this._menu  = menu;

	    SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    
	    final SleepContentActivity me = this;
	    
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
	}
}
