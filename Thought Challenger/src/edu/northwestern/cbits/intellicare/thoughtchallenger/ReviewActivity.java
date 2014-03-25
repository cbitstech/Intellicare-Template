package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.security.SecureRandom;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ReviewActivity extends ConsentedActivity 
{
	private int _index = 0;
	private int _distortionIndex = -1;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_review);

		this.getSupportActionBar().setTitle(R.string.title_review);
		
		final ReviewActivity me = this;
		final String[] distortions = this.getResources().getStringArray(R.array.list_distortions);
		
		final Button tags = (Button) this.findViewById(R.id.action_edit_tags);
		
		tags.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				final String[] tagList = ThoughtContentProvider.fetchTags(me);
				
				if (tagList.length == 0)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					
					builder.setTitle(R.string.action_add_tag);

    				LayoutInflater inflater = LayoutInflater.from(me);
    				View view = inflater.inflate(R.layout.view_add_tag, null, false);
    				
    				final EditText tagField = (EditText) view.findViewById(R.id.field_new_tag);

    				builder.setView(view);

					builder.setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
							Editor e = prefs.edit();
							
							try 
							{
								String newTag = tagField.getText().toString().trim();
								
								JSONArray savedTags = new JSONArray(prefs.getString(ThoughtContentProvider.SAVED_TAGS, "[]"));

								boolean add = true;
								
								for (int i = 0; i < savedTags.length() && add == true; i++)
								{
									String tag = savedTags.getString(i);
									
									if (tag.equalsIgnoreCase(newTag))
										add = false;
								}
								
								if (add)
								{
									savedTags.put(newTag);
									e.putString(ThoughtContentProvider.SAVED_TAGS, savedTags.toString());
									e.commit();
								}
							} 
							catch (JSONException ex) 
							{
								LogManager.getInstance(me).logException(ex);
							}

							tags.performClick();
						}
					});

					builder.create().show();
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);

					builder.setTitle(R.string.action_edit_tags);

					builder.setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
	
						}
					});
					
					boolean[] isChecked = new boolean[tagList.length]; 

					ArrayList<String> selectedTags = new ArrayList<String>();
					
					Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
					
					long selectedId = -1;
					
					if (c.moveToPosition(me._index))
					{
						String tagsJson = c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_TAGS));
						
						selectedId = c.getLong(c.getColumnIndex(ThoughtContentProvider.ID));

						try 
						{
							JSONArray tagsArray = new JSONArray(tagsJson);

							for (int i = 0; i < tagsArray.length(); i++)
							{
								String tag = tagsArray.getString(i);
								
								if (selectedTags.contains(tag) == false)
									selectedTags.add(tag);
							}
						} 
						catch (JSONException e) 
						{
							LogManager.getInstance(me).logException(e);
						}

						for (int i = 0; i < tagList.length; i++)
						{
							String tag = tagList[i];
							
							isChecked[i] = selectedTags.contains(tag);
						}
					}
					
					c.close();
					
					final long finalId = selectedId;
					
					builder.setMultiChoiceItems(tagList, isChecked, new OnMultiChoiceClickListener()
					{
						public void onClick(DialogInterface dialog, int position, boolean checked) 
						{
							if (finalId != -1)
							{
								if (checked)
									ThoughtContentProvider.addTag(me, finalId, tagList[position]);
								else
									ThoughtContentProvider.removeTag(me, finalId, tagList[position]);
							}
						}
					});
	
					builder.setPositiveButton(R.string.action_add_tag, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							AlertDialog.Builder builder = new AlertDialog.Builder(me);
							
							builder.setTitle(R.string.action_add_tag);
							
		    				LayoutInflater inflater = LayoutInflater.from(me);
		    				View view = inflater.inflate(R.layout.view_add_tag, null, false);
		    				
		    				builder.setView(view);

		    				final EditText tagField = (EditText) view.findViewById(R.id.field_new_tag);
							
							builder.setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() 
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
									Editor e = prefs.edit();
									
									try 
									{
										String newTag = tagField.getText().toString().trim();

										JSONArray savedTags = new JSONArray(prefs.getString(ThoughtContentProvider.SAVED_TAGS, "[]"));

										boolean add = true;
										
										for (int i = 0; i < savedTags.length() && add == true; i++)
										{
											String tag = savedTags.getString(i);
											
											if (tag.equalsIgnoreCase(newTag))
												add = true;
										}
										
										if (add)
										{
											savedTags.put(newTag);
											e.putString(ThoughtContentProvider.SAVED_TAGS, savedTags.toString());
											e.commit();
										}
									} 
									catch (JSONException ex) 
									{
										LogManager.getInstance(me).logException(ex);
									}
									
									tags.performClick();
								}
							});
	
							builder.create().show();
						}
					});

					builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{
							me.showPair(me._index);
						}
					});
					
					builder.setOnCancelListener(new OnCancelListener()
					{
						public void onCancel(DialogInterface arg0) 
						{
							me.showPair(me._index);
						}
					});

					builder.create().show();
				}
			}
		});
		
		Button distortion = (Button) this.findViewById(R.id.action_select_distortion);
		
		distortion.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.title_distortions);
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(me, R.layout.row_distortion, distortions)
				{
					public View getView (int position, View convertView, ViewGroup parent)
					{
						if (convertView == null)
						{
		    				LayoutInflater inflater = LayoutInflater.from(me);
		    				convertView = inflater.inflate(R.layout.row_distortion, parent, false);
						}
						
						TextView name = (TextView) convertView.findViewById(R.id.label_distortion_name);
						TextView details = (TextView) convertView.findViewById(R.id.label_distortion_details);
						
						String distortion = distortions[position];
						String[] tokens = distortion.split(":");
						
						name.setText(tokens[0].trim());
						details.setText(tokens[1].trim());
						
						return convertView;
					}
				};
				
				builder.setAdapter(adapter, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						me._distortionIndex = which;
						
						Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
						
						if (c.moveToPosition(me._index))
						{
							long id = c.getLong(c.getColumnIndex(ThoughtContentProvider.ID));
							
							ContentValues values = new ContentValues();
							values.put(ThoughtContentProvider.PAIR_DISTORTIONS, distortions[me._distortionIndex]);
							
							String where = ThoughtContentProvider.ID + " = ?";
							String[] args = { "" + id };
							
							me.getContentResolver().update(ThoughtContentProvider.THOUGHT_PAIR_URI, values, where, args);
						}
						
						c.close();
						
						me.showPair(me._index);
					}
				});

				builder.create().show();
			}
		});
	}
	
	protected void onResume()
	{
		super.onResume();
		
		SecureRandom r = new SecureRandom();
		
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_review_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_review, c.getCount()));
		
		this.showPair(r.nextInt(c.getCount()));
		
		c.close();
	}

	private void showPair(int index) 
	{
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);

		if (index < 0)
			this._index = c.getCount() - 1;
		else if (this._index >= c.getCount())
			this._index = 0;
		else
			this._index = index;
		
		if (c.moveToPosition(this._index))
		{
			TextView automatic = (TextView) this.findViewById(R.id.automatic_thought);
			TextView response = (TextView) this.findViewById(R.id.rational_response);
			TextView distortion = (TextView) this.findViewById(R.id.distortion);
			TextView tagsList = (TextView) this.findViewById(R.id.tags);
			
			automatic.setText(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT)));
			response.setText(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_RATIONAL_RESPONSE)));
			
			String distortionValue = c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_DISTORTIONS)); 
			
			if (distortionValue != null && distortionValue.trim().length() > 3)
			{
				distortion.setText(distortionValue);
				distortion.setTypeface(null, Typeface.NORMAL);
			}
			else
			{
				distortion.setText(R.string.placeholder_distortion);
				distortion.setTypeface(null, Typeface.ITALIC);
			}
			
			String tagsValue = c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_TAGS)); 
			
			try 
			{
				JSONArray tags = new JSONArray(tagsValue);

				if (tags.length() > 0)
				{
					StringBuffer sb = new StringBuffer();
					
					for (int i = 0; i < tags.length(); i++)
					{
						if (sb.length() > 0)
							sb.append(", ");
						
						sb.append(tags.getString(i));
					}
					
					tagsList.setText(sb.toString());
					tagsList.setTypeface(null, Typeface.NORMAL);
				}
				else
				{
					tagsList.setText(R.string.placeholder_tags);
					tagsList.setTypeface(null, Typeface.ITALIC);
				}
			}
			catch (JSONException e) 
			{
				LogManager.getInstance(this).logException(e);
			}
		}
		
		c.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_review, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_next:
				this._index += 1;
				this.showPair(this._index);

				break;
			case R.id.action_previous:
				this._index -= 1;
				this.showPair(this._index);
				
				break;
			case R.id.action_list:
				Intent intent = new Intent(this, ThoughtsListActivity.class);
				
				this.startActivity(intent);
				
				break;
		}
		
		return true;
	}
}
