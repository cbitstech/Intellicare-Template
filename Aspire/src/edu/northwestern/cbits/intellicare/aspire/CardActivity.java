package edu.northwestern.cbits.intellicare.aspire;

import java.security.SecureRandom;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class CardActivity extends ConsentedActivity 
{
	private int _count = 0;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_card);
		
		this.getSupportActionBar().setTitle(R.string.title_card);		
		
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
		this._count = c.getCount();
		c.close();
		
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

		SecureRandom random = new SecureRandom();
		pager.setCurrentItem(random.nextInt(this._count));
	}
	
	protected void onResume()
	{
		super.onResume();
		
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
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		final CardActivity me = this;
		
		switch (itemId)
		{
			case R.id.action_use_card:
		        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

				long id = -1;
				
				Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
				
				if (c.moveToPosition(pager.getCurrentItem()))
				{
					id = c.getLong(c.getColumnIndex(AspireContentProvider.ID));
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("name", c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
					LogManager.getInstance(me).log("selected_card", payload);
				}
				
				c.close();
				
				if (id != -1)
				{
					String where = AspireContentProvider.PATH_CARD_ID + " = ?";
					String[] whereArgs = { "" + id };
					
					c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, where, whereArgs, AspireContentProvider.ID);
					
					if (c.getCount() == 0)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						
						builder.setTitle(R.string.title_first_path);
						
	    				LayoutInflater inflater = LayoutInflater.from(me);
	    				View view = inflater.inflate(R.layout.view_add_path, null, false);
	    				
	    				builder.setView(view);
	    				builder.setCancelable(false);
	    				
	    				final EditText pathField = (EditText) view.findViewById(R.id.field_new_path);
	    				
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
				else
					this.finish();

				break;
		}
		
		return true;
	}
}
