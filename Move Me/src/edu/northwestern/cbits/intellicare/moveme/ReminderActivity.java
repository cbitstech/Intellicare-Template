package edu.northwestern.cbits.intellicare.moveme;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ReminderActivity extends ConsentedActivity 
{
	private static final String TIME_OF_DAY = "time_of_day";

	private static final int MORNING = 0;
	protected static final int AFTERNOON = 1;
	protected static final int EVENING = 2;

	private Menu _menu;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_reminder);
		
		final int timeOfDay = this.getIntent().getIntExtra(ReminderActivity.TIME_OF_DAY, ReminderActivity.MORNING);

		if (timeOfDay == ReminderActivity.AFTERNOON)
			this.getSupportActionBar().setTitle(R.string.title_afternoon_reminder);
		else if (timeOfDay == ReminderActivity.EVENING)
			this.getSupportActionBar().setTitle(R.string.title_evening_reminder);
		else
			this.getSupportActionBar().setTitle(R.string.title_morning_reminder);
		
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
        final ReminderActivity me = this;
        
		PagerAdapter adapter = new PagerAdapter()
		{
			public int getCount() 
			{
				return 2;
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
				LayoutInflater inflater = LayoutInflater.from(me);

				View view = inflater.inflate(R.layout.activity_reminder_thoughts, null, false);
				
				TextView prompt = (TextView) view.findViewById(R.id.prompt_thoughts);
				ListView list = (ListView) view.findViewById(R.id.list_thoughts);

				int arrayId = -1;
						
				if (timeOfDay == ReminderActivity.AFTERNOON)
				{
					if (position == 0)
					{
						prompt.setText(R.string.reminder_afternoon_one);
						arrayId = R.array.afternoon_one;
					}
					else
					{
						prompt.setText(R.string.reminder_afternoon_two);
						arrayId = R.array.afternoon_two;
					}
				}
				else if (timeOfDay == ReminderActivity.EVENING)
				{
					if (position == 0)
					{
						prompt.setText(R.string.reminder_evening_one);
						arrayId = R.array.evening_one;
					}
					else
					{
						prompt.setText(R.string.reminder_evening_two);
						arrayId = R.array.evening_two;
					}
				}
				else // Morning
				{
					if (position == 0)
					{
						prompt.setText(R.string.reminder_morning_one);
						arrayId = R.array.morning_one;
					}
					else
					{
						prompt.setText(R.string.reminder_morning_two);
						arrayId = R.array.morning_two;
					}
				}
				
				if (arrayId != -1)
				{
					String[] items = me.getResources().getStringArray(arrayId);
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(me, android.R.layout.simple_list_item_multiple_choice, items);

					list.setAdapter(adapter);    
					list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				}

				view.setTag("" + position);
				
				container.addView(view);
				
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
				if (me._menu != null)
				{
					MenuItem nextItem = me._menu.findItem(R.id.action_next);
					MenuItem backItem = me._menu.findItem(R.id.action_back);
					MenuItem doneItem = me._menu.findItem(R.id.action_done);

					if (page == 1)
					{
						nextItem.setVisible(false);
						backItem.setVisible(true);
						doneItem.setVisible(true);
					}
					else
					{
						nextItem.setVisible(true);
						backItem.setVisible(false);
						doneItem.setVisible(false);
					}
				}
			} 
		});
		
//		this.getActionBar().setTitle(this._titles[0]);
//		
//		if (this._subtitles != null)
//			this.getActionBar().setSubtitle(this._subtitles[0]);
//		else
//			this.getActionBar().setSubtitle(this.getString(R.string.subtitle_pages, 1, this._titles.length));
	}

    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_sequential, menu);
        
        this._menu = menu;

		MenuItem nextItem = this._menu.findItem(R.id.action_next);
		MenuItem backItem = this._menu.findItem(R.id.action_back);
		MenuItem doneItem = this._menu.findItem(R.id.action_done);

		nextItem.setVisible(true);
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
    		this.finish();
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
