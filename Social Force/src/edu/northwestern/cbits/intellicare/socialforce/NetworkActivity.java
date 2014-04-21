package edu.northwestern.cbits.intellicare.socialforce;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.webkit.WebView;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class NetworkActivity extends ConsentedActivity 
{
	private Menu _menu = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_network);
        
        final NetworkActivity me = this;
        
        final ActionBar actionBar = this.getSupportActionBar();
        
		actionBar.setTitle(R.string.title_network);
		
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        pager.setOffscreenPageLimit(0);
        
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

				switch (position)
				{
					case 0:
						WebView webViewZero = new WebView(container.getContext());
						webViewZero.loadUrl("file:///android_asset/www/network_0.html");
						view = webViewZero;

						break;
					case 1:
						view = inflater.inflate(R.layout.view_network_tricks, null);
						
						Button one = (Button) view.findViewById(R.id.step_one);
						
						one.setOnClickListener(new View.OnClickListener()
						{
							public void onClick(View arg0) 
							{
								pager.setCurrentItem(2, true);
							}
						});

						Button two = (Button) view.findViewById(R.id.step_two);
						
						two.setOnClickListener(new View.OnClickListener()
						{
							public void onClick(View arg0) 
							{
								pager.setCurrentItem(4, true);
							}
						});


						Button three = (Button) view.findViewById(R.id.step_three);
						
						three.setOnClickListener(new View.OnClickListener()
						{
							public void onClick(View arg0) 
							{
								Intent intent = new Intent(me, ScheduleActivity.class);
								me.startActivity(intent);
								
								me.finish();
							}
						});
						
						break;
					case 2:
						WebView webViewTwo = new WebView(container.getContext());
						webViewTwo.loadUrl("file:///android_asset/www/network_2.html");
						view = webViewTwo;

						break;
					case 3:
						WebView webViewThree = new WebView(container.getContext());
						webViewThree.loadUrl("file:///android_asset/www/network_3.html");
						view = webViewThree;

						break;
					case 4:
						WebView webViewFour = new WebView(container.getContext());
						webViewFour.loadUrl("file:///android_asset/www/network_4.html");
						view = webViewFour;

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
							actionBar.setTitle(R.string.title_network);
							actionBar.setSubtitle(null);

							nextItem.setVisible(true);
							backItem.setVisible(false);
							doneItem.setVisible(false);
							break;
						case 1:
							actionBar.setTitle(R.string.title_network_tricks);
							actionBar.setSubtitle(R.string.subtitle_network_tricks);

							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 2:
							actionBar.setTitle(R.string.title_network_meet);
							actionBar.setSubtitle(null);

							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 3:
							actionBar.setTitle(R.string.title_network_recommendation);
							actionBar.setSubtitle(null);

							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(true);
							break;
						case 4:
							actionBar.setTitle(R.string.title_be_friendly);
							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(true);
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
/*			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			Editor e = prefs.edit();
			e.putBoolean(NetworkActivity.CONTACTS_RATED, true);
			e.commit();
			
			this.finish();
*/			
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
