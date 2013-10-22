package edu.northwestern.cbits.intellicare.dailyfeats;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

/**
 * Created by Gabe on 9/19/13.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private SharedPreferences prefs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /*  get current reminder times,
        **  and instantiate new Time Picker.
         */

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int hour = prefs.getInt(AppConstants.reminderHourKey, AppConstants.defaultReminderHour);
        int minutes = prefs.getInt(AppConstants.reminderMinutesKey, AppConstants.defaultReminderMinutes);

        return new TimePickerDialog(getActivity(), this, hour, minutes,
                DateFormat.is24HourFormat(getActivity()));
    }

    /*  Store selected times in preferences
     */
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d("Prefs:Update", "Updating Reminder Time To (h,m): "+String.valueOf(hourOfDay)+":"+String.valueOf(minute));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(AppConstants.reminderHourKey, hourOfDay);
        editor.putInt(AppConstants.reminderMinutesKey, minute);
        editor.commit();
    }
}