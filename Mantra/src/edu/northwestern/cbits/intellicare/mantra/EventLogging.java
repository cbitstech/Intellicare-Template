package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.content.Context;


public class EventLogging {
	
	
	/**
	 * Formats a string for event logging.
	 * @param str
	 * @return
	 */
	public static String format(String message, String methodName, String className) {
		String s = "[" + className + (methodName == null ? "" : "." + methodName) + "] " + message;
		return s;
	}
	
	
	public static void log(Context ctx, String message, String methodName, String className) {
		LogManager.getInstance(ctx).log(format(message, methodName, className), null);
	}
	
}
