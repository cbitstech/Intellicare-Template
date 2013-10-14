package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.views.StarRatingView;
import edu.northwestern.cbits.intellicare.views.StarRatingView.OnRatingChangeListener;

public class RatingActivity extends ConsentedActivity 
{
	private int _rating = 0;
	
	public int getRating()
	{
		return this._rating;
	}
	
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
				me._rating = rating;
				
				me.supportInvalidateOptionsMenu();
			}
		});
	}
	
	public void onSaveInstanceState(Bundle bundle) 
	{
		super.onSaveInstanceState(bundle);  

		bundle.putInt("rating", this._rating);
	}  
	
	public void onRestoreInstanceState(Bundle bundle) 
	{  
		super.onRestoreInstanceState(bundle);  
		
		if (bundle.containsKey("rating"))
		{
			this._rating = bundle.getInt("rating");
		
			StarRatingView ratingView = (StarRatingView) this.findViewById(R.id.star_rating_view);
		
			ratingView.setRating(this._rating);
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_done)
		{
			if (this._rating == 0)
			{
				Toast.makeText(this, R.string.message_complete_form, Toast.LENGTH_LONG).show();
			}
			else
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("rating", Integer.valueOf(this._rating));
				
				EditText feedback = (EditText) this.findViewById(R.id.feedback_field);
				
				String feedbackText = feedback.getText().toString().trim();
				
				if (feedbackText.length() > 0)
					payload.put("feedback", feedbackText);
				
				LogManager.getInstance(this).log("app_rated", payload);

				this.finish();
			}
		}

		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_rating, menu);

		return true;
	}
}
