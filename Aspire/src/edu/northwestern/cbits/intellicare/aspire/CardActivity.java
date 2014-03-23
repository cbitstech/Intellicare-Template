package edu.northwestern.cbits.intellicare.aspire;

import java.security.SecureRandom;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class CardActivity extends ConsentedActivity 
{
	private int _index = 0;
	private int _count = 0;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_card);
		
		this.getSupportActionBar().setTitle(R.string.title_card);
		
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
		
		SecureRandom random = new SecureRandom();
		
		this._count = c.getCount();
		this._index = random.nextInt(this._count);
		
		c.close();
		
		this.showCard(this._index);
		
		final CardActivity me = this;
		
		ImageView previous = (ImageView) this.findViewById(R.id.button_previous);
		
		previous.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				me._index -= 1;
				
				me.showCard(me._index);
			}
		});

		ImageView next = (ImageView) this.findViewById(R.id.button_next);
		
		next.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				me._index += 1;
				
				me.showCard(me._index);
			}
		});
	}
	
	private void showCard(int index) 
	{
		if (index < 0)
			this._index = this._count - 1;
		else if (index >= this._count)
			this._index = 0;
		else
			this._index = index;
		
		TextView name = (TextView) this.findViewById(R.id.card_name);
		TextView description = (TextView) this.findViewById(R.id.card_description);
	
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
		
		if (c.moveToPosition(this._index))
		{
			name.setText(c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
			description.setText(c.getString(c.getColumnIndex(AspireContentProvider.CARD_DESCRIPTION)));
		}
		
		c.close();
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
				long id = -1;
				
				Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, AspireContentProvider.ID);
				
				if (c.moveToPosition(this._index))
				{
					id = c.getLong(c.getColumnIndex(AspireContentProvider.ID));
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
