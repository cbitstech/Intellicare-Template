package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ReviewActivity extends ConsentedActivity 
{
	public static final int FETCH_THOUGHT = 675;
	static final String THOUGHT_INDEX = "thought_index";
	
	private int _distortionIndex = -1;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_review);

		this.getSupportActionBar().setTitle(R.string.title_review);

		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		final int count = c.getCount();
		c.close();

		if (count == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_review_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_review, count));
		
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        pager.setOffscreenPageLimit(0);
        
        final ReviewActivity me = this;
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return count;
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
				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = inflater.inflate(R.layout.view_review, null);
						
				Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, ThoughtContentProvider.ID);

				if (c.moveToPosition(position))
				{					
					long id = c.getLong(c.getColumnIndex(ThoughtContentProvider.ID));

					String where = ThoughtContentProvider.ID + " = ?";
					String[] args = { "" + id };
					
					Cursor cursor = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, where, args, null);		

					if (cursor.moveToNext())
					{
						TextView automatic = (TextView) view.findViewById(R.id.automatic_thought);
						TextView response = (TextView) view.findViewById(R.id.rational_response);
						TextView distortion = (TextView) view.findViewById(R.id.distortion);
						TextView tagsList = (TextView) view.findViewById(R.id.tags);
						
						automatic.setText(cursor.getString(cursor.getColumnIndex(ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT)));
						response.setText(cursor.getString(cursor.getColumnIndex(ThoughtContentProvider.PAIR_RATIONAL_RESPONSE)));
						
						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("automatic_thought", automatic.getText().toString());
						payload.put("rational_response", response.getText().toString());
						LogManager.getInstance(me).log("showed_pair", payload);
						
						String distortionValue = cursor.getString(cursor.getColumnIndex(ThoughtContentProvider.PAIR_DISTORTIONS)); 
						
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
						
						String tagsValue = cursor.getString(cursor.getColumnIndex(ThoughtContentProvider.PAIR_TAGS)); 
						
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
							LogManager.getInstance(me).logException(e);
						}
					}
					
					cursor.close();
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
		pager.setOnPageChangeListener(new OnPageChangeListener()
		{
			public void onPageScrollStateChanged(int arg0) 
			{

			}

			public void onPageScrolled(int arg0, float arg1, int arg2) 
			{

			}

			public void onPageSelected(int page) 
			{
			}
		});
		/*
		
		final ReviewActivity me = this;
		

		Button distortion = (Button) this.findViewById(R.id.action_select_distortion);
		
		distortion.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
			}
		});
		
		SecureRandom r = new SecureRandom();
		
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_review_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_review, c.getCount()));
		
		this.showPair(r.nextInt(c.getCount()));
		
		*/
	}
	
	protected void onResume()
	{
		super.onResume();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_review", payload);
	}
	
	protected void onPause()
	{
		super.onPause();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_review", payload);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_review, menu);

		return true;
	}
	
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == ReviewActivity.FETCH_THOUGHT)
			{
				if (data.hasExtra(ReviewActivity.THOUGHT_INDEX))
				{
					int index = data.getIntExtra(ReviewActivity.THOUGHT_INDEX, 0);
					
			        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
			        
			        pager.setCurrentItem(index);

				}
			}
		}		
	}
	
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		int itemId = item.getItemId();
		
		final ReviewActivity me = this;
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
		AlertDialog.Builder builder = new AlertDialog.Builder(me);

		switch (itemId)
		{
			case R.id.action_distortion:
				builder.setTitle(R.string.title_distortions);

				final String[] distortions = this.getResources().getStringArray(R.array.list_distortions);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_distortion, distortions)
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
						
						Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, ThoughtContentProvider.ID);

						if (c.moveToPosition(pager.getCurrentItem()))
						{
							long id = c.getLong(c.getColumnIndex(ThoughtContentProvider.ID));
							
							ContentValues values = new ContentValues();
							values.put(ThoughtContentProvider.PAIR_DISTORTIONS, distortions[me._distortionIndex]);
							
							String where = ThoughtContentProvider.ID + " = ?";
							String[] args = { "" + id };
							
							me.getContentResolver().update(ThoughtContentProvider.THOUGHT_PAIR_URI, values, where, args);
							
							HashMap<String, Object> payload = new HashMap<String, Object>();
							payload.put("distortion", distortions[me._distortionIndex]);
							LogManager.getInstance(me).log("selected_distortion", payload);
						}
						
						c.close();

						c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, ThoughtContentProvider.ID);

						if (c.moveToPosition(pager.getCurrentItem()))
						{
							View view = pager.findViewWithTag("" + pager.getCurrentItem());
							TextView distortion = (TextView) view.findViewById(R.id.distortion);

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
						}
						
						c.close();
					}
				});

				builder.create().show();
				
				break;
			case R.id.action_list:
				Intent intent = new Intent(this, ThoughtsListActivity.class);
				
				this.startActivityForResult(intent, ReviewActivity.FETCH_THOUGHT);
				
				break;
			case R.id.action_tag:

				final String[] tagList = ThoughtContentProvider.fetchTags(this);
				
				if (tagList.length == 0)
				{
					builder.setTitle(R.string.action_add_tag);

    				LayoutInflater inflater = LayoutInflater.from(this);
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
								
								if (newTag.length() > 0)
								{
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
										HashMap<String, Object> payload = new HashMap<String, Object>();
										payload.put("tag", newTag);
										LogManager.getInstance(me).log("added_tag", payload);
	
										savedTags.put(newTag);
										e.putString(ThoughtContentProvider.SAVED_TAGS, savedTags.toString());
										e.commit();
									}
								}
							} 
							catch (JSONException ex) 
							{
								LogManager.getInstance(me).logException(ex);
							}

							me.onOptionsItemSelected(item);
						}
					});

					builder.create().show();
				}
				else
				{
					builder.setTitle(R.string.action_edit_tags);

					builder.setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
	
						}
					});
					
					boolean[] isChecked = new boolean[tagList.length]; 

					ArrayList<String> selectedTags = new ArrayList<String>();
					
					Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, ThoughtContentProvider.ID);
					
					long selectedId = -1;
					
					if (c.moveToPosition(pager.getCurrentItem()))
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
								HashMap<String, Object> payload = new HashMap<String, Object>();
								payload.put("tag", tagList[position]);

								if (checked)
								{
									ThoughtContentProvider.addTag(me, finalId, tagList[position]);

									LogManager.getInstance(me).log("added_tag", payload);
								}
								else
								{
									ThoughtContentProvider.removeTag(me, finalId, tagList[position]);

									LogManager.getInstance(me).log("removed_tag", payload);
								}
								
								String where = ThoughtContentProvider.ID + " = ?";
								String[] args = { "" + finalId };

								Cursor cursor = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, where, args, null);		

								if (cursor.moveToNext())
								{
									View view = pager.findViewWithTag("" + pager.getCurrentItem());
									
									TextView tagsList = (TextView) view.findViewById(R.id.tags);
									
									String tagsValue = cursor.getString(cursor.getColumnIndex(ThoughtContentProvider.PAIR_TAGS)); 
									
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
										LogManager.getInstance(me).logException(e);
									}
								}
								
								cursor.close();
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
											HashMap<String, Object> payload = new HashMap<String, Object>();
											payload.put("tag", newTag);
											LogManager.getInstance(me).log("added_tag", payload);

											savedTags.put(newTag);
											e.putString(ThoughtContentProvider.SAVED_TAGS, savedTags.toString());
											e.commit();
										}
									} 
									catch (JSONException ex) 
									{
										LogManager.getInstance(me).logException(ex);
									}
									
									me.onOptionsItemSelected(item);
								}
							});
	
							builder.create().show();
						}
					});

					builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{

						}
					});

					builder.create().show();
				}
				
				break;
		}
		
		return true;
	}
}
