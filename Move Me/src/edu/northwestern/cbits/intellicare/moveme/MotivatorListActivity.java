package edu.northwestern.cbits.intellicare.moveme;

import edu.northwestern.cbits.intellicare.SequentialPageActivity;

public class MotivatorListActivity extends SequentialPageActivity 
{
	public int pagesSequence() 
	{
		return R.array.array_motivators_urls;
	}

	public int titlesSequence() 
	{
		return R.array.array_motivators_titles;
	}
}
