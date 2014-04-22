package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.util.HashMap;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ChallengeActivity extends ConsentedActivity 
{
	private Menu _menu = null;
	
	private String _thought = null;
	private String _challenge = null;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_challenge);

        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        pager.setOffscreenPageLimit(0);
        
        final ChallengeActivity me = this;
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 3;
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
				View view = null;

				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				switch(position)
				{
					case 0:
						view = inflater.inflate(R.layout.view_catch, null);
						
						final EditText prompt = (EditText) view.findViewById(R.id.field_negative_thought);
						prompt.setText(me._thought);
						
						prompt.addTextChangedListener(new TextWatcher()
						{
							public void afterTextChanged(Editable editable) 
							{
								me._thought = editable.toString(); 
							}

							public void beforeTextChanged(CharSequence s, int start, int count, int after) 
							{

							}

							public void onTextChanged(CharSequence s, int start, int before, int count) 
							{

							}
						});

						TextView helpLink = (TextView) view.findViewById(R.id.link_help);
						
						helpLink.setOnClickListener(new OnClickListener()
						{
							public void onClick(View arg0) 
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(me);
								
								builder.setTitle(R.string.title_negative_thoughts);
								builder.setSingleChoiceItems(R.array.list_negative_thoughts, -1, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{
										String[] thoughts = me.getResources().getStringArray(R.array.list_negative_thoughts);
										
										prompt.setText(thoughts[which]);
										
										HashMap<String, Object> payload = new HashMap<String, Object>();
										payload.put("automatic_thought", thoughts[which]);
										LogManager.getInstance(me).log("selected_thought", payload);
									}
								});
								
								builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{

									}
								});
								
								builder.create().show();
								
								HashMap<String, Object> payload = new HashMap<String, Object>();
								LogManager.getInstance(me).log("showed_examples", payload);
							}
						});

						break;
					case 1:
						view = inflater.inflate(R.layout.view_check, null);

						break;
					case 2:
						view = inflater.inflate(R.layout.view_change, null);

						final EditText changePrompt = (EditText) view.findViewById(R.id.field_replacement_thought);
						changePrompt.setText(me._challenge);

						changePrompt.addTextChangedListener(new TextWatcher()
						{
							public void afterTextChanged(Editable editable) 
							{
								me._challenge = editable.toString(); 
							}

							public void beforeTextChanged(CharSequence s, int start, int count, int after) 
							{

							}

							public void onTextChanged(CharSequence s, int start, int before, int count) 
							{

							}
						});

						TextView changeHelpLink = (TextView) view.findViewById(R.id.link_challenge_help);
						
						changeHelpLink.setOnClickListener(new OnClickListener()
						{
							public void onClick(View arg0) 
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(me);
								
								builder.setTitle(R.string.title_positive_thoughts);
								builder.setSingleChoiceItems(R.array.list_positive_thoughts, -1, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{
										String[] thoughts = me.getResources().getStringArray(R.array.list_positive_thoughts);
										
										changePrompt.setText(thoughts[which]);
									}
								});
								
								builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface dialog, int which) 
									{

									}
								});
								
								builder.create().show();
								
								HashMap<String, Object> payload = new HashMap<String, Object>();
								LogManager.getInstance(me).log("showed_examples", payload);
							}
						});

						break;
				}
						
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
				MenuItem backItem = me._menu.findItem(R.id.action_back);
				MenuItem nextItem = me._menu.findItem(R.id.action_next);
				MenuItem doneItem = me._menu.findItem(R.id.action_done);
				MenuItem helpItem = me._menu.findItem(R.id.action_help);

				
				ActionBar actionBar = me.getSupportActionBar();
				
				switch(page)
				{
					case 0:
						actionBar.setTitle(R.string.title_catch);
						actionBar.setSubtitle(R.string.subtitle_catch);
						
						backItem.setVisible(false);
						doneItem.setVisible(false);
						nextItem.setVisible(true);
						helpItem.setVisible(true);

						break;
					case 1:
						actionBar.setTitle(R.string.title_check);
						actionBar.setSubtitle(R.string.subtitle_check);

						backItem.setVisible(true);
						doneItem.setVisible(false);
						nextItem.setVisible(true);
						helpItem.setVisible(false);

						if (me._thought == null || me._thought.length() < 3)
						{
							Toast.makeText(me, R.string.step_one_continue, Toast.LENGTH_LONG).show();
							
							pager.setCurrentItem(0);
						}

						break;
					case 2:
						actionBar.setTitle(R.string.title_change);
						actionBar.setSubtitle(R.string.subtitle_change);

						backItem.setVisible(true);
						doneItem.setVisible(true);
						nextItem.setVisible(false);
						helpItem.setVisible(true);

						break;
				}
			}
		});
	}

    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_challenge, menu);
        
        this._menu = menu;

		MenuItem backItem = this._menu.findItem(R.id.action_back);
		MenuItem doneItem = this._menu.findItem(R.id.action_done);

		backItem.setVisible(false);
		doneItem.setVisible(false);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
    	
    	if (item.getItemId() == R.id.action_next)
    	{
    		pager.setCurrentItem(pager.getCurrentItem() + 1);
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_back)
    	{
    		pager.setCurrentItem(pager.getCurrentItem() - 1);
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_done)
    	{
    		if (pager.getCurrentItem() == 0)
    		{
    			
    		}
    		else if (pager.getCurrentItem() == 1)
    		{
    			
    		}
    		else if (pager.getCurrentItem() == 2)
    		{
				if (this._challenge == null || this._challenge.length() < 3)
					Toast.makeText(this, R.string.step_three_continue, Toast.LENGTH_LONG).show();
				else
				{
					ContentValues values = new ContentValues();
					values.put(ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT, this._thought);
					values.put(ThoughtContentProvider.PAIR_RATIONAL_RESPONSE, this._challenge);
					values.put(ThoughtContentProvider.PAIR_DISTORTIONS, "");
					values.put(ThoughtContentProvider.PAIR_TAGS, (new JSONArray()).toString());
					
					this.getContentResolver().insert(ThoughtContentProvider.THOUGHT_PAIR_URI, values);
					
					Intent intent = new Intent(this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					this.startActivity(intent);
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("response", this._challenge);
					LogManager.getInstance(this).log("selected_response", payload);
				}
    		}
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_help)
    	{
    		if (pager.getCurrentItem() == 0)
    		{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					
					builder.setTitle(R.string.title_catch_help);
					builder.setMessage(R.string.message_catch_help);
					
					builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{

						}
					});
					
					builder.create().show();
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					LogManager.getInstance(this).log("showed_catch_help", payload);
    		}
    		else if (pager.getCurrentItem() == 2)
    		{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_change_help);
				builder.setMessage(R.string.message_change_help);
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
	
					}
				});
				
				builder.create().show();

				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(this).log("showed_help", payload);
    		}
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
