package edu.northwestern.cbits.intellicare.moveme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class DashboardActivity extends ConsentedActivity 
{
    protected static final String WEEKLY_GOAL = "setting_weekly_goal";

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_dashboard);
        
        ActionBar actionBar = this.getSupportActionBar();
        
        actionBar.setTitle(R.string.title_dashboard);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_dashboard, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if (item.getItemId() == R.id.action_log)
    	{
    		Intent intent = new Intent(this, LogActivity.class);
    		
    		this.startActivity(intent);
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
}
