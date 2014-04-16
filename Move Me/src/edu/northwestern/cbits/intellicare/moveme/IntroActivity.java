package edu.northwestern.cbits.intellicare.moveme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
	private int _goal = 50;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_intro);
        
        final IntroActivity me = this;
        
        ActionBar actionBar = this.getSupportActionBar();
        
        actionBar.setTitle(R.string.title_welcome);
        
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 2;
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
							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(true);
							break;
					}
				}
			}
		});
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
    		pager.setCurrentItem(1);
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_back)
    	{
    		pager.setCurrentItem(0);
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_done)
    	{
            final IntroActivity me = this;
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			if (prefs.contains(DashboardActivity.WEEKLY_GOAL))
				this.finish();
			else
			{
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		builder.setTitle(R.string.title_set_goal);
	    		builder.setSingleChoiceItems(R.array.array_goals, 0, new OnClickListener()
	    		{
					public void onClick(DialogInterface arg0, int which) 
					{
						switch (which)
						{
							case 0:
								me._goal  = 50;
								break;
							case 1:
								me._goal = 100;
								break;
							case 2:
								me._goal = 150;
								break;
							case 3:
								me._goal = 200;
								break;
						}
					}
	    		});
	    		
	    		builder.setPositiveButton(R.string.action_continue, new OnClickListener()
	    		{
					public void onClick(DialogInterface dialog, int which) 
					{
						Editor e = prefs.edit();
						e.putInt(DashboardActivity.WEEKLY_GOAL, me._goal);
						e.commit();
						
						me.finish();
					}
	    		});
	    		
	    		builder.create().show();
			}
			
			Editor e = prefs.edit();
			e.putBoolean(IntroActivity.INTRO_SHOWN, true);
			e.commit();
			
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
