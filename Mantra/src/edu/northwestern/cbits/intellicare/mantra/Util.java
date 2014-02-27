package edu.northwestern.cbits.intellicare.mantra;
import android.database.Cursor;
import android.util.Log;


public class Util {

	private static final String CN = "Util";

	/**
	 * Logs the contents of a table referenced by a Cursor.
	 * @param cursor
	 */
	public static void logCursor(Cursor cursor) {
		StringBuilder sb = new StringBuilder();
		for(String cn : cursor.getColumnNames()) { sb.append("; " + cn); }
		Log.d(CN+".logCursor", "col names = " + sb.toString());
		while(cursor.moveToNext()) {
			sb.delete(0, sb.length());
			for(int j = 0; j < cursor.getColumnCount(); j++) {
				sb.append("; " + cursor.getString(j));
			}
			Log.d(CN+".logCursor", "row values = " + sb.toString());
		}
		Log.d(CN+".logCursor", "row count = " + cursor.getCount() + "; col count = " + cursor.getColumnCount());
		cursor.moveToPosition(-1);
	}
	
}
