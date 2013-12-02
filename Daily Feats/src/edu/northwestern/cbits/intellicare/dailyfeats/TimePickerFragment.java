package edu.northwestern.cbits.intellicare.dailyfeats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

/**
 * Created by Gabe on 9/19/13.
 */

@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener 
{
	private OnDismissListener mDismiss = null;

	public TimePickerFragment()
	{
		super();
	}

	public TimePickerFragment(OnDismissListener dismiss)
	{
		super();
		
		this.mDismiss = dismiss;
	}
	
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
    	Activity activity = this.getActivity();
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        
    	int hour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
        int minutes = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

        return new TimePickerDialog(activity, this, hour, minutes, DateFormat.is24HourFormat(activity));
    }
    
    public void onDismiss(DialogInterface dialog)
    {
    	if (this.mDismiss != null)
    		this.mDismiss.onDismiss(dialog);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt(ScheduleManager.REMINDER_HOUR, hourOfDay);
        editor.putInt(ScheduleManager.REMINDER_MINUTE, minute);
        editor.commit();
    }
}