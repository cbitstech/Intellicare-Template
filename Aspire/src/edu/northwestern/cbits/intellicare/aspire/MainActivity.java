package edu.northwestern.cbits.intellicare.aspire;

import android.os.Bundle;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
	}
}
