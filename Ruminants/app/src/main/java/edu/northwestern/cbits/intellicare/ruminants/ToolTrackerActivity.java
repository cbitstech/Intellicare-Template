package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Gwen on 3/20/14.
 */
public class ToolTrackerActivity extends Activity {

    public static long LAST_LOG_TIME = 0;
    public static long LAST_WPT_TIME = 0;
    public static long LAST_DIDACTIC_TIME = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_tracker);
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }


    public static class toolLog {
        public String use;
        public String prompt;
        public int icon;
        public Intent launchIntent;

        public toolLog(String use, String prompt, int icon, Intent launchIntent) {
            this.use = use;
            this.prompt = prompt;
            this.icon = icon;
            this.launchIntent = launchIntent;
        }
    }

    public void getLastTime()
    {
        final ToolTrackerActivity me = this;

        Cursor c = me.getContentResolver().query(RuminantsContentProvider.LOG_USE_URI, null, null, null, RuminantsContentProvider.LOG_USE_ID + " DESC LIMIT 1");
        Cursor k = me.getContentResolver().query(RuminantsContentProvider.WPT_USE_URI, null, null, null, RuminantsContentProvider.WPT_USE_ID + " DESC LIMIT 1");
        Cursor z = me.getContentResolver().query(RuminantsContentProvider.DIDACTIC_USE_URI, null, null, null, RuminantsContentProvider.DIDACTIC_USE_ID + " DESC LIMIT 1");

       LAST_LOG_TIME = c.getLong(c.getColumnIndex(RuminantsContentProvider.LOG_USE_TIMESTAMP));
       LAST_WPT_TIME = k.getLong(c.getColumnIndex(RuminantsContentProvider.WPT_USE_TIMESTAMP));
       LAST_DIDACTIC_TIME = z.getLong(c.getColumnIndex(RuminantsContentProvider.DIDACTIC_TIMESTAMP));
    }

    public static String practicePrompt(Context context) {

        long now = System.currentTimeMillis();

        if (LAST_WPT_TIME < now - 1209600000){
            String worryPracticePrompt = context.getResources().getStringArray(R.array.practice_prompt)[0];
            return worryPracticePrompt;
        }

        else {
            String worryPracticePrompt = context.getResources().getStringArray(R.array.practice_prompt)[1];
            return worryPracticePrompt;
        }

    }

    public static String wizardOnePrompt(Context context) {

        long now = System.currentTimeMillis();

        if (LAST_LOG_TIME < (now - 1209600000)) {
            String logPrompt = context.getResources().getStringArray(R.array.log_prompt)[0];
            return logPrompt;
        }
        else {
            String logPrompt = context.getResources().getStringArray(R.array.log_prompt)[1];
            return logPrompt;
        }
    }


    public static String didacticPrompt(Context context) {

        long now = System.currentTimeMillis();

        if (LAST_DIDACTIC_TIME < now - 1209600000) {
            String didacticToolPrompt = context.getResources().getStringArray(R.array.didactic_prompt)[0];
            return didacticToolPrompt;
        }

        else {
            String didacticToolPrompt = context.getResources().getStringArray(R.array.didactic_prompt)[1];
            return didacticToolPrompt;
        }
    }


    protected void onResume() {
        super.onResume();

        final ToolTrackerActivity me = this;

        ArrayList<toolLog> toolLogs = new ArrayList<toolLog>();

        toolLogs.add(new toolLog(this.getString(R.string.wpt_use) + " " + RuminantsContentProvider.WPT_COUNT, practicePrompt(this), R.drawable.ic_action_alarms, new Intent(this, WorryPracticeActivity.class)));
        toolLogs.add(new toolLog(this.getResources().getString(R.string.worry_log_use) + " " + RuminantsContentProvider.LOG_COUNT, wizardOnePrompt(this), R.drawable.ic_action_keyboard, new Intent(this, RuminationLogActivity.class)));
        toolLogs.add(new toolLog(this.getString(R.string.didactic_content_use) + " " + RuminantsContentProvider.DIDACTIC_COUNT, didacticPrompt(this), R.drawable.ic_action_slideshow, new Intent(this, PagedDidacticActivity.class)));

        ListView toolList = (ListView) this.findViewById(R.id.tool_use_log);

        final ArrayAdapter<toolLog> adapter = new ArrayAdapter<toolLog>(this, R.layout.row_use_log, toolLogs) {
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.row_use_log, parent, false);
                }

                TextView use = (TextView) convertView.findViewById(R.id.tool_use);
                TextView prompt = (TextView) convertView.findViewById(R.id.tool_prompt);

                final toolLog t = this.getItem(position);

                use.setText(t.use);
                prompt.setText(t.prompt);

                /*
                ImageView icon = (ImageView) convertView.findViewById(R.id.tool_icon);

                if (t.icon != 0)
                    icon.setImageDrawable(me.getResources().getDrawable(t.icon)); */

                return convertView;
            }
        };

        toolList.setAdapter(adapter);

        toolList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                toolLog t = adapter.getItem(which);

                if (t.launchIntent != null)
                    me.startActivity(t.launchIntent);
            }
        });

    };

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_activity_tool_tracker, menu);

        return true;
    }

    // strategies, play/pause in action bar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause()
    {
        super.onPause();
    }

}

