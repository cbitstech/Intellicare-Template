package edu.northwestern.cbits.intellicare.ruminants;

import java.util.HashMap;
import java.util.logging.LogManager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class WizardOneActivity extends Activity
{

    private static final String SELECTED_RADIO_ISRUM = "selected_radio_isrum";
    private static final String SELECTED_RUM_TRIGGER = "selected_trigger";
    private static final String SELECTED_RADIO_EMO = "selected_radio_emo";
    private static final String SELECTED_RUM_DURATION = "selected_rum_duration";
    private static final String SELECTED_RUM_ATTEMPTED_STOP_METHODS = "attempted_stop_methods";
    //private static final String SELECTED_RUM_CONTENT = "rumination_content";
    private static final String SELECTED_TERMINATION_CAUSE = "rumination_termination_cause";

    protected int _rumDuration = -1;

    protected void onSaveInstanceState (Bundle outState)
    {
        super.onSaveInstanceState(outState);

        ContentValues values = new ContentValues();

        RadioGroup rumRadios = (RadioGroup) this.findViewById(R.id.radios_ruminating);
        int rumChecked = rumRadios.getCheckedRadioButtonId();

        if (rumChecked != -1)
            outState.putInt(WizardOneActivity.SELECTED_RADIO_ISRUM, rumChecked);

        RadioGroup emoRadios = (RadioGroup) this.findViewById(R.id.radios_emotion);
        int emoChecked = emoRadios.getCheckedRadioButtonId();

        if (emoChecked != -1)
            outState.putInt(WizardOneActivity.SELECTED_RADIO_EMO, rumChecked);

        if (this._rumDuration != -1)
            outState.putInt(WizardOneActivity.SELECTED_RUM_DURATION, this._rumDuration);

        EditText trigger = (EditText) this.findViewById(R.id.field_trigger);
        EditText attemptedStopMethods = (EditText) this.findViewById(R.id.field_attempted_stop_methods);
        EditText terminationCause = (EditText) this.findViewById(R.id.field_termination_cause);

        outState.putString(WizardOneActivity.SELECTED_RUM_TRIGGER, trigger.getEditableText().toString());
        outState.putString(WizardOneActivity.SELECTED_RUM_ATTEMPTED_STOP_METHODS, attemptedStopMethods.getEditableText().toString());
        outState.putString(WizardOneActivity.SELECTED_TERMINATION_CAUSE, terminationCause.getEditableText().toString());
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            savedInstanceState = new Bundle();

        this.setContentView(R.layout.activity_wizard_one);

        //this.getSupportActionBar().setTitle(R.string.wizard_one_title);

        final WizardOneActivity me = this;

        final TextView duration = (TextView) this.findViewById(R.id.label_rumination_duration);

        final SeekBar rumDuration = (SeekBar) this.findViewById(R.id.field_rumination_duration);
        rumDuration.setMax(4);

        if (savedInstanceState.containsKey(WizardOneActivity.SELECTED_RUM_DURATION))
            rumDuration.setProgress(savedInstanceState.getInt(WizardOneActivity.SELECTED_RUM_DURATION));

        rumDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar bar, int position, boolean fromUser)
            {
                switch (position)
                {
                    case 0:
                        duration.setText(R.string.label_a_few_min);
                        me._rumDuration  = 0;

                        break;
                    case 1:
                        duration.setText(R.string.label_thirty_min);
                        me._rumDuration  = 5;

                        break;
                    case 2:
                        duration.setText(R.string.label_an_hour);
                        me._rumDuration  = 15;

                        break;
                    case 3:
                        duration.setText(R.string.label_a_few_hours);
                        me._rumDuration  = 30;

                        break;
                    case 4:
                        duration.setText(R.string.label_all_day);
                        me._rumDuration  = 60;

                        break;
                }

            }

            public void onStartTrackingTouch(SeekBar arg0)
            {

            }

            public void onStopTrackingTouch(SeekBar arg0)
            {

            }
        });

        RadioGroup preEmo = (RadioGroup) this.findViewById(R.id.radios_emotion);
        if (savedInstanceState.containsKey(WizardOneActivity.SELECTED_RADIO_EMO))
            preEmo.check(savedInstanceState.getInt(WizardOneActivity.SELECTED_RADIO_EMO));

        RadioGroup rumination = (RadioGroup) this.findViewById(R.id.radios_ruminating);
        if (savedInstanceState.containsKey(WizardOneActivity.SELECTED_RADIO_ISRUM))
            rumination.check(savedInstanceState.getInt(WizardOneActivity.SELECTED_RADIO_ISRUM));

        EditText trigger = (EditText) this.findViewById(R.id.field_trigger);
        if (savedInstanceState.containsKey(WizardOneActivity.SELECTED_RUM_TRIGGER))
           trigger.setText(savedInstanceState.getString(WizardOneActivity.SELECTED_RUM_TRIGGER));

        EditText stopMethods = (EditText) this.findViewById(R.id.field_attempted_stop_methods);
        if (savedInstanceState.containsKey(WizardOneActivity.SELECTED_RUM_ATTEMPTED_STOP_METHODS))
            stopMethods.setText(savedInstanceState.getString(WizardOneActivity.SELECTED_RUM_ATTEMPTED_STOP_METHODS));

        EditText terminationCause = (EditText) this.findViewById(R.id.field_termination_cause);
        if (savedInstanceState.containsKey(WizardOneActivity.SELECTED_TERMINATION_CAUSE))
            terminationCause.setText(savedInstanceState.getString(WizardOneActivity.SELECTED_TERMINATION_CAUSE));
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_save_wizard_one, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_save)
        {
            ContentValues values = new ContentValues();
            ContentValues useValues = new ContentValues();
            HashMap<String, Object> payload = new HashMap<String, Object>();

            RadioGroup rumRadios = (RadioGroup) this.findViewById(R.id.radios_ruminating);
            int rumChecked = rumRadios.getCheckedRadioButtonId();

            if (rumChecked == -1)
            {
                Toast.makeText(this, R.string.message_complete_rum, Toast.LENGTH_SHORT).show();
                return true;
            }

            values.put(RuminantsContentProvider.WIZARD_ONE_RUMINATION_ISOVER, (rumChecked == R.id.rum_yes));

            payload.put("rumination_isOver", (rumChecked == R.id.rum_yes));

            if (this._rumDuration == -1)
            {
                Toast.makeText(this, R.string.message_complete_rum_duration, Toast.LENGTH_SHORT).show();
                return true;
            }

            RadioGroup emoRadios = (RadioGroup) this.findViewById(R.id.radios_emotion);
            int emoChecked = emoRadios.getCheckedRadioButtonId();

            if (emoChecked == -1)
            {
                Toast.makeText(this, R.string.message_complete_emo, Toast.LENGTH_SHORT).show();
                return true;
            }

            // ??
            values.put(RuminantsContentProvider.WIZARD_ONE_EMOTION, (emoChecked == R.id.emotion_one));
            payload.put("emotion", (emoChecked == R.id.emotion_one));


            // toast required if not completed
            values.put(RuminantsContentProvider.WIZARD_ONE_DURATION, this._rumDuration);
            payload.put("rumination_duration", _rumDuration);

            EditText trigger = (EditText) this.findViewById(R.id.field_trigger);
            values.put(RuminantsContentProvider.WIZARD_ONE_TRIGGER, trigger.getEditableText().toString());
            payload.put("trigger", trigger);

            EditText attemptedStopMethods = (EditText) this.findViewById(R.id.field_attempted_stop_methods);
            values.put(RuminantsContentProvider.WIZARD_ONE_ATTEMPTED_STOP_METHOD, attemptedStopMethods.getEditableText().toString());
            payload.put("attempted_stop_methods", attemptedStopMethods);

            EditText terminationCause = (EditText) this.findViewById(R.id.field_termination_cause);
            values.put(RuminantsContentProvider.WIZARD_ONE_RUMINATION_TERMINATION_CAUSE, terminationCause.getEditableText().toString());
            payload.put("termination_cause", terminationCause);

            values.put(RuminantsContentProvider.WIZARD_ONE_TIMESTAMP, System.currentTimeMillis());

            useValues.put(RuminantsContentProvider.LOG_USE_TIMESTAMP, System.currentTimeMillis());

            this.getContentResolver().insert(RuminantsContentProvider.WIZARD_ONE_URI, values);
            this.getContentResolver().insert(RuminantsContentProvider.LOG_USE_URI, useValues);

            //LogManager.getInstance(this).log("stored_survey", payload);

            Toast.makeText(this, R.string.toast_survey_wizard_recorded, Toast.LENGTH_SHORT).show();

            this.finish();
        }

        return true;
    }
    /*
    protected void onResume()
    {
        super.onResume();

        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("launched_survey", payload);
    }

    protected void onPause()
    {
        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("closed_survey", payload);

        super.onPause();
    }
    */
}
