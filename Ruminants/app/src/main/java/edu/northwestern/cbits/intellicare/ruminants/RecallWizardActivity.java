package edu.northwestern.cbits.intellicare.ruminants;

import java.util.HashMap;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class RecallWizardActivity extends Activity
{
/*    private static final String SELECTED_RADIO_HELPFUL = "selected_radio_helpful";
    private static final String SELECTED_RADIO_BETTER = "selected_radio_better";
    private static final String SELECTED_DIFFERENT_STRATEGY = "selected_different_strategy";

    protected void onSaveInstanceState (Bundle outState)
    {
        super.onSaveInstanceState(outState);

        ContentValues values = new ContentValues();

        RadioGroup helpfulRadios = (RadioGroup) this.findViewById(R.id.radios_rum_helpful);
        int helpfulChecked = helpfulRadios.getCheckedRadioButtonId();

        if (helpfulChecked != -1)
            outState.putInt(RecallWizardActivity.SELECTED_RADIO_HELPFUL, helpfulChecked);

        RadioGroup betterRadios = (RadioGroup) this.findViewById(R.id.radios_feel_better);
        int betterChecked = betterRadios.getCheckedRadioButtonId();

        if (betterChecked != -1)
            outState.putInt(RecallWizardActivity.SELECTED_RADIO_BETTER, betterChecked);

        EditText differentStrategy = (EditText) this.findViewById(R.id.field_different_strategy);

        outState.putString(RecallWizardActivity.SELECTED_DIFFERENT_STRATEGY, differentStrategy.getEditableText().toString());

    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            savedInstanceState = new Bundle();

        this.setContentView(R.layout.activity_recall_wizard);

        this.getSupportActionBar().setTitle(R.string.recall_wizard_title);

        final RecallWizardActivity me = this;

        RadioGroup helpfulRadios = (RadioGroup) this.findViewById(R.id.radios_rum_helpful);

        if (savedInstanceState.containsKey(RecallWizardActivity.SELECTED_RADIO_HELPFUL))
            helpfulRadios.check(savedInstanceState.getInt(RecallWizardActivity.SELECTED_RADIO_HELPFUL));

        RadioGroup betterRadios = (RadioGroup) this.findViewById(R.id.radios_feel_better);

        if (savedInstanceState.containsKey(RecallWizardActivity.SELECTED_RADIO_BETTER))
            betterRadios.check(savedInstanceState.getInt(RecallWizardActivity.SELECTED_RADIO_BETTER));

        EditText differentStrategy = (EditText) this.findViewById(R.id.field_different_strategy);
        if (savedInstanceState.containsKey(RecallWizardActivity.SELECTED_DIFFERENT_STRATEGY))
            comments.setText(savedInstanceState.getString(RecallWizardActivity.SELECTED_RUM_BEFORE));
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_save_recall_wizard, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_save)
        {
            ContentValues values = new ContentValues();
            HashMap<String, Object> payload = new HashMap<String, Object>();

            RadioGroup helpfulRadios = (RadioGroup) this.findViewById(R.id.radios_rum_helpful);
            int helpfulChecked = helpfulRadios.getCheckedRadioButtonId();

            if (helpfulChecked == -1)
            {
                Toast.makeText(this, R.string.message_complete_helpful, Toast.LENGTH_SHORT).show();
                return true;
            }

            values.put(RuminantContentProvider.RECALL_HELPFUL, (helpfulChecked == R.id.rum_yes));

            payload.put("helpful", (helpfulChecked == R.id.help_yes));

            RadioGroup betterRadios = (RadioGroup) this.findViewById(R.id.radios_feel_better);
            int betterChecked = betterRadios.getCheckedRadioButtonId();

            if (betterChecked == -1)
            {
                Toast.makeText(this, R.string.message_complete_better, Toast.LENGTH_SHORT).show();
                return true;
            }

            values.put(RuminantContentProvider.RECALL_BETTER, (betterChecked == R.id.better_yes));

            payload.put("better", (helpfulChecked == R.id.better_yes));


            -*need to add checks to make sure strategy field was filled in? *-

            EditText differentStrategy = (EditText) this.findViewById(R.id.field_different_strategy);

            values.put(RuminantContentProvider.RECALL_DIFFERENT_STRATEGY, differentStrategy.getEditableText().toString());

            payload.put("different_strategy_response", differentStrategy);

            values.put(RuminantContentProvider.RECALL_TIMESTAMP, System.currentTimeMillis());

            this.getContentResolver().insert(RuminantContentProvider.RECALL_WIZARD_URI, values);

            LogManager.getInstance(this).log("stored_recall", payload);

            Toast.makeText(this, R.string.toast_recall_wizard_recorded, Toast.LENGTH_SHORT).show();

            this.finish();
        }

        return true;
    }

    protected void onResume()
    {
        super.onResume();

        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("launched_recall_wizard", payload);
    }

    protected void onPause()
    {
        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("closed_recall_wizard", payload);

        super.onPause();
    }

    */
}
