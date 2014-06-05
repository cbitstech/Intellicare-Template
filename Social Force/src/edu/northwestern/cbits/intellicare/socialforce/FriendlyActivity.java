package edu.northwestern.cbits.intellicare.socialforce;

import edu.northwestern.cbits.intellicare.SequentialPageActivity;

public class FriendlyActivity extends SequentialPageActivity 
{
	
	public int pagesSequence() 
	{
		return R.array.friendly_urls;
	}

	public int titlesSequence() 
	{
		return R.array.friendly_titles;
	}
}
