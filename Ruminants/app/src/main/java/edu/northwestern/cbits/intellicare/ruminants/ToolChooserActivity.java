package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Gwen on 3/14/14.
 */

public class ToolChooserActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_chooser);

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

    tools.add(new Tool(this.getString(R.string.tool_worry_practice), this.getString(R.string.desc_worry_practice), R.drawable.clock_checklist_dark, new Intent(this, WorryPracticeActivity.class)));
    tools.add(new Tool(this.getString(R.string.survey_wizard_title), this.getString(R.string.desc_survey_wizard), R.drawable.clock_checklist_dark, new Intent(this, WizardOneActivity.class)));
    tools.add(new Tool(this.getResources().getString(R.string.tool_didactic_content), this.getString(R.string.desc_didactic_content), R.drawable.clock_question_dark, new Intent(this, DidacticActivity.class)));

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

   /* public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        return true;
    } */
    protected void onPause()
    {
        super.onPause();
    }

}
