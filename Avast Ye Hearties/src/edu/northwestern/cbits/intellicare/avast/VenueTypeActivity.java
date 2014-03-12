package edu.northwestern.cbits.intellicare.avast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.avast.AvastContentProvider.Category;

public class VenueTypeActivity extends ConsentedActivity 
{
	private final HashSet<String> _selectedIds = new HashSet<String>();
	private List<Category> _categories = new ArrayList<Category>();
	
	private String _searchFilter = null;
	
	private boolean _showedDialog = false;

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_venue);
        
        final VenueTypeActivity me = this;
        
		AvastContentProvider.fetchCategories(this, new Runnable()
		{
			public void run() 
			{
				me._categories.clear();
				me._categories.addAll(AvastContentProvider.getLastCategories());
				
				me.filterList();
			}
		});
    }
	
	protected void onResume()
	{
		super.onResume();
		
		if (this._showedDialog == false)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_venue_type_picker);
			builder.setMessage(R.string.message_venue_type_picker);
			
			builder.setPositiveButton(R.string.action_continue, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder.create().show();
			
			this._showedDialog = true;
		}
	}
    
	private void filterList()
	{
        final VenueTypeActivity me = this;

        this.refreshSelectedCategories();
        
        final ListView categories = (ListView) this.findViewById(R.id.categories_list);
        
        ArrayList<Category> matches = new ArrayList<Category>();
        
        for (Category category : this._categories)
        {
        	if (this._searchFilter == null || category.name.toLowerCase().contains(this._searchFilter))
        		matches.add(category);
        }

		ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, R.layout.row_checkbox, matches)
		{
			public View getView (int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(me);
					convertView = inflater.inflate(R.layout.row_checkbox, null, false);
				}
				
				convertView = super.getView(position, convertView, parent);
				
				final Category category = this.getItem(position);
				
				CheckBox check = (CheckBox) convertView.findViewById(R.id.checkbox);

				check.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton arg0, boolean checked) 
					{

					}
				});

				check.setChecked(me._selectedIds.contains(category.id));
				
				check.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton button, boolean checked) 
					{
						String where = AvastContentProvider.VENUE_TYPE_FOURSQUARE_ID + " = ?";
						String[] whereArgs = { category.id };
						
						ContentValues values = new ContentValues();
						values.put(AvastContentProvider.VENUE_TYPE_ENABLED, false);
								
						me.getContentResolver().update(AvastContentProvider.VENUE_TYPE_URI, values, where, whereArgs);
						
						if (checked)
						{
							values.put(AvastContentProvider.VENUE_TYPE_ENABLED, true);
									
							int updateCount = me.getContentResolver().update(AvastContentProvider.VENUE_TYPE_URI, values, where, whereArgs);
							
							if (updateCount == 0)
							{
								values.put(AvastContentProvider.VENUE_TYPE_NAME, category.name);
								values.put(AvastContentProvider.VENUE_TYPE_FOURSQUARE_ID, category.id);
								
								if (category.parentId != null)
									values.put(AvastContentProvider.VENUE_TYPE_FOURSQUARE_PARENT_ID, category.parentId);
								
								me.getContentResolver().insert(AvastContentProvider.VENUE_TYPE_URI, values);
							}
						}
						
						me.refreshSelectedCategories();
					}
				});
				
				check.setText(category.name);
				
				return convertView;
			}
		};
		
		categories.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		categories.invalidate();
		
	}
	
	protected void refreshSelectedCategories() 
	{
		String enabledWhere = AvastContentProvider.VENUE_TYPE_ENABLED + " = ?";
		String[] enabledWhereArgs = { "1" };
		
		Cursor c = this.getContentResolver().query(AvastContentProvider.VENUE_TYPE_URI, null, enabledWhere, enabledWhereArgs, null);
		
		this._selectedIds.clear();
		
		while (c.moveToNext())
		{
			this._selectedIds.add(c.getString(c.getColumnIndex(AvastContentProvider.VENUE_TYPE_FOURSQUARE_ID)));
		}
		
		c.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_venue_type, menu);
		
		final MenuItem searchItem = menu.findItem(R.id.menu_search);

	    final SearchView searchView = (SearchView) searchItem.getActionView();
	    searchView.setSubmitButtonEnabled(false);
	    
	    final VenueTypeActivity me = this;
	    
	    searchView.setOnQueryTextListener(new OnQueryTextListener()
	    {
			public boolean onQueryTextChange(String query) 
			{
				return this.onQueryTextSubmit(query);
			}

			public boolean onQueryTextSubmit(String query) 
			{
				if (query == null || query.length() == 0)
					searchView.setIconified(true);
				else
				{
					me._searchFilter = query.toLowerCase();
				
					me.filterList();
				}
				
				return true;
			}
	    });
	    
	    searchView.setOnCloseListener(new OnCloseListener()
	    {
			public boolean onClose() 
			{
				me._searchFilter = null;
				me.filterList();

				return false;
			}
	    });
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_next:
				Toast.makeText(this, "TODO: cOnfirm THAT wE shOULd ContinUE...", Toast.LENGTH_LONG).show();
				
				Intent locationIntent = new Intent(this, LocationChooserActivity.class);
				this.startActivity(locationIntent);
				
				break;
		}
		
		return true;
	}

}
