package edu.northwestern.cbits.intellicare;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import edu.northwestern.cbits.ic_template.R;

public abstract class SequentialPageActivity extends ConsentedActivity 
{
	public abstract int pagesSequence();
	public abstract int titlesSequence();
	
	private String[] _urls = null;
	private String[] _titles = null;
	private String[] _subtitles = null;
	
	private Menu _menu = null;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_sequential);

		this._urls = this.getResources().getStringArray(this.pagesSequence());
		this._titles = this.getResources().getStringArray(this.titlesSequence());
		
		if (this.subtitlesSequence() != -1)
			this._subtitles = this.getResources().getStringArray(this.subtitlesSequence());

        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        pager.setOffscreenPageLimit(0);
        
        final SequentialPageActivity me = this;
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return me._urls.length;
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
				WebView webView = new WebView(container.getContext());

				webView.loadUrl(me._urls[position]);
				
				webView.setTag("" + position);

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
				me.getActionBar().setTitle(me._titles[page]);
				
				if (me._subtitles != null)
					me.getActionBar().setSubtitle(me._subtitles[page]);
				else
					me.getActionBar().setSubtitle(me.getString(R.string.subtitle_pages, (page + 1), me._titles.length));
				
				if (me._menu != null)
				{
					MenuItem nextItem = me._menu.findItem(R.id.action_next);
					MenuItem backItem = me._menu.findItem(R.id.action_back);
					MenuItem doneItem = me._menu.findItem(R.id.action_done);

					if (page == me._urls.length - 1)
					{
						nextItem.setVisible(false);
						backItem.setVisible(true);
						doneItem.setVisible(true);
					}
					else if (page == 0)
					{
						nextItem.setVisible(true);
						backItem.setVisible(false);
						doneItem.setVisible(false);
					}
					else
					{
						nextItem.setVisible(true);
						backItem.setVisible(true);
						doneItem.setVisible(false);
					}
				}
			}
		});
		
		this.getActionBar().setTitle(this._titles[0]);
		
		if (this._subtitles != null)
			this.getActionBar().setSubtitle(this._subtitles[0]);
		else
			this.getActionBar().setSubtitle(this.getString(R.string.subtitle_pages, 1, this._titles.length));
		

	}

	public void onSequenceComplete()
	{
		// Placeholder for sequences with no completion action.
	}
	
	public int subtitlesSequence() 
	{
		return -1;
	}

    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_sequential, menu);
        
        this._menu = menu;

		MenuItem nextItem = this._menu.findItem(R.id.action_next);
		MenuItem backItem = this._menu.findItem(R.id.action_back);
		MenuItem doneItem = this._menu.findItem(R.id.action_done);

		if (this._urls.length == 1)
		{
			nextItem.setVisible(false);
			backItem.setVisible(false);
			doneItem.setVisible(true);
		}
		else
		{
			nextItem.setVisible(true);
			backItem.setVisible(false);
			doneItem.setVisible(false);
		}

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
			this.onSequenceComplete();
    		this.finish();
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}

