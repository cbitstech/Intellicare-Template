package edu.northwestern.cbits.intellicare.ruminants;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

/**
 * Created by Gwen on 2/26/14.
 */
public class PagedIntroActivity extends ConsentedActivity 
{
    private Menu _menu;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);

    	this.setContentView(R.layout.activity_paged_intro);
    	
    	this.getSupportActionBar().setSubtitle(R.string.app_name);
    	
    	final PagedIntroActivity me = this;
    	
    	final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

       final ImageView background = (ImageView) this.findViewById(R.id.background_image);

    	pager.setAdapter(new PagerAdapter()
    	{
			public int getCount() 
			{
				return 27;
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

				View view = inflater.inflate(R.layout.view_paged_content, null);

		        String[] contentValues = me.getResources().getStringArray(R.array.intro_content);

                int[] introImages = {R.drawable.greet_1, R.drawable.greet_2, R.drawable.greet_3,
                        R.drawable.intent_1, R.drawable.intent_2, R.drawable.intent_3,
                        R.drawable.intent_4, R.drawable.intent_5, R.drawable.intent_6,
                        R.drawable.rumination_1, R.drawable.rumination_2, R.drawable.rumination_3,
                        R.drawable.rumination_4, R.drawable.rumination_5, R.drawable.rumination_6,
                        R.drawable.rumination_7, R.drawable.specific_1, R.drawable.specific_2,
                        R.drawable.specific_2, R.drawable.specific_4,
                        R.drawable.specific_5, R.drawable.specific_7,
                        R.drawable.specific_9, R.drawable.specific_11,
                        R.drawable.conclusion_1, R.drawable.conclusion_2, R.drawable.conclusion_3};


                TextView content = (TextView) view.findViewById(R.id.content_text);


		        content.setText(contentValues[position]);
				background.setBackgroundResource(introImages[position]);

				view.setTag("" + position);

				container.addView(view);

				LayoutParams layout = (LayoutParams) view.getLayoutParams();
				layout.height = LayoutParams.MATCH_PARENT;
				layout.width = LayoutParams.MATCH_PARENT;
				
				view.setLayoutParams(layout);

				return view.getTag();
			}
    	});
    	
    	pager.setOnPageChangeListener(new OnPageChangeListener()
    	{
			public void onPageScrollStateChanged(int arg0) 
			{

			}

			public void onPageScrolled(int arg0, float arg1, int arg2) 
			{
				
			}

			public void onPageSelected(int position) 
			{
		        MenuItem back = me._menu.findItem(R.id.action_previous);
		        MenuItem next = me._menu.findItem(R.id.action_next);
		        MenuItem done = me._menu.findItem(R.id.action_done);

		        back.setVisible(true);
		        next.setVisible(true);
		        done.setVisible(false);

				if (position == 0)
					back.setVisible(false);
				
				if (position == pager.getAdapter().getCount() - 1)
				{
					next.setVisible(false);
					done.setVisible(true);
				}
				
		        // TODO: Pull out into strings file...
		        me.getSupportActionBar().setTitle("Page " + (position + 1) + " of " + pager.getAdapter().getCount());
			}
    	});
    	
    	// TODO: Pull out into strings file...
        me.getSupportActionBar().setTitle("Page " + 1 + " of " + pager.getAdapter().getCount());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);

        this.getMenuInflater().inflate(R.menu.menu_paged_intro, menu);
        
        this._menu = menu;
        
        MenuItem back = this._menu.findItem(R.id.action_previous);
        back.setVisible(false);

        MenuItem done = this._menu.findItem(R.id.action_done);
        done.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

    	switch (item.getItemId()) 
        {
            case R.id.action_previous:
            	pager.setCurrentItem(pager.getCurrentItem() - 1, true);

            	return true;

            case R.id.action_next:
            	pager.setCurrentItem(pager.getCurrentItem() + 1, true);

            	return true;

            case R.id.action_done:
            	this.finish();

            	return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
