package edu.northwestern.cbits.intellicate.ruminants;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}


public class MainActivity extends PortraitActivity
{
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

    private static final String APP_ID = "62e48583d6763b21b5ccf7186bd44089"; /* need to gen different app id? */

    @SuppressLint("SetJavaScriptEnabled")
    protected void onResume()
    {
        super.onResume();

        CrashManager.register(this, APP_ID, new CrashManagerListener()
        {
            public boolean shouldAutoUploadCrashes()
            {
                return true;
            }
        });

        final MainActivity me = this;

        ArrayList<Tool> tools = new ArrayList<Tool>();

        tools.add(new Tool(this.getString(R.string.tool_worry_practice), this.getString(R.string.desc_worry_practice), R.drawable.clock_log_dark, new Intent(this, WorryPracticeActivity.class)));
        tools.add(new Tool(this.getString(R.string.tool_didactic_content), this.getString(R.string.desc_didactic_content), R.drawable.clock_question_dark, new Intent(this, DidacticActivity.class)));
        tools.add(new Tool(this.getString(R.string.tool_survey_wizard), this.getString(R.string.desc_survey_wizard), R.drawable.clock_diary_dark, new Intent(this, SurveyWizardActivity.class)));
        tools.add(new Tool(this.getString(R.string.tool_recall_wizard), this.getString(R.string.desc_recall_wizard), R.drawable.clock_question_dark, new Intent(this, RecallWizardActivity.class)));
        tools.add(new Tool(this.getString(R.string.tool_replay_intro), this.getString(R.string.desc_replay_intro), R.drawable.clock_youtube_dark, new Intent(this, ScreenSlideActivity.class)));

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

        toolsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int which, long id)
            {
                Tool t = adapter.getItem(which);

                if (t.launchIntent != null)
                    me.startActivity(t.launchIntent);
            }
        });

        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("launched_home_activity", payload);
    }

    protected void onPause()
    {
        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("closed_home_activity", payload);

        super.onPause();
    }

};

