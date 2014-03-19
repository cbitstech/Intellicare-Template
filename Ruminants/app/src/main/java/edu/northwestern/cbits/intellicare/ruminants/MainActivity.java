package edu.northwestern.cbits.intellicare.ruminants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

public class MainActivity extends Activity
{
    public static final String RUNBEFORE = "runBefore";

    protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        ScheduleManager.getInstance(this.getApplicationContext());
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

   /* protected void onCreate() {
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
    } */

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

       Intent introIntent = new Intent(this, IntroActivity.class).putExtra("skipCheck", true);

       ArrayList<Tool> tools = new ArrayList<Tool>();

       tools.add(new Tool(this.getString(R.string.tool_rumination_log), this.getString(R.string.desc_rumination_log), R.drawable.clock_checklist_dark, new Intent(this, RuminationLogActivity.class)));
       tools.add(new Tool(this.getResources().getString(R.string.tool_chooser_name), this.getString(R.string.desc_tool_chooser), R.drawable.clock_question_dark, new Intent(this, ToolChooserActivity.class)));
       tools.add(new Tool(this.getString(R.string.profile_wizard), this.getString(R.string.desc_profile_wizard), R.drawable.clock_checklist_dark, new Intent(this, ProfileActivity.class)));
       tools.add(new Tool(this.getResources().getString(R.string.tool_replay_intro), this.getString(R.string.desc_replay_intro), R.drawable.clock_question_dark, introIntent));

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

                TextView name = (TextView) convertView.findViewById(R.id.label_tool_name);
                TextView desc = (TextView) convertView.findViewById(R.id.label_tool_description);

                Tool t = this.getItem(position);

                name.setText(t.name);
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
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        return true;
    }

   /* public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_reset:
                // launches intro from start for testing by clearing flag
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(IntroActivity.RUNBEFORE);
                editor.commit();

                return true;

        }

        return super.onOptionsItemSelected(item);
    } */

    protected void onPause()
    {
        super.onPause();
    }

}


