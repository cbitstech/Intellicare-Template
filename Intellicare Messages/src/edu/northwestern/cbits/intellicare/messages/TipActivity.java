package edu.northwestern.cbits.intellicare.messages;

import java.util.HashMap;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.RatingActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.views.StarRatingView;
import edu.northwestern.cbits.intellicare.views.StarRatingView.OnRatingChangeListener;

public class TipActivity extends TaskActivity 
{
	public static final String INDEX = "INDEX";
	protected static final String TASK = "TASK";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_tip);
		
		StarRatingView ratingView = (StarRatingView) this.findViewById(R.id.star_rating_view);
		
		final RatingActivity me = this;
		
		ratingView.setOnRatingChangeListener(new OnRatingChangeListener()
		{
			public void onRatingChanged(View view, int rating) 
			{
				me.setRating(rating);
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_tip, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_task)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder = builder.setTitle(R.string.task_title);
			builder = builder.setMessage(this.getIntent().getStringExtra(TipActivity.TASK));
			
			builder.create().show();
		}
		else if (item.getItemId() ==  R.id.action_done)
		{
			if (this._rating == 0)
			{
				Toast.makeText(this, R.string.message_complete_form, Toast.LENGTH_LONG).show();
			}
			else
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("rating", Integer.valueOf(this._rating));
				payload.put("tip", this.getIntent().getStringExtra(TipActivity.MESSAGE));
				payload.put("task", this.getIntent().getStringExtra(TipActivity.TASK));
				
				CheckBox interruption = (CheckBox) this.findViewById(R.id.interrupt_check);
				payload.put("interruption", interruption.isChecked());
				
				LogManager.getInstance(this).log("tip_rated", payload);

				this.finish();
			}
		}

		return true;
	}

	public void onResume()
	{
		super.onResume();
		
		String index = this.getIntent().getStringExtra(TipActivity.INDEX);
		
		if (index != null)
			this.getSupportActionBar().setTitle(this.getString(R.string.tip_title, index));
		else
			this.getSupportActionBar().setTitle(this.getString(R.string.app_name, index));
	}
}
