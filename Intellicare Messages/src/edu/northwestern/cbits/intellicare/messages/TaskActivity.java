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
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.RatingActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.messages.ScheduleManager.Message;
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
		
		final TaskActivity me = this;
		
		ratingView.setOnRatingChangeListener(new OnRatingChangeListener()
		{
			public void onRatingChanged(View view, int rating) 
			{
				me.setRating(rating);

				Toast.makeText(me, R.string.toast_rating_thanks, Toast.LENGTH_LONG).show();

				me.close();
			}
		});
		
		Intent intent = this.getIntent();
		Uri u = intent.getData();
		
		if (intent.hasExtra(ScheduleManager.MESSAGE_MESSAGE))
		{
			String message = intent.getStringExtra(ScheduleManager.MESSAGE_MESSAGE);
			String descIndex = intent.getStringExtra(ScheduleManager.MESSAGE_INDEX);

			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("message_index", descIndex);
			payload.put("message", message);
			LogManager.getInstance(this).log("notification_tapped", payload);
		}
		else if (u != null)
		{
			int currentLesson = Integer.parseInt(u.getPathSegments().get(1));
			int index = Integer.parseInt(u.getPathSegments().get(2));
			
			Message msg = ScheduleManager.getInstance(this).getMessage(currentLesson, index);

			String message = msg.message;
			String descIndex = "" + index;

			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("message_index", descIndex);
			payload.put("message", message);
			LogManager.getInstance(this).log("notification_tapped", payload);
		}
	}

	private void close()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor e = prefs.edit();
		e.putBoolean(ScheduleManager.INSTRUCTION_COMPLETED, true);
		e.commit();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("rating", Integer.valueOf(this._rating));
		payload.put("task", this.getIntent().getStringExtra(TaskActivity.MESSAGE));
		
		LogManager.getInstance(this).log("task_rated", payload);

		this.finish();
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

		Intent intent = this.getIntent();

		String message = intent.getStringExtra(TaskActivity.MESSAGE);
		String image = intent.getStringExtra(TaskActivity.IMAGE);

		Uri u = intent.getData();
		
		if (u != null)
		{
			int currentLesson = Integer.parseInt(u.getPathSegments().get(1));
			int index = Integer.parseInt(u.getPathSegments().get(2));
			
			Message msg = ScheduleManager.getInstance(this).getMessage(currentLesson, index);

			message = msg.message;
			image = msg.image;
		}
		
		this.getSupportActionBar().setTitle(R.string.task_title);
		
		this.setContent("file:///android_asset/lesson_images/" + image, message);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}

	public static Uri uriForTask(Message message) 
	{
		String uriString = "intellicare://day-to-day/task/" + message.lessonId + "/" + message.index;

		return Uri.parse(uriString);
	}
}
