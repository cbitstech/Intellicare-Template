    package edu.northwestern.cbits.intellicare.mantra;

import java.util.Calendar;
import java.util.Date;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

    /**
     * Sets an alarm to handle a wakelock, then run some code.
     * Src: http://stackoverflow.com/questions/4459058/alarm-manager-example
     * @author mohrlab
     *
     */
    public class NotificationAlarm extends BroadcastReceiver 
    {	
    	private static final String CN = "NotificationAlarm";
		final int POLLING_RATE = 1000 * 60 * 1; 				// Millisec * Second * Minute
		final int IMAGE_SCAN_RATE_MINUTES = 5;
		private static boolean isAlreadyCalled = false;
    	
         @Override
         public void onReceive(Context context, Intent intent) 
         {   
        	 Log.d(CN+".onReceive", "entered; context = " + context.toString() + ": intent = " + intent.toString());
             PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
             PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
             wl.acquire();

             // Put here YOUR code.
             Log.d(CN+".onReceive","alarm firing! Should see a toast.");
             Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

             SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
             int startHour = sp.getInt(ScheduleManager.REMINDER_START_HOUR, ScheduleManager.DEFAULT_HOUR);
             int startMinute = sp.getInt(ScheduleManager.REMINDER_START_MINUTE, ScheduleManager.DEFAULT_MINUTE);
             int endHour = sp.getInt(ScheduleManager.REMINDER_END_HOUR, ScheduleManager.DEFAULT_HOUR);
             int endMinute = sp.getInt(ScheduleManager.REMINDER_END_MINUTE, ScheduleManager.DEFAULT_MINUTE);
             int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
             int m = Calendar.getInstance().get(Calendar.MINUTE);
             Log.d(CN+"onReceive", "h = " + h + "; m = " + m + "; startHour = " + startHour + "; startMinute = " + startMinute + "; endHour = " + endHour + "; endMinute = " + endMinute);
             
             // beginning-of-day notification 
             if		 (h == startHour && m == startMinute) {
            	 Log.d(CN+".onReceive", "at h = " + h + ", m = " + m + ", MAKE STARTING NOTIFICATION");
            	 makeNotification(context, context.getString(R.string.notification_start_day), R.drawable.abc_ic_go);
             }
             // end-of-day notification
             else if (h == endHour && m == endMinute) {
            	 Log.d(CN+".onReceive", "at h = " + h + ", m = " + m + ", MAKE ENDING NOTIFICATION");
            	 makeNotification(context, context.getString(R.string.notification_end_day), R.drawable.abc_ic_go);
             }
             
             // if it's time to scan for an image
             if (m % IMAGE_SCAN_RATE_MINUTES == 0) {
            	 Log.d(CN+".onReceive", "at h = " + h + ", m = " + m + ", SCAN FOR IMAGES");
            	 Toast.makeText(context, "Mantra is scanning for new images...", Toast.LENGTH_SHORT).show();
             }
             
             wl.release();
             Log.d(CN+".onReceive","exiting");
         }

		/**
		 * Creates and displays a notification in the notification menu.
		 * @param context
		 */
		private void makeNotification(Context context, String message, int iconId) {
			// src: http://www.vogella.com/tutorials/AndroidNotifications/article.html
			 Intent intent1 = new Intent(context, NotificationAlarm.class);
			 PendingIntent pi = PendingIntent.getActivity(context, 0, intent1, 0);
			 Notification.Builder n = new Notification.Builder(context)
			 	.setContentTitle(context.getString(R.string.app_name))
			 	.setContentText(message)
			 	.setSmallIcon(R.drawable.abc_ic_go)
			 	.setContentIntent(pi)
			 	.setAutoCancel(true)
			 	;
			 NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			 nm.notify("Mantra",0, n.build());
		}

     public void SetAlarm(Context context)
     {
    	 if(!isAlreadyCalled) {
    		 isAlreadyCalled = true;
        	 Log.d(CN+".SetAlarm","entered");
             AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
             Intent i = new Intent(context, NotificationAlarm.class);
             PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
             am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), POLLING_RATE, pi); 
        	 Log.d(CN+".SetAlarm","exiting; am = " + am.toString());
    	 }
     }

     public void CancelAlarm(Context context)
     {
         Intent intent = new Intent(context, NotificationAlarm.class);
         PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         alarmManager.cancel(sender);
     }
 }