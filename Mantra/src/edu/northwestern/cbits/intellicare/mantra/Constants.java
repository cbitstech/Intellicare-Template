package edu.northwestern.cbits.intellicare.mantra;

public final class Constants {
	
	public static final String BROADCAST_ACTION = "edu.northwestern.cbits.intellicare.mantra.BROADCAST";
	public static final String BROADCAST_STATUS = "edu.northwestern.cbits.intellicare.mantra.STATUS";

	/* Notification Alarm constants */
	public static final String REMINDER_START_HOUR = "start_hour";
	public static final String REMINDER_START_MINUTE = "start_minute";
	public static final String REMINDER_END_HOUR = "end_hour";
	public static final String REMINDER_END_MINUTE = "end_minute";
	private static final String LAST_NOTIFICATION = "last_notification";
	public static final int DEFAULT_HOUR = 18;
	public static final int DEFAULT_MINUTE = 0;
	public final static int IMAGE_SCAN_RATE_MINUTES = 1;
	public final static int ALARM_POLLING_RATE_MILLIS = 1000 * 60; //  * IMAGE_SCAN_RATE_MINUTES; 				// Millisec * Second * Minute

}