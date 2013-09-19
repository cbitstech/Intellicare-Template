package edu.northwestern.cbits.intellicare.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.RatingActivity;
import edu.northwestern.cbits.intellicare.SequentialPageActivity;

public class LessonActivity extends SequentialPageActivity 
{
	protected static final String UNLOCK_LEVEL = "UNLOCK_LEVEL";
	protected static final String TITLE_LIST = "TITLE_LIST";
	protected static final String URL_LIST = "URL_LIST";

	public int pagesSequence() 
	{
		return this.getIntent().getIntExtra(LessonActivity.URL_LIST, 0);
	}

	public int titlesSequence() 
	{
		return this.getIntent().getIntExtra(LessonActivity.TITLE_LIST, 0);
	}
	
	public void onSequenceComplete()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int currentLevel = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);
		
		Editor e = prefs.edit();
		e.putBoolean(LessonsActivity.LESSON_READ_PREFIX + currentLevel, true);
		e.commit();

		Intent intent = new Intent(this, RatingActivity.class);
		this.startActivity(intent);
	}
}
