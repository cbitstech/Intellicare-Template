    package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.activities.ReviewActivity;
import edu.northwestern.cbits.intellicare.mantra.activities.SharedUrlActivity;
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
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
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
    	 // get an Android wake-lock
    	 Log.d(CN+".onReceive", "entered; context = " + context.toString() + ": intent = " + intent.toString());
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();

         // Put here YOUR code.
         Log.d(CN+".onReceive","alarm firing!");
//         Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

         // get time bounds for notification
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         int startHour = sp.getInt(ScheduleManager.REMINDER_START_HOUR, ScheduleManager.DEFAULT_HOUR);
         int startMinute = sp.getInt(ScheduleManager.REMINDER_START_MINUTE, ScheduleManager.DEFAULT_MINUTE);
         int endHour = sp.getInt(ScheduleManager.REMINDER_END_HOUR, ScheduleManager.DEFAULT_HOUR);
         int endMinute = sp.getInt(ScheduleManager.REMINDER_END_MINUTE, ScheduleManager.DEFAULT_MINUTE);
         Calendar currentCalendarInstance = Calendar.getInstance();
         Date currentTime = currentCalendarInstance.getTime();
         int h = currentCalendarInstance.get(Calendar.HOUR_OF_DAY);
         int m = currentCalendarInstance.get(Calendar.MINUTE);
         Log.d(CN+".onReceive", "h = " + h + "; m = " + m + "; startHour = " + startHour + "; startMinute = " + startMinute + "; endHour = " + endHour + "; endMinute = " + endMinute);
         
         // beginning-of-day notification 
         if		 (h == startHour && m == startMinute) {
        	 Log.d(CN+".onReceive", "at h = " + h + ", m = " + m + ", MAKE STARTING NOTIFICATION");
        	 
        	 // put the user's list of mantra boards in the notification
        	 FocusBoardCursor mantraItemCursor = FocusBoardManager.get(context).queryFocusBoards();
        	 ArrayList<String> al = new ArrayList<String>();
        	 while(mantraItemCursor.moveToNext()) {
        		 al.add(
        				 FocusBoardManager.get(context).getFocusBoard(
        						 mantraItemCursor.getLong(
        								 mantraItemCursor.getColumnIndex("_id")
								 )
						 ).getMantra()
				 );
        	 }
        	 // destinationless notification
        	 makeNotification(
        			 context, 
        			 context.getString(R.string.notification_start_day), 
        			 R.drawable.abc_ic_go, 
        			 al.toArray(new String[al.size()]), 
        			 null,
        			 0);
         }
         // end-of-day notification
         else if (h == endHour && m == endMinute) {
        	 Log.d(CN+".onReceive", "at h = " + h + ", m = " + m + ", MAKE ENDING NOTIFICATION");
        	 // notification destination: Review activity
        	 makeNotification(
        			 context, 
        			 context.getString(R.string.notification_end_day), 
        			 R.drawable.abc_ic_go, 
        			 null,
        			 new Intent(context, ReviewActivity.class),
        			 1);
         }
         
         // if it's time to scan for an image
         if (m % IMAGE_SCAN_RATE_MINUTES == 0) {
        	 Log.d(CN+".onReceive", "at h = " + h + ", m = " + m + ", SCAN FOR IMAGES");
        	 Toast.makeText(context, "Mantra is scanning for new images...", Toast.LENGTH_SHORT).show();
        	 
        	 // list files in the camera folder
        	 File d = new File(Paths.CAMERA_IMAGES);
        	 File[] cameraImages = d.listFiles();
        	 ArrayList<File> filesAddedSinceLastCheck = new ArrayList<File>();
        	 for(File f : cameraImages) {
        		 if(f.lastModified() >= currentTime.getTime()) {
        			 filesAddedSinceLastCheck.add(f);
        		 }
        	 }
        	 
        	 // if any files have modification date after 5 minutes ago, then prompt the user to select one and apply it to a mantra
        	 if(filesAddedSinceLastCheck.size() > 0) {
        		 Intent sua = new Intent(context, SharedUrlActivity.class);
        		 sua.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, true);
        		 context.startActivity(sua);
        	 }
         }
         
         wl.release();
         Log.d(CN+".onReceive","exiting");
     }

    /**
     * Creates and displays a notification in the notification menu.
     * src: http://www.vogella.com/tutorials/AndroidNotifications/article.html
     * @param context
     * @param message
     * @param iconId
     * @param messageList If null, then default-sized notification, else, large notification (like GMail's).
     */
	public static void makeNotification(Context context, String message, int iconId, String[] messageList, Intent onClickIntent, int notificationId) {
		// setup the notification intent
		 Intent intent1 = new Intent(context, NotificationAlarm.class);
		 PendingIntent pi = PendingIntent.getActivity(context, 0, intent1, 0);
		 NotificationCompat.Builder n = null;
		 String appName = context.getString(R.string.app_name);
		 
		 // if the caller specifies an onClickIntent (destination intent when the notification is tapped), then apply it, else don't.
		 if(onClickIntent != null) {
			 n = new NotificationCompat.Builder(context)
			 	.setContentTitle(appName)
			 	.setContentText(message)
			 	.setSmallIcon(R.drawable.abc_ic_go)
			 	.setContentIntent(pi)
			 	.setAutoCancel(true)
			 	.setContentIntent(PendingIntent.getActivity(context, 0, onClickIntent, 0))
			 	;
		 }
		 else {
			 n = new NotificationCompat.Builder(context)
			 	.setContentTitle(appName)
			 	.setContentText(message)
			 	.setSmallIcon(R.drawable.abc_ic_go)
			 	.setContentIntent(pi)
			 	.setAutoCancel(true)
			 	;
		 }
		 
		 // if large notification desired...
		 // src: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
		 if(messageList != null) {
			 NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			 inboxStyle.setBigContentTitle(context.getString(R.string.app_name));
			 for(String m : messageList) {
				 inboxStyle.addLine(m);
			 }
			 n.setStyle(inboxStyle);
		 }
		 NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		 nm.notify(appName, notificationId, n.build());
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