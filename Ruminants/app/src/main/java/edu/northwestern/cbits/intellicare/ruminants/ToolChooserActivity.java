package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
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

/**
 * Created by Gwen on 3/14/14.
 */

public class ToolChooserActivity extends Activity {
/*

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_chooser);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ScheduleManager.getInstance(this.getApplicationContext());
    }

    private class Tool {
        public String name;
        public String description;
        public int icon;
        public Intent launchIntent;

        public Tool(String name, String description, int icon, Intent launchIntent) {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.launchIntent = launchIntent;
        }
    }

    protected void onResume() {
        super.onResume();

    final ToolChooserActivity me = this;

    ArrayList<Tool> tools = new ArrayList<Tool>();



    ListView toolsList = (ListView) this.findViewById(R.id.rum_tools);

    final ArrayAdapter<Tool> adapter = new ArrayAdapter<Tool>(this, R.layout.row_tool_chooser, tools)
    {
        public View getView (int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.row_tool_chooser, parent, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.rum_tool_name);
            TextView desc = (TextView) convertView.findViewById(R.id.rum_tool_description);

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
        this.getMenuInflater().inflate(R.menu.menu_activity_tool_tracker, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onPause()
    {
        super.onPause();
    }
*/
}
