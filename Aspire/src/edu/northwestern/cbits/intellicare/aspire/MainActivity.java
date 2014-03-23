package edu.northwestern.cbits.intellicare.aspire;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
	private int _index = -1;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		ImageView background = (ImageView) this.findViewById(R.id.image_background);

		try 
		{
		    InputStream ims = getAssets().open("placeholder.jpg");

            Bitmap bitmap = BitmapFactory.decodeStream(ims);

            background.setImageBitmap(bitmap);
            
            ims.close();
		}
		catch(IOException e) 
		{
			e.printStackTrace();
			
			LogManager.getInstance(this).logException(e);
		}
		
		final MainActivity me = this;
		
		ImageView previous = (ImageView) this.findViewById(R.id.button_previous);
		
		previous.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				me._index  -= 1;
				
				me.showCard(me._index);
			}
		});

		ImageView next = (ImageView) this.findViewById(R.id.button_next);
		
		next.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				me._index  += 1;
				
				me.showCard(me._index);
			}
		});

		ImageView path = (ImageView) this.findViewById(R.id.button_path);
		
		path.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Intent intent = new Intent(me, EditActivity.class);
				me.startActivity(intent);
			}
		});
		
		ImageView photo = (ImageView) this.findViewById(R.id.button_photo);
		
		photo.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Toast.makeText(me, "tOdo: chaNge PHOtO", Toast.LENGTH_LONG).show();
			}
		});
		
		this.showCard(0);
	}
	
	private void showCard(int index) 
	{
		final MainActivity me = this;
		
		HashMap<Long, Integer> cardCount = new HashMap<Long, Integer>();
		ArrayList<Long> cardIds = new ArrayList<Long>();
	
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, null, null, AspireContentProvider.PATH_CARD_ID);
		
		while (c.moveToNext())
		{
			Long cardId = Long.valueOf(c.getLong(c.getColumnIndex(AspireContentProvider.PATH_CARD_ID)));
			
			if (cardIds.contains(cardId) == false)
				cardIds.add(cardId);
			
			int count = 0;
			
			if (cardCount.containsKey(cardId))
				count = cardCount.get(cardId).intValue();
			
			count += 1;
			
			cardCount.put(cardId, Integer.valueOf(count));
		}
		
		c.close();

		if (cardIds.size() > 0)
		{
			if (index < 0)
				this._index = cardIds.size() - 1;
			else if (index >= cardIds.size())
				this._index = 0;
			else
				this._index = index;

			final long cardId = cardIds.get(this._index).longValue();
	
			int count = cardCount.get(Long.valueOf(cardId)).intValue();
	
			final TextView name = (TextView) this.findViewById(R.id.card_name);
			TextView description = (TextView) this.findViewById(R.id.card_description);
			
			String where = AspireContentProvider.ID + " = ?";
			String[] args = { "" + cardId };
			
			c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, where, args, null);
			
			if (c.moveToNext())
				name.setText(c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
			
			c.close();
			
			if (count == 1)
				description.setText(R.string.desc_single_path);
			else
				description.setText(this.getString(R.string.desc_paths, count));
			
			LinearLayout userInfo = (LinearLayout) this.findViewById(R.id.layout_card_info);
			
			userInfo.setOnClickListener(new OnClickListener()
			{
				public void onClick(View arg0) 
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					
					builder.setTitle(name.getText().toString());
					
					String where = AspireContentProvider.PATH_CARD_ID + " = ?";
					String[] args = { "" + cardId };
					
					Cursor c = me.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, where, args, AspireContentProvider.PATH_PATH);
					
					ArrayList<String> paths = new ArrayList<String>();
					final ArrayList<Long> pathIds = new ArrayList<Long>();
					
					while (c.moveToNext())
					{
						paths.add(c.getString(c.getColumnIndex(AspireContentProvider.PATH_PATH)));
						pathIds.add(c.getLong(c.getColumnIndex(AspireContentProvider.ID)));
					}
					
					String[] pathArray = new String[paths.size()];
					boolean[] selected = new boolean[paths.size()];

					Calendar cal = Calendar.getInstance();

					for (int i = 0; i < pathArray.length; i++)
					{
						pathArray[i] = paths.get(i);
						
						String select = AspireContentProvider.TASK_PATH_ID + " = ?";
						select += " AND " + AspireContentProvider.TASK_YEAR + " =?";
						select += " AND " + AspireContentProvider.TASK_MONTH + " =?";
						select += " AND " + AspireContentProvider.TASK_DAY + " =?";
						
						String[] selectArgs = { "" + pathIds.get(i), "" + cal.get(Calendar.YEAR), "" + cal.get(Calendar.MONTH), "" + cal.get(Calendar.DAY_OF_MONTH) };
						
						Cursor cursor = me.getContentResolver().query(AspireContentProvider.ASPIRE_TASK_URI, null, select, selectArgs, null);
						
						selected[i] = (cursor.getCount() > 0);
						
						cursor.close();
					}
					
					builder.setMultiChoiceItems(pathArray, selected, new OnMultiChoiceClickListener()
					{
						public void onClick(DialogInterface arg0, int which, boolean checked) 
						{
							final Calendar cal = Calendar.getInstance();

							String where = AspireContentProvider.TASK_PATH_ID + " = ?";
							where += " AND " + AspireContentProvider.TASK_YEAR + " =?";
							where += " AND " + AspireContentProvider.TASK_MONTH + " =?";
							where += " AND " + AspireContentProvider.TASK_DAY + " =?";
							
							String[] args = { "" + pathIds.get(which), "" + cal.get(Calendar.YEAR), "" + cal.get(Calendar.MONTH), "" + cal.get(Calendar.DAY_OF_MONTH) };
							
							me.getContentResolver().delete(AspireContentProvider.ASPIRE_TASK_URI, where, args);

							if (checked)
							{
								ContentValues values = new ContentValues();
								values.put(AspireContentProvider.TASK_PATH_ID, pathIds.get(which));
								values.put(AspireContentProvider.TASK_YEAR, cal.get(Calendar.YEAR));
								values.put(AspireContentProvider.TASK_MONTH, cal.get(Calendar.MONTH));
								values.put(AspireContentProvider.TASK_DAY, cal.get(Calendar.DAY_OF_MONTH));
								
								me.getContentResolver().insert(AspireContentProvider.ASPIRE_TASK_URI, values);
							}
						}
					});
					
					c.close();
					
					builder.setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{

						}
					});
					
					builder.create().show();
				}
			});
		}
	}

	protected void onResume()
	{
		super.onResume();
		
		final MainActivity me = this;
		
		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, null, null, AspireContentProvider.ID);

		if (c.getCount() == 0)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle(R.string.title_path_needed);
			
			builder.setMessage(R.string.message_path_needed);
			
			builder.setPositiveButton(R.string.action_add_path, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					Intent cardIntent = new Intent(me, CardActivity.class);
					
					me.startActivity(cardIntent);
				}
			});
			
			builder.create().show();
		}
		else
		{
			HashSet<Long> cardIds = new HashSet<Long>();
			
			while (c.moveToNext())
			{
				cardIds.add(Long.valueOf(c.getLong(c.getColumnIndex(AspireContentProvider.PATH_CARD_ID))));
			}
			
			if (cardIds.size() == 1)
				this.getSupportActionBar().setSubtitle(R.string.subtitle_single_value);
			else
				this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_values, cardIds.size()));
				
		}
		
		c.close();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				this.startActivity(settingsIntent);
				
				break;
			case R.id.action_feedback:
				this.sendFeedback(this.getString(R.string.app_name));
					
				break;
			case R.id.action_add_value:
				Intent cardIntent = new Intent(this, CardActivity.class);
				this.startActivity(cardIntent);
					
				break;
		}
		
		return true;
	}
}
