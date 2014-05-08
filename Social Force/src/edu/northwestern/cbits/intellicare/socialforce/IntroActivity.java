package edu.northwestern.cbits.intellicare.socialforce;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class IntroActivity extends ConsentedActivity 
{
	public static final String INTRO_SHOWN = "intro_shown";
	private Menu _menu = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_intro);
        
        final IntroActivity me = this;
        
        final ActionBar actionBar = this.getSupportActionBar();
        
		actionBar.setTitle(me.getString(R.string.title_welcome, 1));
		actionBar.setSubtitle(R.string.subtitle_welcome);
        
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 4;
			}

			public boolean isViewFromObject(View view, Object content) 
			{
				WebView webView = (WebView) view;

				return webView.getTag().equals(content);
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
				WebView webView = new WebView(container.getContext());
				
				switch (position)
				{
					case 0:
						webView.loadUrl("file:///android_asset/www/intro_0.html");
						webView.setTag("0");

						break;
					case 1:
						webView.loadUrl("file:///android_asset/www/intro_1.html");
						webView.setTag("1");

						break;
					case 2:
						webView.loadUrl("file:///android_asset/www/intro_2.html");
						webView.setTag("2");

						break;
					case 3:
						webView.loadUrl("file:///android_asset/www/intro_3.html");
						webView.setTag("3");

						break;
				}
				
				container.addView(webView);

				LayoutParams layout = (LayoutParams) webView.getLayoutParams();
				layout.height = LayoutParams.MATCH_PARENT;
				layout.width = LayoutParams.MATCH_PARENT;
				
				webView.setLayoutParams(layout);

				return webView.getTag();
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
				actionBar.setTitle(me.getString(R.string.title_welcome, page + 1));
				
				if (me._menu != null)
				{
					MenuItem nextItem = me._menu.findItem(R.id.action_next);
					MenuItem backItem = me._menu.findItem(R.id.action_back);
					MenuItem doneItem = me._menu.findItem(R.id.action_rate);

					switch(page)
					{
						case 0:
							nextItem.setVisible(true);
							backItem.setVisible(false);
							doneItem.setVisible(false);
							break;
						case 1:
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 2:
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 3:
							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(true);
							break;
					}
				}
			}
		});
		
		pager.setCurrentItem(0, false);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_intro, menu);
        
        this._menu = menu;

		MenuItem backItem = this._menu.findItem(R.id.action_back);
		MenuItem doneItem = this._menu.findItem(R.id.action_rate);

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
    	else if (item.getItemId() == R.id.action_rate)
    	{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			Editor e = prefs.edit();
			e.putBoolean(IntroActivity.INTRO_SHOWN, true);
			e.commit();
			
			this.finish();
			
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
