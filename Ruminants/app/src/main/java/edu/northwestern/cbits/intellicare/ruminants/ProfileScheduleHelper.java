package edu.northwestern.cbits.intellicare.ruminants;

/**
 * Created by Gwen on 3/14/14.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ProfileScheduleHelper extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        ProfileScheduleManager manager = ProfileScheduleManager.getInstance(context);

        manager.updateSchedule();
    }
}