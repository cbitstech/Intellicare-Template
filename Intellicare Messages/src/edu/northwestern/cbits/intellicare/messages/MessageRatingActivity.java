package edu.northwestern.cbits.intellicare.messages;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.RatingActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MessageRatingActivity extends RatingActivity 
{
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (super.onOptionsItemSelected(item))
		{
			if (this.getIntent().getBooleanExtra(ScheduleManager.IS_INSTRUCTION, false))
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				Editor e = prefs.edit();
				e.putBoolean(ScheduleManager.INSTRUCTION_COMPLETED, true);
				e.commit();
			}
			
			return true;
		}

		return false;
	}
	
	public void onResume()
	{
		super.onResume();
		
		String title = this.getString(R.string.title_rating);
		
		if (this.getIntent().hasExtra(ScheduleManager.MESSAGE_TITLE))
		{
			title = this.getIntent().getStringExtra(ScheduleManager.MESSAGE_TITLE);

			this.getSupportActionBar().setTitle(title);
		}
		
		Intent intent = this.getIntent();
		
		if (intent.hasExtra(ScheduleManager.MESSAGE_MESSAGE))
		{
			String message = intent.getStringExtra(ScheduleManager.MESSAGE_MESSAGE);
			String descIndex = intent.getStringExtra(ScheduleManager.MESSAGE_INDEX);

			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("message_index", descIndex);
			LogManager.getInstance(this).log("notification_tapped", payload);
			
			message += System.getProperty("line.separator") + System.getProperty("line.separator") + System.getProperty("line.separator");
			message += this.getString(edu.northwestern.cbits.intellicare.messages.R.string.message_confirm_text);
			
			TextView ratingText = (TextView) this.findViewById(R.id.desc_rating);
			
			ratingText.setText(message);
		}
	}
}
