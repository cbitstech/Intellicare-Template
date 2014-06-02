package edu.northwestern.cbits.intellicare.aspire;

import java.security.SecureRandom;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class IntroActivityDepricated extends ConsentedActivity
{
	public static final String INTRO_SHOWN = "intro_shown";
	private Menu _menu = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
    	final IntroActivityDepricated me = this;

        this.setContentView(R.layout.activity_sequential);

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
		   //     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me.getApplicationContext());

		        View view = null;
				
				switch (position)
				{
					case 0:
						WebView webView = new WebView(container.getContext());

						webView.loadUrl("file:///android_asset/help_0.html");
						
						view = webView;

						break;

					case 1:
						WebView secondWebView = new WebView(container.getContext());
						secondWebView.loadUrl("file:///android_asset/help_1.html");
						
						break;

					case 2:

						WebView thirdWebView = new WebView(container.getContext());
						thirdWebView.loadUrl("file:///android_asset/help_2.html");
						
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

							break;

						case 2:

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
        this.getMenuInflater().inflate(R.menu.menu_sequential, menu);
        
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
    		e.putBoolean(IntroActivityDepricated.INTRO_SHOWN, true);
    		e.commit();
    		
    		Intent intent = new Intent(this, MainActivity.class);
    		this.startActivity(intent);
    		
    		this.finish();
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
