package edu.northwestern.cbits.intellicare.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.RatingActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.views.StarRatingView;
import edu.northwestern.cbits.intellicare.views.StarRatingView.OnRatingChangeListener;

public class TaskActivity extends RatingActivity 
{
	public static final String MESSAGE = "MESSAGE";
	public static final String IMAGE = "IMAGE";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_task);
		
		StarRatingView ratingView = (StarRatingView) this.findViewById(R.id.star_rating_view);
		
		final RatingActivity me = this;
		
		ratingView.setOnRatingChangeListener(new OnRatingChangeListener()
		{
			public void onRatingChanged(View view, int rating) 
			{
				me.setRating(rating);
			}
		});
		
		Intent intent = this.getIntent();
		
		if (intent.hasExtra(ScheduleManager.MESSAGE_MESSAGE))
		{
			String message = intent.getStringExtra(ScheduleManager.MESSAGE_MESSAGE);
			String descIndex = intent.getStringExtra(ScheduleManager.MESSAGE_INDEX);

			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("message_index", descIndex);
			payload.put("message", message);
			LogManager.getInstance(this).log("notification_tapped", payload);
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
				if (this.getIntent().getBooleanExtra(ScheduleManager.IS_INSTRUCTION, false))
				{
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					Editor e = prefs.edit();
					e.putBoolean(ScheduleManager.INSTRUCTION_COMPLETED, true);
					e.commit();
				}

				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("rating", Integer.valueOf(this._rating));
				payload.put("task", this.getIntent().getStringExtra(TaskActivity.MESSAGE));
				
				CheckBox interruption = (CheckBox) this.findViewById(R.id.interrupt_check);
				payload.put("interruption", interruption.isChecked());
				
				LogManager.getInstance(this).log("task_rated", payload);

				this.finish();
			}
		}

		return true;
	}

	protected void onNewIntent (Intent intent)
	{
		super.onNewIntent(intent);
		
		this.setIntent(intent);
	}

	public void setContent(String imageUrl, String content)
	{
		try 
		{
			InputStream in = this.getAssets().open("www/message_content.html");
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[1024];
			int read = 0;
			
			while ((read = in.read(buffer, 0, buffer.length)) != -1)
			{
				bout.write(buffer, 0, read);
			}
			
			in.close();
			
			String contents = bout.toString();
			bout.close();
			
			contents = contents.replace("[[ CONTENT ]]", content);
			contents = contents.replace("[[ IMAGE ]]", imageUrl);
			
			WebView webview = (WebView) this.findViewById(R.id.web_view);
			webview.loadDataWithBaseURL("", contents, "text/html", "UTF-8", "");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void onResume()
	{
		super.onResume();
		
		String message = this.getIntent().getStringExtra(TaskActivity.MESSAGE);
		String image = this.getIntent().getStringExtra(TaskActivity.IMAGE);
		
		this.getSupportActionBar().setTitle(R.string.task_title);
		
		this.setContent("file:///android_asset/lesson_images/" + image, message);
	}

	public static Uri uriForMessage(ScheduleManager.Message message) 
	{
		String uriString = "intellicare://day-to-day/task/" + message.lessonId + "/" + message.index;

		return Uri.parse(uriString);
	}
}
