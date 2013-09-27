package edu.northwestern.cbits.intellicare;

import android.support.v7.app.ActionBarActivity;

public abstract class FormQuestionActivity extends ActionBarActivity 
{
	protected abstract void setupListeners();
	
	protected void onResume()
	{
		super.onResume();
		
		this.setupListeners();
	}
}
