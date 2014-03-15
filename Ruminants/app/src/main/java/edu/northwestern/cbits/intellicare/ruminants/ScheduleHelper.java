package edu.northwestern.cbits.intellicare.ruminants;

/**
 * Created by Gwen on 3/14/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleHelper extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        ScheduleManager manager = ScheduleManager.getInstance(context);

        Log.e("CAR", "SHOULD FIRE!");

        manager.updateSchedule();
    }
}

