package edu.northwestern.cbits.intellicare.aspire;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
	protected static final int RESULT_FETCH_IMAGE = 123;

	private int _index = -1;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		final MainActivity me = this;
		
		ImageView previous = (ImageView) this.findViewById(R.id.button_previous);
		
		previous.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("previous_card", payload);

				me._index  -= 1;
				
				me.showCard(me._index);
			}
		});

		ImageView next = (ImageView) this.findViewById(R.id.button_next);
		
		next.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("next_card", payload);

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
				intent.putExtra(EditActivity.CARD_ID, me.currentCardId());
				
				me.startActivity(intent);
			}
		});
		
		ImageView photo = (ImageView) this.findViewById(R.id.button_photo);
		
		photo.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("fetching_photo", payload);

				Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				me.startActivityForResult(in, MainActivity.RESULT_FETCH_IMAGE);
			}
		});
		
		this.showCard(0);
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if (requestCode == MainActivity.RESULT_FETCH_IMAGE) 
        {
            if (resultCode == RESULT_OK) 
            {
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(this).log("fetched_photo", payload);

				String where = AspireContentProvider.ID + " = ?";
            	String[] args = { "" + this.currentCardId() };
            	
            	ContentValues values = new ContentValues();
            	values.put(AspireContentProvider.CARD_IMAGE, data.getDataString());
            	
            	this.getContentResolver().update(AspireContentProvider.ASPIRE_CARD_URI, values, where, args);

            	this.showCard(this._index);
            }
        }
    }
	
	protected long currentCardId() 
	{
		ArrayList<Long> cardIds = new ArrayList<Long>();

		Cursor c = this.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, null, null, AspireContentProvider.PATH_CARD_ID);
		
		while (c.moveToNext())
		{
			Long cardId = Long.valueOf(c.getLong(c.getColumnIndex(AspireContentProvider.PATH_CARD_ID)));
			
			if (cardIds.contains(cardId) == false)
				cardIds.add(cardId);
		}
		
		c.close();
		
		return cardIds.get(this._index);
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
			
			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("virtue", name.getText().toString());
			LogManager.getInstance(this).log("showed_virtue", payload);

			String where = AspireContentProvider.ID + " = ?";
			String[] args = { "" + cardId };
			
			c = this.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, where, args, null);
			
			if (c.moveToNext())
			{
				name.setText(c.getString(c.getColumnIndex(AspireContentProvider.CARD_NAME)));
			
				ImageView background = (ImageView) this.findViewById(R.id.image_background);
				
				String imageUri = c.getString(c.getColumnIndex(AspireContentProvider.CARD_IMAGE));
	
				if (imageUri != null && imageUri.trim().length() > 0)
					background.setImageURI(Uri.parse(imageUri));
				else
				{
					try 
					{
					    InputStream ims = this.getAssets().open("placeholder.jpg");
		
			            Bitmap bitmap = BitmapFactory.decodeStream(ims);
		
			            background.setImageBitmap(bitmap);
			            
			            ims.close();
					}
					catch(IOException e) 
					{
						e.printStackTrace();
						
						LogManager.getInstance(this).logException(e);
					}
				}
			}

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
					
					final String[] pathArray = new String[paths.size()];
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

							HashMap<String, Object> payload = new HashMap<String, Object>();
							payload.put("virtue", name.getText().toString());
							payload.put("path", pathArray[which]);

							if (checked)
							{
								ContentValues values = new ContentValues();
								values.put(AspireContentProvider.TASK_PATH_ID, pathIds.get(which));
								values.put(AspireContentProvider.TASK_YEAR, cal.get(Calendar.YEAR));
								values.put(AspireContentProvider.TASK_MONTH, cal.get(Calendar.MONTH));
								values.put(AspireContentProvider.TASK_DAY, cal.get(Calendar.DAY_OF_MONTH));
								
								me.getContentResolver().insert(AspireContentProvider.ASPIRE_TASK_URI, values);

								LogManager.getInstance(me).log("selected_path", payload);
							}
							else
								LogManager.getInstance(me).log("deselected_path", payload);
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
		
		CrashManager.register(this, "6eacc0dcfbc01f632146cae8b602f5c5", new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});
		
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

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_main", payload);

		this.updateWeek();
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_main", payload);
	}
	
	private void updateWeek() 
	{
		final MainActivity me = this;
		
		Calendar cal = Calendar.getInstance();

		int[] dayIds = { R.id.day_zero, R.id.day_one, R.id.day_two, 
				         R.id.day_three, R.id.day_four, R.id.day_five,
				         R.id.day_six };

		int[] weekdays = { R.string.day_sun, R.string.day_mon, R.string.day_tue, 
				           R.string.day_wed, R.string.day_thu, R.string.day_fri,
				           R.string.day_sat };
		
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		
		long now = System.currentTimeMillis();
		
		for (int i = dayIds.length; i > 0; i--)
		{
			int dayId = dayIds[i - 1];
			
			LinearLayout layout = (LinearLayout) this.findViewById(dayId);
			
			TextView dayLabel = (TextView) layout.findViewById(R.id.label_day);
			
			if (i == dayIds.length)
				dayLabel.setTypeface(null, Typeface.BOLD);
			else
				dayLabel.setTypeface(null, Typeface.NORMAL);
			
			int offset = i + dayOfWeek;
			
			dayLabel.setText(weekdays[offset % dayIds.length]);
			
			final long timestamp = now - (1000 * 60 * 60 * 24 * (dayIds.length - i));
			
			Calendar thisDay = Calendar.getInstance();
			thisDay.setTimeInMillis(timestamp);
			
			final int year = thisDay.get(Calendar.YEAR);
			final int month = thisDay.get(Calendar.MONTH);
			final int day = thisDay.get(Calendar.DAY_OF_MONTH);
			
			String select = AspireContentProvider.TASK_YEAR + " =?";
			select += " AND " + AspireContentProvider.TASK_MONTH + " =?";
			select += " AND " + AspireContentProvider.TASK_DAY + " =?";
			
			String[] selectArgs = { "" + year, "" + month, "" + day };

			ImageView dayIcon = (ImageView) layout.findViewById(R.id.image_day);
			
			Cursor cursor = this.getContentResolver().query(AspireContentProvider.ASPIRE_TASK_URI, null, select, selectArgs, null);

			if (cursor.getCount() > 0)
				dayIcon.setImageResource(R.drawable.ic_action_emo_basic);
			else
				dayIcon.setImageResource(R.drawable.ic_action_emo_err);
			
			cursor.close();
			
			layout.setOnClickListener(new OnClickListener()
			{
				@SuppressWarnings("deprecation")
				public void onClick(View arg0) 
				{
					String select = AspireContentProvider.TASK_YEAR + " =?";
					select += " AND " + AspireContentProvider.TASK_MONTH + " =?";
					select += " AND " + AspireContentProvider.TASK_DAY + " =?";
					
					String[] selectArgs = { "" + year, "" + month, "" + day };

					Cursor cursor = me.getContentResolver().query(AspireContentProvider.ASPIRE_TASK_URI, null, select, selectArgs, null);

					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					
					java.text.DateFormat formatter = DateFormat.getLongDateFormat(me);
					builder.setTitle(formatter.format(new Date(timestamp)));

					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("year", "" + year);
					payload.put("month", "" + month);
					payload.put("year", "" + day);
					LogManager.getInstance(me).log("tapped_date", payload);

					if (cursor.getCount() > 0)
					{
	    				LayoutInflater inflater = LayoutInflater.from(me);
	    				View view = inflater.inflate(R.layout.view_paths, null, false);
	    				
	    				ListView list = (ListView) view.findViewById(R.id.list_view);
	
	    				SimpleCursorAdapter adapter = new SimpleCursorAdapter(me, R.layout.row_task, cursor, new String[0], new int[0])
	    				{
	    					public void bindView (View view, Context context, Cursor cursor)
	    					{
	    						TextView path = (TextView) view.findViewById(R.id.path_name);
	    						TextView card = (TextView) view.findViewById(R.id.card_name);
	    						
	    						long pathId = cursor.getLong(cursor.getColumnIndex(AspireContentProvider.TASK_PATH_ID));
	    						
	    						String where = AspireContentProvider.ID + " = ?";
	    						String[] args = { "" + pathId };
	    						
	    						Cursor pathCursor = me.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, where, args, AspireContentProvider.ID);
	    						
	    						if (pathCursor.moveToNext())
	    						{
	    							path.setText(pathCursor.getString(pathCursor.getColumnIndex(AspireContentProvider.PATH_PATH)));
	    							
	    							args[0] = pathCursor.getString(pathCursor.getColumnIndex(AspireContentProvider.PATH_CARD_ID));
	    							
	    							Cursor cardCursor = me.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, null, null, null);
	    							
	    							if (cardCursor.moveToNext())
	    								card.setText(cardCursor.getString(cardCursor.getColumnIndex(AspireContentProvider.CARD_NAME)));
	    							
	    							cardCursor.moveToNext();
	    						}
	    						
	    						pathCursor.close();
	    					}
	    				};
	    				
	    				list.setAdapter(adapter);
	    				
						builder.setView(view);
					}
					else
						builder.setMessage(R.string.message_no_paths);
					
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
