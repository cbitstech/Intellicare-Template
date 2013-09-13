package edu.northwestern.cbits.intellicare.messages;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.SequentialPageActivity;

public class LessonPagesActivity extends SequentialPageActivity 
{
	protected static final String UNLOCK_LEVEL = "UNLOCK_LEVEL";
	protected static final String TITLE_LIST = "TITLE_LIST";
	protected static final String URL_LIST = "URL_LIST";

	public int pagesSequence() 
	{
		return this.getIntent().getIntExtra(LessonPagesActivity.URL_LIST, 0);
	}

	public int titlesSequence() 
	{
		return this.getIntent().getIntExtra(LessonPagesActivity.TITLE_LIST, 0);
	}
	
	public void onSequenceComplete()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int newLevel = this.getIntent().getIntExtra(LessonPagesActivity.UNLOCK_LEVEL, 0);
		int currentLevel = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);

		if (newLevel > currentLevel)
		{
			Editor e = prefs.edit();
			e.putInt(LessonsActivity.LESSON_LEVEL, newLevel);
			e.commit();
		}
	}
}
