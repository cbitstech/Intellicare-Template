package edu.northwestern.cbits.intellicare.socialforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.getSupportActionBar().setSubtitle("lIst NExT ConTACT EvEnT HeRE");
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false) == false)
        {
	        Intent introIntent = new Intent(this, IntroActivity.class);
	        this.startActivity(introIntent);
        }
        else if (prefs.getBoolean(RatingActivity.CONTACTS_RATED, false) == false)
        {
	        Intent introIntent = new Intent(this, RatingActivity.class);
	        this.startActivity(introIntent);
        }
        
        final MainActivity me = this;
        
        View network = this.findViewById(R.id.network_visualization);
        
        network.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle("SeLeCt sUppORTeR");
				
				List<ContactRecord> contacts = ContactCalibrationHelper.fetchContactRecords(me);
				
				final List<ContactRecord> positives = new ArrayList<ContactRecord>();
				
				for (ContactRecord record : contacts)
				{
					if (record.level >= 0 && record.level < 3)
						positives.add(record);
				}
				
				Collections.sort(positives, new Comparator<ContactRecord>()
				{
					public int compare(ContactRecord one, ContactRecord two) 
					{
						if (one.level < two.level)
							return -1;
						else if (one.level > two.level)
							return 1;
						
						return one.name.compareTo(two.name);
					}
				});

				String[] names = new String[positives.size()];
				
				for (int i = 0; i < positives.size(); i++)
				{
					ContactRecord record = positives.get(i);
					
					names[i] = record.name;
				}
				
				builder.setItems(names, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						Intent intent = new Intent(me, ScheduleActivity.class);
						intent.putExtra(ScheduleActivity.CONTACT_KEY, positives.get(which).key);
						
						me.startActivity(intent);
					}
				});
				
				builder.create().show();
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
//			Intent settingsIntent = new Intent(this, SettingsActivity.class);
//			this.startActivity(settingsIntent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));

        return super.onOptionsItemSelected(item);
    }
}
