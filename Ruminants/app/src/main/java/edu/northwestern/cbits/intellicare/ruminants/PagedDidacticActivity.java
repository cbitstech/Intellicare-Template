package edu.northwestern.cbits.intellicare.ruminants;

import java.security.SecureRandom;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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
public class PagedDidacticActivity extends ConsentedActivity 
{
	private String[] _content = new String[0];
	private Menu _menu = null;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);

    	this.setContentView(R.layout.activity_paged_intro);
    	
    	this.getSupportActionBar().setSubtitle(R.string.app_name);
    	
    	this._content = PagedDidacticActivity.chooseRandomContentSet(this);
    	
    	final PagedDidacticActivity me = this;
    	
    	final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);

        final ImageView background = (ImageView) this.findViewById(R.id.background_image);

    	pager.setAdapter(new PagerAdapter()
    	{
			public int getCount() 
			{
				return me._content.length;
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
		        
		        TextView content = (TextView) view.findViewById(R.id.content_text);

		        content.setText(me._content[position]);

                int[] chickens = {R.drawable.rumination_7, R.drawable.specific_1, R.drawable.specific_4, R.drawable.specific_11};

                Random rand = new Random();
                int chicken = rand.nextInt(4);

                background.setImageResource(chickens[chicken]);
				
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
        
        this._menu  = menu;
        
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
                Intent mainIntent = new Intent(this, MainActivity.class);
                this.startActivity(mainIntent);

            	return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    public static String[] chooseRandomContentSet(Context context)
    {
        String[] one = context.getResources().getStringArray(R.array.didactic_content1);
        String[] two = context.getResources().getStringArray(R.array.didactic_content2);
        String[] three = context.getResources().getStringArray(R.array.didactic_content3);
        String[] four = context.getResources().getStringArray(R.array.didactic_content4);
        String[] five = context.getResources().getStringArray(R.array.didactic_content5);
        String[] six = context.getResources().getStringArray(R.array.didactic_content6);
        String[] seven = context.getResources().getStringArray(R.array.didactic_content7);
        String[] eight = context.getResources().getStringArray(R.array.didactic_content8);
        String[] nine = context.getResources().getStringArray(R.array.didactic_content9);

        String[][] didacticContentSet = { one, two, three, four, five, six, seven, eight, nine };
    	
        SecureRandom random = new SecureRandom();

        return didacticContentSet[random.nextInt(didacticContentSet.length)];
    }
}
