package edu.northwestern.cbits.intellicare.ruminants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.logging.LogManager;

public class MainActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

    protected void onResume()
    {
        super.onResume();

/*
        CrashManager.register(this, APP_ID, new CrashManagerListener()
        {
            public boolean shouldAutoUploadCrashes()
            {
                return true;
            }
        });
*/
        final MainActivity me = this;

      Intent introIntent = new Intent(this, IntroActivity.class).putExtra("skipCheck", true);

       ArrayList<Tool> tools = new ArrayList<Tool>();

       tools.add(new Tool(this.getString(R.string.tool_worry_practice), this.getString(R.string.desc_worry_practice), R.drawable.clock_log_dark, new Intent(this, WorryPracticeActivity.class)));
       tools.add(new Tool(this.getResources().getString(R.string.tool_didactic_content), this.getString(R.string.desc_didactic_content), R.drawable.clock_log_dark, new Intent(this, DidacticActivity.class)));
       // tools.add(new Tool(this.getString(R.string.tool_survey_wizard), this.getString(R.string.desc_survey_wizard), R.drawable.clock_diary_dark, new Intent(this, SurveyWizardActivity.class)));
       tools.add(new Tool(this.getResources().getString(R.string.tool_replay_intro), this.getString(R.string.desc_replay_intro), R.drawable.clock_tips, introIntent));

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

    public boolean onOptionsItemSelected(MenuItem item) {
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
    }

    protected void onPause()
    {
        super.onPause();
    }

}
