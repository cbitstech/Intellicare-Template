package edu.northwestern.cbits.intellicare.socialforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class RatingActivity extends ConsentedActivity 
{
	public static final String CONTACTS_RATED = "contacts_rated";

	private Menu _menu = null;
	private List<ContactRecord> _contacts = null;
	
    @SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_intro);
        
        final RatingActivity me = this;
        
        final ActionBar actionBar = this.getSupportActionBar();
        
		actionBar.setTitle(me.getString(R.string.title_welcome, 1));
		actionBar.setSubtitle(R.string.subtitle_welcome);
		
		this._contacts = ContactCalibrationHelper.fetchContactRecords(this);
        
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 5;
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

				View view = null;

				final String[] levels = me.getResources().getStringArray(R.array.contact_roles);
				
				switch (position)
				{
					case 0:
						WebView webViewZero = new WebView(container.getContext());
						
						webViewZero.loadUrl("file:///android_asset/www/rating_0.html");

						view = webViewZero;
						break;
					case 1:
						view = inflater.inflate(R.layout.view_contact_rater, null);
						
						if (view instanceof ListView)
						{
							ListView list = (ListView) view;
							
					    	list.setAdapter(new ArrayAdapter<ContactRecord>(me, R.layout.row_contact, me._contacts)
			    			{
			    	    		public View getView (int position, View convertView, ViewGroup parent)
			    	    		{
			    	    			if (convertView == null)
			    	    			{
			    	    				LayoutInflater inflater = LayoutInflater.from(me);
			    	    				convertView = inflater.inflate(R.layout.row_contact, parent, false);
			    	    			}
			    	    			
			    	    			TextView contactName = (TextView) convertView.findViewById(R.id.label_contact_name);
			    	    			TextView contactNumber = (TextView) convertView.findViewById(R.id.label_contact_number);
			    	    			TextView contactType = (TextView) convertView.findViewById(R.id.label_contact_type);
			    	    			
			    	    			ContactRecord contact = this.getItem(position);
			    	    			
			    	    			if ("".equals(contact.name) == false)
			    	    				contactName.setText(contact.name);
			    	    			else
			    	    				contactName.setText(contact.number);
			    	    			
			    	    			if (contact.count != 1)
			    	    				contactNumber.setText(me.getString(R.string.label_contact_count, contact.number, contact.count));
			    	    			else
			    	    				contactNumber.setText(me.getString(R.string.label_contact_count_single, contact.number));
			    					
			    					if (contact.level >= 0)
			    					{
			    						if (me._menu != null)
			    						{
				    						MenuItem nextItem = me._menu.findItem(R.id.action_next);
				    						nextItem.setVisible(true);
			    						}
			    						
			    						contactType.setText(levels[contact.level]);
			    					}
			    					else
			    						contactType.setText(R.string.placeholder_rate);

			    	    			return convertView;
			    	    		}
			    	    	});
					    	
					    	list.setOnItemClickListener(new OnItemClickListener()
					    	{
								public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) 
								{
									final ContactRecord contact = me._contacts.get(position);
									
									AlertDialog.Builder builder = new AlertDialog.Builder(me);
									builder.setTitle(contact.name);
									
									builder.setItems(R.array.contact_roles, new OnClickListener()
									{
										public void onClick(DialogInterface dialog, int which) 
										{
					    	    			ContactRecord contact = me._contacts.get(position);
					    	    			contact.level = which;

					    	    			TextView contactType = (TextView) view.findViewById(R.id.label_contact_type);
					    	    			
					    	    			ContactCalibrationHelper.setLevel(me, contact.key, which);

					    					MenuItem nextItem = me._menu.findItem(R.id.action_next);

					    					if (which >= 0)
					    						contactType.setText(levels[which]);
					    					else
					    						contactType.setText(R.string.placeholder_rate);
					    					
					    					nextItem.setVisible(true);
										}
									});
									
									builder.create().show();
								}
					    	});
						}

						break;
					case 2:
						WebView webViewThree = new WebView(container.getContext());
						
						webViewThree.loadUrl("file:///android_asset/www/rating_3.html");

						view = webViewThree;

						break;
					case 3:
						view = inflater.inflate(R.layout.view_contact_category, null);
						
						ListView list = (ListView) view.findViewById(R.id.rater_list);
						
						ArrayList<ContactRecord> contacts = new ArrayList<ContactRecord>();
						
						for (ContactRecord contact : me._contacts)
						{
							if (contact.level >= 0 && contact.level < 3)
								contacts.add(contact);
						}
						
						Collections.sort(contacts, new Comparator<ContactRecord>()
						{
							public int compare(ContactRecord one, ContactRecord two) 
							{
								if (one.level < two.level)
									return -1;
								else if (one.level > two.level)
									return 1;
								else if (one.count < two.count)
									return -1;
								else if (one.count > two.count)
									return 1;
								
								return one.name.compareTo(two.name);
							}
						});
						
				    	list.setAdapter(new ArrayAdapter<ContactRecord>(me, R.layout.row_category, contacts)
		    			{
		    	    		@SuppressWarnings("deprecation")
							public View getView (int position, View convertView, ViewGroup parent)
		    	    		{
		    	    			if (convertView == null)
		    	    			{
		    	    				LayoutInflater inflater = LayoutInflater.from(me);
		    	    				convertView = inflater.inflate(R.layout.row_category, parent, false);
		    	    			}
		    	    			
		    	    			TextView contactName = (TextView) convertView.findViewById(R.id.label_contact_name);
		    	    			TextView contactNumber = (TextView) convertView.findViewById(R.id.label_contact_number);
		    	    			
		    	    			final ContactRecord contact = this.getItem(position);
		    	    			
		    	    			if ("".equals(contact.name) == false)
		    	    				contactName.setText(contact.name);
		    	    			else
		    	    				contactName.setText(contact.number);
		    	    			
		    	    			if (contact.count != 1)
		    	    				contactNumber.setText(me.getString(R.string.label_contact_count, contact.number, contact.count));
		    	    			else
		    	    				contactNumber.setText(me.getString(R.string.label_contact_count_single, contact.number));

		    					final ImageView practical = (ImageView) convertView.findViewById(R.id.practical_item);

		    					OvalShape practicalOval = new OvalShape();

		    					ShapeDrawable practicalCircle = new ShapeDrawable(practicalOval);
		    					practicalCircle.setIntrinsicHeight(32);
		    					practicalCircle.setIntrinsicWidth(32);
		    					practicalCircle.setBounds(0, 0, 32, 32);
		    					practicalCircle.getPaint().setColor(0xff669900);
		    					
		    					practical.setImageDrawable(practicalCircle);
		    					
		    					if (ContactCalibrationHelper.isPractical(me, contact))
		    						practical.setAlpha(255);
		    					else
		    						practical.setAlpha(64);

		    					practical.setOnClickListener(new View.OnClickListener()
		    					{
									public void onClick(View arg0) 
									{
				    					if (ContactCalibrationHelper.isPractical(me, contact))
				    					{
				    						ContactCalibrationHelper.setPractical(me, contact, false);
				    						practical.setAlpha(64);
				    					}
				    					else
				    					{
				    						ContactCalibrationHelper.setPractical(me, contact, true);
				    						practical.setAlpha(255);
				    					}
									}
		    					});

		    					final ImageView advice = (ImageView) convertView.findViewById(R.id.advice_item);

		    					OvalShape adviceOval = new OvalShape();

		    					ShapeDrawable adviceCircle = new ShapeDrawable(adviceOval);
		    					adviceCircle.setIntrinsicHeight(32);
		    					adviceCircle.setIntrinsicWidth(32);
		    					adviceCircle.setBounds(0, 0, 32, 32);
		    					adviceCircle.getPaint().setColor(0xff0099CC);
		    					
		    					advice.setImageDrawable(adviceCircle);

		    					if (ContactCalibrationHelper.isAdvice(me, contact))
		    						advice.setAlpha(255);
		    					else
		    						advice.setAlpha(64);

		    					advice.setOnClickListener(new View.OnClickListener()
		    					{
									public void onClick(View arg0) 
									{
				    					if (ContactCalibrationHelper.isAdvice(me, contact))
				    					{
				    						ContactCalibrationHelper.setAdvice(me, contact, false);
				    						advice.setAlpha(64);
				    					}
				    					else
				    					{
				    						ContactCalibrationHelper.setAdvice(me, contact, true);
				    						advice.setAlpha(255);
				    					}
									}
		    					});

		    					final ImageView companion = (ImageView) convertView.findViewById(R.id.companion_item);
		    					
		    					OvalShape companionOval = new OvalShape();

		    					ShapeDrawable companionCircle = new ShapeDrawable(companionOval);
		    					companionCircle.setIntrinsicHeight(32);
		    					companionCircle.setIntrinsicWidth(32);
		    					companionCircle.setBounds(0, 0, 32, 32);
		    					companionCircle.getPaint().setColor(0xff9933CC);
		    					
		    					companion.setImageDrawable(companionCircle);

		    					if (ContactCalibrationHelper.isCompanion(me, contact))
		    						companion.setAlpha(255);
		    					else
		    						companion.setAlpha(64);

		    					companion.setOnClickListener(new View.OnClickListener()
		    					{
									public void onClick(View arg0) 
									{
				    					if (ContactCalibrationHelper.isCompanion(me, contact))
				    					{
				    						ContactCalibrationHelper.setCompanion(me, contact, false);
				    						companion.setAlpha(64);
				    					}
				    					else
				    					{
				    						ContactCalibrationHelper.setCompanion(me, contact, true);
				    						companion.setAlpha(255);
				    					}
									}
		    					});

		    					final ImageView emotional = (ImageView) convertView.findViewById(R.id.emotional_item);

		    					OvalShape emotionalOval = new OvalShape();

		    					ShapeDrawable emotionalCircle = new ShapeDrawable(emotionalOval);
		    					emotionalCircle.setIntrinsicHeight(32);
		    					emotionalCircle.setIntrinsicWidth(32);
		    					emotionalCircle.setBounds(0, 0, 32, 32);
		    					emotionalCircle.getPaint().setColor(0xffCC0000);
		    					
		    					emotional.setImageDrawable(emotionalCircle);

		    					if (ContactCalibrationHelper.isEmotional(me, contact))
		    						emotional.setAlpha(255);
		    					else
		    						emotional.setAlpha(64);

		    					emotional.setOnClickListener(new View.OnClickListener()
		    					{
									public void onClick(View arg0) 
									{
				    					if (ContactCalibrationHelper.isEmotional(me, contact))
				    					{
				    						ContactCalibrationHelper.setEmotional(me, contact, false);
				    						emotional.setAlpha(64);
				    					}
				    					else
				    					{
				    						ContactCalibrationHelper.setEmotional(me, contact, true);
				    						emotional.setAlpha(255);
				    					}
									}
		    					});

		    	    			return convertView;
		    	    		}
		    	    	});
						
    					final ImageView emotional = (ImageView) view.findViewById(R.id.legend_emotional_item);

    					OvalShape emotionalOval = new OvalShape();

    					ShapeDrawable emotionalCircle = new ShapeDrawable(emotionalOval);
    					emotionalCircle.setIntrinsicHeight(32);
    					emotionalCircle.setIntrinsicWidth(32);
    					emotionalCircle.setBounds(0, 0, 32, 32);
    					emotionalCircle.getPaint().setColor(0xffCC0000);
    					
    					emotional.setImageDrawable(emotionalCircle);
    					
    					final ImageView companion = (ImageView) view.findViewById(R.id.legend_companion_item);
    					
    					OvalShape companionOval = new OvalShape();

    					ShapeDrawable companionCircle = new ShapeDrawable(companionOval);
    					companionCircle.setIntrinsicHeight(32);
    					companionCircle.setIntrinsicWidth(32);
    					companionCircle.setBounds(0, 0, 32, 32);
    					companionCircle.getPaint().setColor(0xff9933CC);
    					
    					companion.setImageDrawable(companionCircle);

    					final ImageView practical = (ImageView) view.findViewById(R.id.legend_practical_item);
    					
    					OvalShape practicalOval = new OvalShape();

    					ShapeDrawable practicalCircle = new ShapeDrawable(practicalOval);
    					practicalCircle.setIntrinsicHeight(32);
    					practicalCircle.setIntrinsicWidth(32);
    					practicalCircle.setBounds(0, 0, 32, 32);
    					practicalCircle.getPaint().setColor(0xff669900);
    					
    					practical.setImageDrawable(practicalCircle);

    					final ImageView advice = (ImageView) view.findViewById(R.id.legend_advice_item);

    					OvalShape adviceOval = new OvalShape();

    					ShapeDrawable adviceCircle = new ShapeDrawable(adviceOval);
    					adviceCircle.setIntrinsicHeight(32);
    					adviceCircle.setIntrinsicWidth(32);
    					adviceCircle.setBounds(0, 0, 32, 32);
    					adviceCircle.getPaint().setColor(0xff0099CC);
    					
    					advice.setImageDrawable(adviceCircle);



						break;
					case 4:
						view = inflater.inflate(R.layout.view_my_network, null);
						
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
						Editor e = prefs.edit();
						e.putBoolean(RatingActivity.CONTACTS_RATED, true);
						e.commit();
						
						Button scheduleButton = (Button) view.findViewById(R.id.button_yes);
						
						scheduleButton.setOnClickListener(new View.OnClickListener() 
						{
							public void onClick(View v) 
							{
								Intent intent = new Intent(me, ScheduleActivity.class);
								me.startActivity(intent);
								
								me.finish();
							}
						});
						
						Button meetButton = (Button) view.findViewById(R.id.button_no);
						
						meetButton.setOnClickListener(new View.OnClickListener() 
						{
							public void onClick(View v) 
							{
								Intent intent = new Intent(me, NetworkActivity.class);
								me.startActivity(intent);
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
				actionBar.setSubtitle(me.getString(R.string.subtitle_rating, page + 1));
				
				if (me._menu != null)
				{
					MenuItem nextItem = me._menu.findItem(R.id.action_next);
					MenuItem backItem = me._menu.findItem(R.id.action_back);
					MenuItem doneItem = me._menu.findItem(R.id.action_done);

					switch(page)
					{
						case 0:
							actionBar.setTitle(R.string.title_people_rater);
							nextItem.setVisible(true);
							backItem.setVisible(false);
							doneItem.setVisible(false);
							break;
						case 1:
							actionBar.setTitle(R.string.title_people_rater_tool);
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 2:
							actionBar.setTitle(R.string.title_people_rater);
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 3:
							actionBar.setTitle(R.string.title_people_category_tool);
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 4:
							actionBar.setTitle(R.string.title_my_network);
							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(false);

							
							WebView graphView = (WebView) me.findViewById(R.id.network_visualization);
							graphView.getSettings().setJavaScriptEnabled(true);
							graphView.getSettings().setBuiltInZoomControls(true);
							graphView.getSettings().setDisplayZoomControls(false);
							graphView.getSettings().setLoadWithOverviewMode(true);
							graphView.getSettings().setUseWideViewPort(true);
							graphView.setInitialScale(1);
							
							graphView.addJavascriptInterface(me, "android");
							graphView.loadDataWithBaseURL("file:///android_asset/viz/", MainActivity.generateBubbles(me, MainActivity.STATE_ALL), "text/html", null, null);
							
							break;
					}
				}
			}
		});
		
		pager.setCurrentItem(0, false);
		
		actionBar.setTitle(R.string.title_people_rater);
		actionBar.setSubtitle(me.getString(R.string.subtitle_rating, 1));
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_rate, menu);
        
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
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			Editor e = prefs.edit();
			e.putBoolean(RatingActivity.CONTACTS_RATED, true);
			e.commit();
			
			this.finish();
			
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
    
    @JavascriptInterface
    public void selectByName(String name)
    {
    	// Intentionally empty.
    }
}
