package edu.northwestern.cbits.intellicare.messages;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.messages.ScheduleManager.Message;
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
		
		final TipActivity me = this;
		
		ratingView.setOnRatingChangeListener(new OnRatingChangeListener()
		{
			public void onRatingChanged(View view, int rating) 
			{
				me.setRating(rating);
				
				Toast.makeText(me, R.string.toast_rating_thanks, Toast.LENGTH_LONG).show();

				me.close();
			}
		});
	}

	private void close()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("rating", Integer.valueOf(this._rating));
		payload.put("tip", this.getIntent().getStringExtra(TipActivity.MESSAGE));
		payload.put("task", this.getIntent().getStringExtra(TipActivity.TASK));
		
		LogManager.getInstance(this).log("tip_rated", payload);

		this.finish();

		Intent intent = this.getIntent();

		Uri u = intent.getData();
		
		if (u != null)
		{
			int currentLesson = Integer.parseInt(u.getPathSegments().get(1));
			int index = Integer.parseInt(u.getPathSegments().get(2));
			
			Message msg = ScheduleManager.getInstance(this).getMessage(currentLesson, index);

			String key = "response_" + msg.lessonId + "_" + msg.index;
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			if (prefs.contains(key) == false)
			{
				Editor e = prefs.edit();
				e.putLong(key, System.currentTimeMillis());
				e.commit();
			}
		}
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
			
			if (this.getIntent().hasExtra(TipActivity.TASK))
				builder = builder.setMessage(this.getIntent().getStringExtra(TipActivity.TASK));
			else
			{
				Uri u = this.getIntent().getData();
				
				int currentLesson = Integer.parseInt(u.getPathSegments().get(1));
				int index = Integer.parseInt(u.getPathSegments().get(2));
				
				index = index - (index % 5);

				Message msg = ScheduleManager.getInstance(this).getMessage(currentLesson, index);
				
				builder = builder.setMessage(msg.message);
			}
				
			builder.create().show();
		}
		else if (item.getItemId() ==  R.id.action_done)
		{
			if (this._rating == 0)
				Toast.makeText(this, R.string.message_complete_form, Toast.LENGTH_LONG).show();
			else
				this.close();
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
			this.getSupportActionBar().setTitle(this.getString(R.string.app_name));
	}

	public static Uri uriForTip(Message message) 
	{
		String uriString = "intellicare://day-to-day/tip/" + message.lessonId + "/" + message.index;

		return Uri.parse(uriString);
	}
}
