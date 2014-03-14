package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Notification;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Created by Gwen on 3/14/14.
 */
public class HelpScheduleManager {

    public HelpScheduleManager(Context context)
    {
        this._context  = context.getApplicationContext();

        AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);

        Intent broadcast = new Intent(this._context, HelpScheduleHelper.class);
        PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000, pi);
    }

    public static HelpScheduleManager getInstance(Context context)
    {
        if (HelpScheduleManager._instance == null)
        {
            HelpScheduleManager._instance = new HelpScheduleManager(context.getApplicationContext());
            HelpScheduleManager._instance.updateSchedule();
        }

        return HelpScheduleManager._instance;
    }

    //from last profile, get number of times per day they want help
    public int helpFrequency = context.getContentResolver().query(RuminantsContentProvider.PROFILE_URI, help_frequency, where, whereArgs, sortOrder);

    public void updateSchedule()
    {
        public void setNoteFreq() {

            final boolean noteOne = true;
            boolean noteTwo = false;
            boolean noteThree = false;

            if (helpFrequency == 1) {

                noteTwo = false;
                noteThree = false;

            } else if (helpFrequency == 2) {

                noteTwo = true;
                noteThree = false;

            } else {
                noteTwo = true;
                noteThree = true;
            }
        }

        public void setNotetimes(boolean noteTwo, boolean noteThree){

            // static final int noteOneTime = 8amtoday;
            int noteTwoTime = Integer.parseInt(null);
            int noteThreeTime = Integer.parseInt(null);

            if (noteTwo == true) {
                //noteTwotime = 3pmtoday
            }
            else {
                return int noteTwoTime;
            }

            if (noteThree == true) {
                //noteThreeTime = 8pm today;
            }
            else {
                return int noteThreeTime;
            };

    }

}
