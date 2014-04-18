package edu.northwestern.cbits.intellicare.socialforce;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class RatingActivity extends ConsentedActivity 
{
	public static final String CONTACTS_RATED = "contacts_rated";

	private Menu _menu = null;
	private List<ContactRecord> _contacts = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_intro);
        
        final RatingActivity me = this;
        
        final ActionBar actionBar = this.getSupportActionBar();
        
		actionBar.setTitle(me.getString(R.string.title_welcome, 1));
		actionBar.setSubtitle(R.string.subtitle_welcome);
		
		this._contacts = ContactCalibrationHelper.fetchContactRecords(this);
        
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager_content);
        
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
				LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View view = null;
				
				switch (position)
				{
					case 0:
						WebView webViewZero = new WebView(container.getContext());
						
						webViewZero.loadUrl("file:///android_asset/www/rating_0.html");

						view = webViewZero;
						break;
					case 1:
						WebView webViewOne = new WebView(container.getContext());
						
						webViewOne.loadUrl("file:///android_asset/www/rating_1.html");
						webViewOne.setTag("1");

						view = webViewOne;

						break;
					case 2:
						view = inflater.inflate(R.layout.view_contact_rater, null);
						
						if (view instanceof ListView)
						{
							ListView list = (ListView) view;
							
					    	list.setAdapter(new ArrayAdapter<ContactRecord>(me, R.layout.row_contact, me._contacts)
			    			{
			    	    		public View getView (int position, View convertView, ViewGroup parent)
			    	    		{
			    	    			if (convertView == null)
			    	    			{
			    	    				LayoutInflater inflater = LayoutInflater.from(me);
			    	    				convertView = inflater.inflate(R.layout.row_contact, parent, false);
			    	    			}
			    	    			
			    	    			TextView contactName = (TextView) convertView.findViewById(R.id.label_contact_name);
			    	    			TextView contactNumber = (TextView) convertView.findViewById(R.id.label_contact_number);
			    	    			TextView contactType = (TextView) convertView.findViewById(R.id.label_contact_type);
			    	    			
			    	    			ContactRecord contact = me._contacts.get(position);
			    	    			
			    	    			if ("".equals(contact.name) == false)
			    	    				contactName.setText(contact.name);
			    	    			else
			    	    				contactName.setText(contact.number);
			    	    			
			    	    			contactName.setText(contactName.getText() + " (" + contact.count + ")");

			    					contactNumber.setText(contact.number);

			    	    			return convertView;
			    	    		}
			    	    	});
							
						}

						break;
					case 3:
						WebView webViewThree = new WebView(container.getContext());
						
						webViewThree.loadUrl("file:///android_asset/www/rating_3.html");

						view = webViewThree;

						break;
					case 4:
						view = inflater.inflate(R.layout.view_contact_category, null);
						
						// TODO: Init rater view...

						break;
					case 5:
						view = inflater.inflate(R.layout.view_my_network, null);
						
						// TODO: Init rater view...

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
							actionBar.setTitle(R.string.title_people_rater);
							nextItem.setVisible(true);
							backItem.setVisible(false);
							doneItem.setVisible(false);
							break;
						case 1:
							actionBar.setTitle(R.string.title_people_rater);
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 2:
							actionBar.setTitle(R.string.title_people_rater_tool);
							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 3:
							actionBar.setTitle(R.string.title_people_rater);
							nextItem.setVisible(true);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 4:
							actionBar.setTitle(R.string.title_people_category_tool);
							nextItem.setVisible(false);
							backItem.setVisible(true);
							doneItem.setVisible(false);
							break;
						case 5:
							actionBar.setTitle(R.string.title_my_network);
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
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			Editor e = prefs.edit();
			e.putBoolean(RatingActivity.CONTACTS_RATED, true);
			e.commit();
			
			this.finish();
			
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
