package edu.northwestern.cbits.intellicare.aspire;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class CardActivity extends ConsentedActivity 
{
	private int _count = 0;
	private Menu _menu = null;
	private boolean _cardsVisible = false;
	private int _index = -1;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_card);
		
		this.getSupportActionBar().setTitle(R.string.title_card);		
	}
	
	protected void onResume()
	{
		super.onResume();
		
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
		this._count = c.getCount();
		
		if (this._index == -1)
		{
			SecureRandom random = new SecureRandom();

			this._index = random.nextInt(this._count);
		}
		
		final CardActivity me = this;
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return me._count;
			}

			public boolean isViewFromObject(View view, Object content) 
			{
				return view.getTag().equals(content);
			}

			public void destroyItem (ViewGroup container, int position, Object content)
			{
				int toRemove = -1;

				for (int i = 0; i < container.getChildCount(); i++)
				{
					View child = container.getChildAt(i);

					if (this.isViewFromObject(child, content))
						toRemove = i;
				}

				if (toRemove >= 0)
					container.removeViewAt(toRemove);
			}

			public Object instantiateItem (ViewGroup container, int position)
			{
				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_card, null, false);
				
				View cardView = view.findViewById(R.id.view_card);

				cardView.setOnClickListener(new View.OnClickListener() 
				{
					public void onClick(View arg0) 
					{
						Cursor c = me.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
						
						if (c.moveToPosition(pager.getCurrentItem()))
						{
							long id = c.getLong(c.getColumnIndex(AspireContentProvider.ID));
							String name = c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME));
							String description = c.getString(c.getColumnIndex(AspireContentProvider.CARD_DESCRIPTION));
							
							HashMap<String, Object> payload = new HashMap<String, Object>();
							payload.put("name", c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
							LogManager.getInstance(me).log("selected_card", payload);

							me.useCard(id, name, description);
						}
						
						c.close();
					}
				});

				TextView name = (TextView) view.findViewById(R.id.card_name);
				TextView description = (TextView) view.findViewById(R.id.card_description);
			
				Cursor c = me.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
				
				if (c.moveToPosition(position))
				{
					name.setText(c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
					description.setText(c.getString(c.getColumnIndex(AspireContentProvider.CARD_DESCRIPTION)));

					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("name", name.getText().toString());
					payload.put("description", description.getText().toString());
					LogManager.getInstance(me).log("showed_card", payload);
				}
				
				c.close();

				view.setTag("" + position);

				container.addView(view);

				LayoutParams layout = (LayoutParams) view.getLayoutParams();
				layout.height = LayoutParams.MATCH_PARENT;
				layout.width = LayoutParams.MATCH_PARENT;

				view.setLayoutParams(layout);

				return view.getTag();
			}
		};

		pager.setAdapter(adapter);

		pager.setCurrentItem(this._index);

		ListView list = (ListView) this.findViewById(R.id.list_content);
		
		String[] from = { AspireContentProvider.CARD_NAME, AspireContentProvider.CARD_DESCRIPTION };
		int[] to = { android.R.id.text1, android.R.id.text2 };
		
		SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, from, to, 0);
		
		list.setAdapter(listAdapter);
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Cursor c = me.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
				
				if (c.moveToPosition(position))
				{
					id = c.getLong(c.getColumnIndex(AspireContentProvider.ID));
					String name = c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME));
					String description = c.getString(c.getColumnIndex(AspireContentProvider.CARD_DESCRIPTION));
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("name", c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
					LogManager.getInstance(me).log("selected_card", payload);

					me.useCard(id, name, description);
				}
				
				c.close();
			}
		});
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_cards", payload);
	}
	
	protected void onPause()
	{
		super.onPause();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_cards", payload);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_card, menu);

		this._menu = menu;
		
		this.toggleView();

		return true;
	}

	private void toggleView() 
	{
		this._cardsVisible = (this._cardsVisible == false);

		MenuItem listItem = this._menu.findItem(R.id.action_list_view);
		MenuItem cardsItem = this._menu.findItem(R.id.action_cards_view);

		ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
		ListView list = (ListView) this.findViewById(R.id.list_content);
		
		if (this._cardsVisible)
		{
			listItem.setVisible(true);
			cardsItem.setVisible(false);
			
			pager.setVisibility(View.VISIBLE);
			list.setVisibility(View.GONE);
		}
		else
		{
			listItem.setVisible(false);
			cardsItem.setVisible(true);

			pager.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
		}
	}
	
	public void useCard(final long id, final String title, final String description)
	{
        final CardActivity me = this;

		if (id != -1)
		{
			String where = AspireContentProvider.PATH_CARD_ID + " = ?";
			String[] whereArgs = { "" + id };
			
			Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, where, whereArgs, AspireContentProvider.ID);
			
			if (c.getCount() == 0)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_first_path);
				
				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_add_path, null, false);
				
				builder.setView(view);
				
				final EditText pathField = (EditText) view.findViewById(R.id.field_new_path);

                final CardActivity me = this;

                ArrayList<Tool> paths = new ArrayList<Tool>();

              //  paths.add(new Path(this.getString(R.string.tool_use_log), this.getString(R.string.desc_tool_chooser_log), new Intent(this, ToolTrackerActivity.class)));

                ListView toolsList = (ListView) this.findViewById(R.id.path_spot);

                final ArrayAdapter<Tool> adapter = new ArrayAdapter<Tool>(this, R.layout.row_task, paths)

                {
                    public View getView (int position, View convertView, ViewGroup parent)
                    {
                        if (convertView == null)
                        {
                            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                            convertView = inflater.inflate(R.layout.row_task, parent, false);
                        }

                        TextView pathName = (TextView) convertView.findViewById(R.id.path_name);

                        // below we query the path bank instead
                        Tool p = this.getItem(position);

                        pathName.setText(p.pathText);

                        return convertView;
                    }
                };

                toolsList.setAdapter(adapter);

                toolsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                        Tool t = adapter.getItem(which);

                        // on selection add path to card
                        public void onSelected(//checkbox, int which)
                        {
                            ContentValues preValues = new ContentValues();
                            preValues.put(AspireContentProvider.PATH_CARD_ID, cardId);
                            preValues.put(AspireContentProvider.PATH_PATH, pathField.getText().toString().trim());

                            me.getContentResolver().insert(AspireContentProvider.ASPIRE_PATH_URI, preValues);

                            HashMap<String, Object> payload = new HashMap<String, Object>();
                            payload.put("path", pathField.getText().toString().trim());
                            LogManager.getInstance(me).log("added_path", payload);

                            me.finish();

                        });
                    }
                });
				
				final long cardId = id;
				
				builder.setPositiveButton(R.string.action_add_path, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						ContentValues values = new ContentValues();
						values.put(AspireContentProvider.PATH_CARD_ID, cardId);
						values.put(AspireContentProvider.PATH_PATH, pathField.getText().toString().trim());
						
						me.getContentResolver().insert(AspireContentProvider.ASPIRE_PATH_URI, values);
						
						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("path", pathField.getText().toString().trim());
						LogManager.getInstance(me).log("added_path", payload);

						me.finish();
					}
				});
				
				builder.create().show();
			}
			else
				this.finish();
			
			c.close();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		final CardActivity me = this;
		
		switch (itemId)
		{
			case R.id.action_list_view:
				this.toggleView();
				break;
			case R.id.action_cards_view:
				this.toggleView();
				break;
			case R.id.action_add_card:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_add_card);

				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.view_add_card, null, false);
				
				builder.setView(view);
				
				final EditText name = (EditText) view.findViewById(R.id.field_card_name);
				final EditText description = (EditText) view.findViewById(R.id.field_card_description);
				
				builder.setPositiveButton(R.string.action_add_card, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						ContentValues values = new ContentValues();
						values.put(AspireContentProvider.CARD_NAME, name.getText().toString());
						values.put(AspireContentProvider.CARD_DESCRIPTION, description.getText().toString());
						
						me.getContentResolver().insert(AspireContentProvider.ASPIRE_CARD_URI, values);
						
						Cursor c = me.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
						
						if (c.moveToLast())
							me.useCard(c.getLong(c.getColumnIndex(AspireContentProvider.ID)), values.getAsString(AspireContentProvider.CARD_NAME), values.getAsString(AspireContentProvider.CARD_DESCRIPTION));
						
						c.close();
					}
				});

				builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{

					}
				});
				
				builder.create().show();

				break;
		}
		
		return true;
	}
}
