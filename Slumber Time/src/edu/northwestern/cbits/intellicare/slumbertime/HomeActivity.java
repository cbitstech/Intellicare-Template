package edu.northwestern.cbits.intellicare.slumbertime;

import android.os.Bundle;
import android.view.Menu;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class HomeActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_home);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
}
