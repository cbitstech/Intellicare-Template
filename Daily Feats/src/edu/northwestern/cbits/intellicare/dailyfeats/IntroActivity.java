package edu.northwestern.cbits.intellicare.dailyfeats;

import java.security.SecureRandom;
import java.util.HashMap;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class IntroActivity extends ConsentedActivity 
{
	public static final String INTRO_SHOWN = "intro_shown";

	protected int _dialogId = 0;

	private Menu _menu = null;
	
	final IntroActivity me = this;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_intro);

        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        pager.setOffscreenPageLimit(0);
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 6;
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
		        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me.getApplicationContext());

		        View view = null;
				
				switch (position)
				{
					case 0:
						WebView webView = new WebView(container.getContext());

						webView.loadUrl("file:///android_asset/help_0.html");
						
						view = webView;

						break;
					case 1:
						LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						view = inflater.inflate(R.layout.view_mood_intro, null, false);

				        RadioGroup depression = (RadioGroup) view.findViewById(R.id.depression_level);

				        if (prefs.contains(FeatsProvider.DEPRESSION_LEVEL))
				        {
				        	int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 0);
				        	
				        	Log.e("DF", "GOT LEVEL: " + level);
				        	
				        	int id = -1;
				        	
							switch(level)
							{
								case 1:
									id = R.id.depression_question_0;
									break;
								case 2:
									id = R.id.depression_question_1;
									break;
								case 3:
									id = R.id.depression_question_2;
									break;
								case 4: 
									id = R.id.depression_question_3;
									break;
							}
							
							Log.e("DF", "GOT ID: " + id);
							
							if (id != -1)
							{
								RadioButton button = (RadioButton) depression.findViewById(id);
								
								Log.e("DF", "GOT BUTTON: " + button);
								
								button.setChecked(true);
							}
				        }
				        			
				        depression.setOnCheckedChangeListener(new OnCheckedChangeListener()
				        {
							public void onCheckedChanged(RadioGroup group, int id) 
							{
						        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me.getApplicationContext());
								Editor e = prefs.edit();
								
								switch(id)
								{
									case R.id.depression_question_0:
										e.putInt(FeatsProvider.DEPRESSION_LEVEL, 1);
										break;
									case R.id.depression_question_1:
										e.putInt(FeatsProvider.DEPRESSION_LEVEL, 2);
										break;
									case R.id.depression_question_2:
										e.putInt(FeatsProvider.DEPRESSION_LEVEL, 3);
										break;
									case R.id.depression_question_3:
										e.putInt(FeatsProvider.DEPRESSION_LEVEL, 4);
										break;
								}
								
								e.commit();
								
								MenuItem nextItem = me._menu.findItem(R.id.action_next);

								nextItem.setVisible(true);
							}
				        });
						
						break;
					case 2:
						WebView msgWebView = new WebView(container.getContext());
						msgWebView.loadUrl("file:///android_asset/help_2.html");
						
						view = msgWebView;
						
						break;
					case 3:
						WebView helpWebView = new WebView(container.getContext());
						
						int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 0); 

						switch (level)
						{
							case 4:
								helpWebView.loadUrl("file:///android_asset/help_3_4.html");
								break;
							case 3:
								helpWebView.loadUrl("file:///android_asset/help_3_3.html");
								break;
							default:
								helpWebView.loadUrl("file:///android_asset/help_3_12.html");
								break;
						}
						
						view = helpWebView;
						
						FeatsProvider.updateLevel(me, level);
						
						break;
					case 4:
						WebView remindWebView = new WebView(container.getContext());
						remindWebView.loadUrl("file:///android_asset/help_5.html");
						
						view = remindWebView;
						
						break;
					case 5:
						WebView doneWebView = new WebView(container.getContext());
						doneWebView.loadUrl("file:///android_asset/help_8.html");
						
						view = doneWebView;
						
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
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
				
				if (me._menu != null)
				{
					MenuItem nextItem = me._menu.findItem(R.id.action_next);
					MenuItem backItem = me._menu.findItem(R.id.action_back);
					MenuItem doneItem = me._menu.findItem(R.id.action_done);

					switch(page)
					{
						case 0:
							nextItem.setVisible(true);
							backItem.setVisible(false);
							doneItem.setVisible(false);
							break;
						case 1:
							backItem.setVisible(true);
							doneItem.setVisible(false);

							if (prefs.contains(FeatsProvider.DEPRESSION_LEVEL))
								nextItem.setVisible(true);
							else
								nextItem.setVisible(false);

							break;
						case 2:
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							
							if (prefs.contains(FeatsProvider.DEPRESSION_LEVEL) == false)
							{
								Toast.makeText(me, R.string.depression_toast, Toast.LENGTH_LONG).show();
								pager.setCurrentItem(1);
							}
							
							break;
						case 3:
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 4:
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);

							break;
						case 5:
					    	int hour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
					        int minutes = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

							TimePickerDialog timeDialog = new TimePickerDialog(me, new OnTimeSetListener()
							{
								public void onTimeSet(TimePicker arg0, int hour, int minute) 
								{
							        Editor editor = prefs.edit();
							        
							        editor.putInt(ScheduleManager.REMINDER_HOUR, hour);
							        editor.putInt(ScheduleManager.REMINDER_MINUTE, minute);
							        editor.commit();

									HashMap<String, Object> payload = new HashMap<String, Object>();
									payload.put("hour", hour);
									payload.put("minute", minute);
									payload.put("source", "intro");
									
									LogManager.getInstance(me).log("set_reminder_time", payload);
								}
								
							}, hour, minutes, DateFormat.is24HourFormat(me));
							
							timeDialog.show();

							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(true);
							
							break;
					}
				}
			}
		});
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.contains("settings_full_mode") == false)
        {
        	Editor e = prefs.edit();
        	
        	SecureRandom random = new SecureRandom();
        	random.setSeed(System.currentTimeMillis());
        	
        	boolean isFull = random.nextBoolean(); 
        	e.putBoolean("settings_full_mode", isFull);
        	e.commit();

			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("full_mode", isFull);
        	
        	LogManager.getInstance(this).log("set_mode", payload);
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_intro, menu);
        
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
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		Editor e = prefs.edit();
    		e.putBoolean(IntroActivity.INTRO_SHOWN, true);
    		e.commit();
    		
    		Intent intent = new Intent(this, CalendarActivity.class);
    		me.startActivity(intent);
    		
    		me.finish();
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
