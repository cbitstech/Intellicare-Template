package edu.northwestern.cbits.intellicare.ruminants;

/**
 * Created by Gwen on 3/14/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HelpScheduleHelper  extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        HelpScheduleManager manager = HelpScheduleManager.getInstance(context);

        manager.updateSchedule();
    }
}

