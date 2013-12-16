package edu.northwestern.cbits.intellicare.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.SequentialPageActivity;

public class LessonActivity extends SequentialPageActivity 
{
	protected static final String UNLOCK_LEVEL = "UNLOCK_LEVEL";
	protected static final String TITLE_LIST = "TITLE_LIST";
	protected static final String URL_LIST = "URL_LIST";

	public int pagesSequence() 
	{
		Intent intent = this.getIntent();
		Uri uri = intent.getData();
		
		if (uri != null)
			return Integer.parseInt(uri.getPathSegments().get(2));
		
		return intent.getIntExtra(LessonActivity.URL_LIST, 0);
	}

	public int titlesSequence() 
	{
		Intent intent = this.getIntent();
		Uri uri = intent.getData();

		if (uri != null)
			return Integer.parseInt(uri.getPathSegments().get(1));

		return intent.getIntExtra(LessonActivity.TITLE_LIST, 0);
	}
	
	public void onSequenceComplete()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int currentLevel = this.getIntent().getIntExtra(LessonsActivity.LESSON_LEVEL, 0);
		
		if (currentLevel == 0)
			currentLevel = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);

		String readKey = LessonsActivity.LESSON_READ_PREFIX + currentLevel;
		
		if (prefs.getBoolean(readKey, false) == false)
		{
			Editor e = prefs.edit();
			e.putBoolean(LessonsActivity.LESSON_READ_PREFIX + currentLevel, true);
			e.commit();
	
			Intent ratingIntent = new Intent(this, MessageRatingActivity.class);
			this.startActivity(ratingIntent);
		}
	}

	public static Uri uriForLesson(int currentLesson) 
	{
		int titles = 0;
		int urls = 0;
		
		switch (currentLesson)
		{
			case 1:
				titles = R.array.one_titles;
				urls = R.array.one_urls;
				break;
			case 2:
				titles = R.array.two_titles;
				urls = R.array.two_urls;
				break;
			case 3:
				titles = R.array.three_titles;
				urls = R.array.three_urls;

				break;
			case 4:
				titles = R.array.four_titles;
				urls = R.array.four_urls;

				break;
			case 5:
				titles = R.array.five_titles;
				urls = R.array.five_urls;

				break;
		}

		return Uri.parse("intellicare://day-to-day/lesson/" + titles + "/" + urls);
	}
}
