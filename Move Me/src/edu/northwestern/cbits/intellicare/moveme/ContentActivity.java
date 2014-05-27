package edu.northwestern.cbits.intellicare.moveme;

import android.os.Bundle;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ContentActivity extends ConsentedActivity
{
//	private Menu _menu = null;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_content_entries);
	}
	
/*	public void onResume()
	{
		super.onResume();

		final ContentActivity me = this;
		
		ListView entriesList = (ListView) this.findViewById(R.id.list_entries);
		
		Cursor c = this.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, null, null, EntriesContentProvider.WEIGHT);

		// this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};
		
		final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_content_entry, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				TextView entrySection = (TextView) view.findViewById(R.id.entry_section);
				entrySection.setText(cursor.getString(cursor.getColumnIndex(EntriesContentProvider.SECTION)));

				TextView entryName = (TextView) view.findViewById(R.id.entry_name);
				entryName.setText(cursor.getString(cursor.getColumnIndex(EntriesContentProvider.TITLE)));
				
				entrySection.setVisibility(View.GONE);
				
				if (cursor.moveToPrevious() == false)
					entrySection.setVisibility(View.VISIBLE);
				else
				{
					String category = cursor.getString(cursor.getColumnIndex(EntriesContentProvider.SECTION));
					
					if (category.equals(entrySection.getText().toString()) == false)
						entrySection.setVisibility(View.VISIBLE);
					
					cursor.moveToNext();
				}
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
	    
	    final ContentActivity me = this;
	    
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
