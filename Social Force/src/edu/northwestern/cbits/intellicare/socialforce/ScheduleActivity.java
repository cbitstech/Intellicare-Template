package edu.northwestern.cbits.intellicare.socialforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ScheduleActivity extends ConsentedActivity 
{
	private Menu _menu = null;
	
	private String _activity = null;
	private String _meetingActivity = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_schedule);
        
        final ScheduleActivity me = this;
        
        final ActionBar actionBar = this.getSupportActionBar();
        
		actionBar.setTitle("tOdO: Schedule ActiviTY");
        
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
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
				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = null;

				final String[] levels = me.getResources().getStringArray(R.array.contact_roles);
				
				switch (position)
				{
					case 0:
						WebView webViewZero = new WebView(container.getContext());
						
						webViewZero.loadUrl("file:///android_asset/www/schedule_0.html");

						view = webViewZero;
						break;
					case 1:
						WebView webViewOne = new WebView(container.getContext());
						
						webViewOne.loadUrl("file:///android_asset/www/schedule_1.html");
						webViewOne.setTag("1");

						view = webViewOne;

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
							nextItem.setVisible(true);
							backItem.setVisible(false);
							doneItem.setVisible(false);
							break;
						case 1:
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
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
        final ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
    	
        final ScheduleActivity me = this;
        
    	if (item.getItemId() == R.id.action_next)
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("What CoULD you MaKE TimE For THis WeeK?");
    		
    		final String[] activities = this.getResources().getStringArray(R.array.positive_activities);
    		
    		builder.setItems(activities, new DialogInterface.OnClickListener() 
    		{
				public void onClick(DialogInterface dialog, final int which) 
				{
					me._activity = activities[which];
					
					if (which == 0)
						pager.setCurrentItem(1);
					else
					{
			    		AlertDialog.Builder builder = new AlertDialog.Builder(me);
			    		builder.setTitle("What WOUlD YOu LIKe To DO?");
			    		
			    		final String[] meetUpActivities = me.getActivities();
			    		
			    		builder.setItems(activities, new DialogInterface.OnClickListener()
			    		{
							public void onClick(DialogInterface dialog, int which) 
							{
								if (which == meetUpActivities.length - 1) // Other
								{
									// TODO: Show Other Dialog
								}
								else
								{
									me._meetingActivity = meetUpActivities[which];
									
									pager.setCurrentItem(1);
								}							
							}
			    		});
			    		
			    		builder.create().show();
					}
				}
			});
    		
    		builder.create().show();
    		
    		return true;
    	}
    	else if (item.getItemId() == R.id.action_back)
    	{
    		pager.setCurrentItem(pager.getCurrentItem() - 1);
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }

	protected String[] getActivities() {
		// TODO Auto-generated method stub
		return null;
	}
}
