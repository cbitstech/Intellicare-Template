package edu.northwestern.cbits.intellicare.ruminants;

/**
 * Created by Gwen on 3/14/14.
 */
import java.util.HashMap;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.widget.TimePicker;
//import edu.northwestern.cbits.intellicare.ConsentedActivity;
//import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SettingsActivity extends PreferenceActivity
{
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setTitle(R.string.title_settings);

        this.addPreferencesFromResource(R.layout.activity_settings);
    }


    /*
    public void onResume()
    {
        super.onResume();

        LogManager.getInstance(this).log("opened_settings", null);
    }

    public void onPause()
    {
        LogManager.getInstance(this).log("closed_settings", null);

        super.onPause();
    } */

    @SuppressWarnings("deprecation")
    public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
    {
        String key = preference.getKey();

        if (key == null)
        {

        }
        else if (key.equals("settings_reminder_time"))
        {
/*            final SettingsActivity me = this;

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener()
            {
                public void onTimeSet(TimePicker arg0, int hour, int minute)
                {
                    Editor editor = prefs.edit();

                    editor.putInt(ScheduleManager.REMINDER_HOUR, hour);
                    editor.putInt(ScheduleManager.REMINDER_MINUTE, minute);
                    editor.commit();

                    HashMap<String, Object> payload = new HashMap<String, Object>();
                    payload.put("hour", hour);
                    payload.put("minute", minute);
                    payload.put("source", "settings");

                    payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
                    //LogManager.getInstance(me).log("set_reminder_time", payload);
                }
            }, prefs.getInt(ScheduleManager.REMINDER_HOUR, 18), prefs.getInt(ProfileScheduleManager.REMINDER_MINUTE, 0), DateFormat.is24HourFormat(this));

            dialog.show();
*/
            return true;
        }
        else if (key.equals("copyright_statement"))
            ConsentedActivity.showCopyrightDialog(this);

        return super.onPreferenceTreeClick(screen, preference);
    }
}

