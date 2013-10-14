package edu.northwestern.cbits.intellicare.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
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
		
		int currentLevel = this.getIntent().getIntExtra(LessonsActivity.LESSON_LEVEL, 0);
		
		String readKey = LessonsActivity.LESSON_READ_PREFIX + currentLevel;
		
		if (prefs.getBoolean(readKey, false) == false)
		{
			Editor e = prefs.edit();
			e.putBoolean(LessonsActivity.LESSON_READ_PREFIX + currentLevel, true);
			e.commit();
	
			Intent intent = new Intent(this, MessageRatingActivity.class);
			this.startActivity(intent);
		}
	}
}
