package edu.northwestern.cbits.intellicare.mantra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MediaScannerServiceReceiver extends BroadcastReceiver {
	public static final String CN = "MediaScannerServiceReceiver";
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.d(CN+".onReceive", "entered; arg1 = " + arg1);
		
		
	}

}
