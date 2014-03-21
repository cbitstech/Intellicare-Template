package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.logging.LogManager;

/**
 * Created by Gwen on 3/12/14.
 */
public class ProfileActivity extends Activity {

    private static final String SELECTED_RADIO_RUMINATING_LATELY = "ruminating_lately";
    private static final String SELECTED_HELP_FREQUENCY = "help_frequency";
    private static final String SELECTED_ISSUES = "commonly_encountered_issues";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            savedInstanceState = new Bundle();

        this.setContentView(R.layout.activity_profile);

       // this.getSupportActionBar().setTitle(R.string.profile_title);

        final ProfileActivity me = this;

        RadioGroup rumLately = (RadioGroup) this.findViewById(R.id.radios_ruminating_lately);
        if (savedInstanceState.containsKey(ProfileActivity.SELECTED_RADIO_RUMINATING_LATELY))
            rumLately.check(savedInstanceState.getInt(ProfileActivity.SELECTED_RADIO_RUMINATING_LATELY));

        EditText issues = (EditText) this.findViewById(R.id.field_rumination_content);
        if (savedInstanceState.containsKey(ProfileActivity.SELECTED_ISSUES))
            issues.setText(savedInstanceState.getString(ProfileActivity.SELECTED_ISSUES));

        // ?? for an integer ok?
        EditText helpFrequency = (EditText) this.findViewById(R.id.editText_help_frequency);
        if (savedInstanceState.containsKey(ProfileActivity.SELECTED_HELP_FREQUENCY))
            helpFrequency.setText(savedInstanceState.getString(ProfileActivity.SELECTED_HELP_FREQUENCY));

    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_save_profile, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_profile_save)
        {
/*            RadioGroup rumLately = (RadioGroup) this.findViewById(R.id.radios_ruminating_lately);
            int rumChecked = rumLately.getCheckedRadioButtonId();

            if (rumChecked != -1)
                outState.putInt(ProfileActivity.SELECTED_RADIO_RUMINATING_LATELY, rumChecked);


            EditText issues = (EditText) this.findViewById(R.id.field_rumination_content);
            outState.putString(ProfileActivity.SELECTED_ISSUES, issues.getEditableText().toString());
*/
            ContentValues values = new ContentValues();
            HashMap<String, Object> payload = new HashMap<String, Object>();

            RadioGroup rumLately = (RadioGroup) this.findViewById(R.id.radios_ruminating_lately);
            int rumChecked = rumLately.getCheckedRadioButtonId();

            if (rumChecked == -1)
            {
                Toast.makeText(this, R.string.message_complete_rum, Toast.LENGTH_SHORT).show();
                return true;
            }

            values.put(RuminantsContentProvider.PROFILE_RUMINATING_LATELY, (rumChecked == R.id.rum_yes));

            payload.put("ruminating_lately", (rumChecked == R.id.rum_yes));

            EditText help = (EditText) this.findViewById(R.id.editText_help_frequency);

            int n = Integer.parseInt(help.getEditableText().toString());

            values.put(RuminantsContentProvider.PROFILE_HELP_FREQUENCY, n);
            payload.put("help_frequency", SELECTED_HELP_FREQUENCY);

            // toast required if not completed
            EditText concerns = (EditText) this.findViewById(R.id.field_rumination_content);
            values.put(RuminantsContentProvider.PROFILE_RUMINATION_CONCERNS, concerns.getEditableText().toString());
            payload.put("concerns", concerns);

            values.put(RuminantsContentProvider.PROFILE_TIMESTAMP, System.currentTimeMillis());

            this.getContentResolver().insert(RuminantsContentProvider.PROFILE_URI, values);

            //LogManager.getInstance(this).log("stored_profile", payload);

            Toast.makeText(this, R.string.toast_profile_recorded, Toast.LENGTH_SHORT).show();

            this.finish();

            Intent launchIntent = new Intent(this, MainActivity.class);
            this.startActivity(launchIntent);
        }


        return true;
    }
}
