package edu.northwestern.cbits.intellicare.moveme;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.getSupportActionBar().setSubtitle("lIst NExT ExeRCISe HeRE");

        final MainActivity me = this;
        
        Button dashboard = (Button) this.findViewById(R.id.button_dashboard);
        dashboard.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent intent = new Intent(me, DashboardActivity.class);
				me.startActivity(intent);
			}
        });

        Button inspire = (Button) this.findViewById(R.id.button_inspire);
        inspire.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent intent = new Intent(me, InspireActivity.class);
				me.startActivity(intent);
			}
        });

        Button boost = (Button) this.findViewById(R.id.button_boost);
        boost.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent intent = new Intent(me, TimerActivity.class);
				me.startActivity(intent);
			}
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	int itemId = item.getItemId();
    	
		if (itemId == R.id.action_settings)
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));

        return super.onOptionsItemSelected(item);
    }
}
