package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.views.StarRatingView;
import edu.northwestern.cbits.intellicare.views.StarRatingView.OnRatingChangeListener;

public class RatingActivity extends ConsentedActivity 
{
	private Menu _menu = null;
	private int _rating = 0;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_rating);
		
		this.getSupportActionBar().setTitle(R.string.title_rating);
		
		StarRatingView ratingView = (StarRatingView) this.findViewById(R.id.star_rating_view);
		
		final RatingActivity me = this;
		
		ratingView.setOnRatingChangeListener(new OnRatingChangeListener()
		{
			public void onRatingChanged(View view, int rating) 
			{
				if (me._menu != null)
					me._menu.findItem(R.id.action_done).setVisible(true);
				
				me._rating = rating;
			}
		});
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_done)
		{
			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("rating", Integer.valueOf(this._rating));
			payload.put("app_package", this.getApplicationContext().getPackageName());
			
			LogManager.getInstance(this).log("app_rated", payload);
			
			this.finish();
		}

		return true;
	}

	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_rating, menu);

		this._menu = menu;

		this._menu.findItem(R.id.action_done).setVisible(false);

		return true;
	}

}
