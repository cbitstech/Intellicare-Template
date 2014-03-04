package edu.northwestern.cbits.intellicare.ruminants;

import java.util.HashMap;

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

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SurveyWizardActivity extends ConsentedActivity
{
    private static final String SELECTED_RADIO_RUM = "selected_radio_rum";
    private static final String SELECTED_BEFORE = "selected_before";
    private static final String SELECTED_FEELING = "selected_feeling";
    private static final String SELECTED_RADIO_EMO = "selected_radio_emo";
    private static final String SELECTED_RUM_DURATION = "selected_rum_duration"; /*rumination duration*/
    private static final String SELECTED_RUM_STRATEGY = "selected_rum_strategy"; /*rumination strategy*/
    private static final String SELECTED_STRATEGY_SUCCESS = "selected_strategy_success"; /*successful strategy*/

    protected int _bedHour = -1;
    protected int _bedMinute = -1;
    protected int _sleepHour = -1;
    protected int _sleepMinute = -1;
    protected int _wakeHour = -1;
    protected int _wakeMinute = -1;
    protected int _upHour = -1;
    protected int _upMinute = -1;
    protected int _rumDuration = -1;
    protected int _wakeCount = -1;
    protected int _sleepQuality = -1;

    protected void onSaveInstanceState (Bundle outState)
    {
        super.onSaveInstanceState(outState);

        ContentValues values = new ContentValues();

        RadioGroup rumRadios = (RadioGroup) this.findViewById(R.id.radios_ruminating);
        int rumChecked = rumRadios.getCheckedRadioButtonId();

        if (rumChecked != -1)
            outState.putInt(SurveyWizardActivity.SELECTED_RADIO_RUM, rumChecked);

        if (this._rumDuration != -1)
            outState.putInt(SurveyWizardActivityActivity.SELECTED_RUM_DURATION, this._rumDuration);

        EditText beforeRum = (EditText) this.findViewById(R.id.field_before_rumination);
        EditText rumStrategy = (EditText) this.findViewById(R.id.field_rumination_strategy);
        EditText strategySucccess = (EditText) this.findViewById(R.id.field_strategy_success);

        outState.putString(SurveyWizardActivity.SELECTED_RUM_BEFORE, beforeRum.getEditableText().toString());
        outState.putString(SurveyWizardActivity.SELECTED_RUM_STRATEGY, rumStrategy.getEditableText().toString());
        outState.putString(SurveyWizardActivity.SELECTED_STRATEGY_SUCCESS, strategySucccess.getEditableText().toString());
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            savedInstanceState = new Bundle();

        this.setContentView(R.layout.activity_survey_wizard);

        this.getSupportActionBar().setTitle(R.string.survey_wizard_title);

        final SurveyWizardActivity me = this;

        final TextView durationQuestion = (TextView) this.findViewById(R.id.question_rumination_duration);

        final SeekBar rumDuration = (SeekBar) this.findViewById(R.id.field_rumination_duration);
        rumDuration.setMax(5);

        if (savedInstanceState.containsKey(SurveyWizardActivity.SELECTED_RUM_DURATION))
            rumDuration.setProgress(savedInstanceState.getInt(SurveyWizardActivity.SELECTED_RUM_DURATION));

        rumDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar bar, int position, boolean fromUser)
            {
                switch (position)
                {
                    case 0:
                        sleepLabel.setText(R.string.label_a_few_min);
                        me._rumDuration  = 0;

                        break;
                    case 1:
                        sleepLabel.setText(R.string.label_thirty_min);
                        me._rumDuration  = 5;

                        break;
                    case 2:
                        sleepLabel.setText(R.string.label_an_hour);
                        me._rumDuration  = 15;

                        break;
                    case 3:
                        sleepLabel.setText(R.string.label_a_few_hours);
                        me._rumDuration  = 30;

                        break;
                    case 4:
                        sleepLabel.setText(R.string.label_all_day);
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

        RadioGroup preEmo = (RadioGroup) this.findViewById(R.id.radios_pre_rum_feelings);

        if (savedInstanceState.containsKey(SurveyWizardActivity.SELECTED_RADIO_EMO))
            preEmo.check(savedInstanceState.getInt(SurveyWizardActivity.SELECTED_RADIO_EMO));

        RadioGroup rumination = (RadioGroup) this.findViewById(R.id.radios_ruminating);

        if (savedInstanceState.containsKey(SurveyWizardActivity.SELECTED_RADIO_RUM))
            rumination.check(savedInstanceState.getInt(SurveyWizardActivity.SELECTED_RADIO_RUM));

        EditText beforeRum = (EditText) this.findViewById(R.id.field_before_rumination);

        if (savedInstanceState.containsKey(SurveyWizardActivity.SELECTED_RUM_BEFORE))
            beforeRum.setText(savedInstanceState.getString(SurveyWizardActivity.SELECTED_RUM_BEFORE));

        EditText rumStrategy = (EditText) this.findViewById(R.id.field_rumination_strategy);
        if (savedInstanceState.containsKey(SurveyWizardActivity.SELECTED_RUM_STRATEGY))
            rumStrategy.setText(savedInstanceState.getString(SurveyWizardActivity.SELECTED_RUM_BEFORE));

        EditText strategySucccess = (EditText) this.findViewById(R.id.field_strategy_success);
        if (savedInstanceState.containsKey(SurveyWizardActivity.SELECTED_RUM_BEFORE))
            strategySucccess.setText(savedInstanceState.getString(SurveyWizardActivity.SELECTED_STRATEGY_SUCCESS));
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_save_survey_wizard, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_save)
        {
            ContentValues values = new ContentValues();
            HashMap<String, Object> payload = new HashMap<String, Object>();

            RadioGroup rumRadios = (RadioGroup) this.findViewById(R.id.radios_ruminating);
            int rumChecked = rumRadios.getCheckedRadioButtonId();

            if (rumChecked == -1)
            {
                Toast.makeText(this, R.string.message_complete_rum, Toast.LENGTH_SHORT).show();
                return true;
            }

            values.put(RuminantContentProvider.SURVEY_RUM, (rumChecked == R.id.rum_yes));

            payload.put("ruminated", (rumChecked == R.id.rum_yes));

            if (this._rumDuration == -1)
            {
                Toast.makeText(this, R.string.message_complete_rum_duration, Toast.LENGTH_SHORT).show();
                return true;
            }

            /*need to add checks to make sure all of the fields were filled in, and the second radio group */

            values.put(RuminantContentProvider.SURVEY_RUM_DELAY, this._rumDuration);

            EditText beforeRum = (EditText) this.findViewById(R.id.field_before_rumination);

            values.put(RuminantContentProvider.SURVEY_BEFORE_RUM, beforeRum.getEditableText().toString());

            payload.put("before_rumination_response", beforeRum);

            EditText rumStrategy = (EditText) this.findViewById(R.id.field_rumination_strategy);

            values.put(RuminantContentProvider.SURVEY_RUM_STRATEGY, rumStrategy.getEditableText().toString());

            payload.put("rumination_strategy", rumStrategy);

            EditText strategySucccess = (EditText) this.findViewById(R.id.field_strategy_success);

            values.put(RuminantContentProvider.SURVEY_STRATEGY_SUCCESS, beforeRum.getEditableText().toString());

            payload.put("strategy_success", strategySucccess);

            values.put(RuminantContentProvider.SURVEY_TIMESTAMP, System.currentTimeMillis());

            this.getContentResolver().insert(RuminantContentProvider.SURVEY_WIZARD_URI, values);

            LogManager.getInstance(this).log("stored_survey", payload);

            Toast.makeText(this, R.string.toast_survey_wizard_recorded, Toast.LENGTH_SHORT).show();

            this.finish();
        }

        return true;
    }

    protected void onResume()
    {
        super.onResume();

        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("launched_survey_wizard", payload);
    }

    protected void onPause()
    {
        HashMap<String, Object> payload = new HashMap<String, Object>();
        LogManager.getInstance(this).log("closed_survey_wizard", payload);

        super.onPause();
    }
}
