package edu.northwestern.cbits.intellicare.ruminants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity
{
    public static final String RUNBEFORE = "runBefore";

    protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        ScheduleManager.getInstance(this.getApplicationContext());

        // check if this is first app use
        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        int sizeStack =  am.getRunningTasks(1).get(0).numActivities;

        if (sizeStack == 2)
        {
            // if first app use have user fill out profile

            Intent launchIntent = new Intent(this, ProfileActivity.class);
            this.startActivity(launchIntent);
        }
        else
        {
            // then set RUNBEFORE to true to turn profiles over to notification manager

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(MainActivity.RUNBEFORE, true);
            editor.commit();
        }
    }

    private class Tool
    {
        public String name;
        public String description;
        public int icon;
        public Intent launchIntent;

        public Tool(String name, String description, int icon, Intent launchIntent)
        {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.launchIntent = launchIntent;
        }
    }
    @SuppressLint("SetJavaScriptEnabled")

    /*
   protected void onCreate() {
        // check if this is first app use
        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        int sizeStack =  am.getRunningTasks(1).get(0).numActivities;

        // if first app use have user fill out profile
        if (sizeStack == 2)
        {Intent launchIntent = new Intent(this, ProfileActivity.class);
            this.startActivity(launchIntent);

            return;}

        // then set RUNBEFORE to true to turn profiles over to notification manager
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(MainActivity.RUNBEFORE, true);
            editor.commit();

            return;
        }
    }

    */

    protected void onResume()
    {
        super.onResume();

/*
        CrashManager.register(this, APP_ID, new CrashManagerListener()
        {
            public bintoolean shouldAutoUploadCrashes()
            {
                return true;
            }
        });
*/
       final MainActivity me = this;

       ArrayList<Tool> tools = new ArrayList<Tool>();

       tools.add(new Tool(this.getResources().getString(R.string.tool_chooser_name), this.getString(R.string.desc_tool_chooser), R.drawable.ic_action_star, new Intent(this, ToolChooserActivity.class)));
       tools.add(new Tool(this.getString(R.string.tool_use_log), this.getString(R.string.desc_tool_chooser_log), R.drawable.ic_action_view_as_list, new Intent(this, ToolTrackerActivity.class)));
       tools.add(new Tool(this.getString(R.string.profile_wizard), this.getString(R.string.desc_profile_wizard), R.drawable.ic_action_profile, new Intent(this, ProfileActivity.class)));

        ListView toolsList = (ListView) this.findViewById(R.id.list_tools);

        final ArrayAdapter<Tool> adapter = new ArrayAdapter<Tool>(this, R.layout.row_home_tool, tools)
        {
            public View getView (int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.row_home_tool, parent, false);
                }

                TextView desc = (TextView) convertView.findViewById(R.id.label_tool_description);

                Tool t = this.getItem(position);

                desc.setText(t.description);

                ImageView icon = (ImageView) convertView.findViewById(R.id.icon_tool);

                if (t.icon != 0)
                    icon.setImageDrawable(me.getResources().getDrawable(t.icon));

                return convertView;
            }
        };

        toolsList.setAdapter(adapter);

        toolsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                Tool t = adapter.getItem(which);

                if (t.launchIntent != null)
                    me.startActivity(t.launchIntent);
            }
        });

        String[] punBank = this.getResources().getStringArray(R.array.pun_bank);

        Random random = new SecureRandom();

        TextView puns = (TextView) this.findViewById(R.id.pun);
        puns.setText(punBank[random.nextInt(punBank.length)]);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        return true;
    }

   // replay intro from menu
   public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_replay_intro:

            Intent introIntent =  new Intent(this, IntroActivity.class);
            introIntent.putExtra("skipCheck", true);
            this.startActivity(introIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause()
    {
        super.onPause();

        // TODO: Log that user exited this activity using LogManager...
    }

}


