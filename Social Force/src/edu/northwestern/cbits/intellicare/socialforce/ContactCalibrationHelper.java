package edu.northwestern.cbits.intellicare.socialforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class ContactCalibrationHelper 
{
	private static SharedPreferences _cachedPrefs = null;
	
	public static int getLevel(Context context, String key) 
	{
		if (key == null)
			return -1;
		
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		return ContactCalibrationHelper._cachedPrefs.getInt("contact_calibration_" + key + "_level", -1);
	}

	public static void setLevel(Context context, String key, int level)
	{
		Log.e("SF", "SETTING " + key + " LEVEL " + level);
		
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putInt("contact_calibration_" + key + "_level", level);
		e.commit();
	}

    public static List<ContactRecord> fetchContactRecords(Context context)
    {
    	ArrayList<ContactRecord> contacts = new ArrayList<ContactRecord>();
    	
		Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

		while (c.moveToNext())
		{
			String numberName = c.getString(c.getColumnIndex(Calls.CACHED_NAME));
			String phoneNumber = PhoneNumberUtils.formatNumber(c.getString(c.getColumnIndex(Calls.NUMBER)));

			boolean found = false;
			
			if (numberName == null)
				numberName = "";
			
			for (ContactRecord contact : contacts)
			{
				if (contact.number.endsWith(phoneNumber) || phoneNumber.endsWith(contact.number))
				{
					String largerNumber = contact.number;
					
					if (phoneNumber.length() > largerNumber.length())
						largerNumber = phoneNumber;
					
					contact.number = largerNumber;
					
					found = true;
					contact.count += 1;
					
					if ("".equals(numberName) == false && "".equals(contact.name))
						contact.name = numberName;
				}
			}
			
			if (found == false)
			{
				ContactRecord contact = new ContactRecord();
				contact.name = numberName;
				contact.number = phoneNumber;
				
				contact.key = contact.name;
				
				if ("".equals(contact.key))
					contact.key = contact.number;
				
				contact.level = ContactCalibrationHelper.getLevel(context, contact.key);

				Log.e("SF", "CONTACT " + contact.key + " LEVEL = " + contact.level);

				contacts.add(contact);
			}
		}
		
		Collections.sort(contacts);

		ArrayList<ContactRecord> normalizedContacts = new ArrayList<ContactRecord>();
		
		for (ContactRecord contact : contacts)
		{
			if ("".equals(contact.name) == false)
			{
				boolean found = false;
				
				for (ContactRecord normalized : normalizedContacts)
				{
					if (contact.name.equals(normalized.name))
					{
						found = true;
						
						normalized.count += contact.count;
					}
				}
				
				if (found == false)
					normalizedContacts.add(contact);
			}
			else
				normalizedContacts.add(contact);
		}
		
		Collections.sort(normalizedContacts);
		
    	return normalizedContacts;
    }

	public static boolean isCompanion(Context context, ContactRecord contact) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		return ContactCalibrationHelper._cachedPrefs.getBoolean("contact_calibration_" + contact.key + "_companion", false);
	}

	public static void setCompanion(Context context, ContactRecord contact, boolean isCompanion) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putBoolean("contact_calibration_" + contact.key + "_companion", isCompanion);
		e.commit();
	}

	public static boolean isAdvice(Context context, ContactRecord contact) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		return ContactCalibrationHelper._cachedPrefs.getBoolean("contact_calibration_" + contact.key + "_advice", false);
	}

	public static void setAdvice(Context context, ContactRecord contact, boolean isAdvice) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putBoolean("contact_calibration_" + contact.key + "_advice", isAdvice);
		e.commit();
	}

	public static boolean isEmotional(Context context, ContactRecord contact) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		return ContactCalibrationHelper._cachedPrefs.getBoolean("contact_calibration_" + contact.key + "_emotional", false);
	}

	public static void setEmotional(Context context, ContactRecord contact, boolean isEmotional) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putBoolean("contact_calibration_" + contact.key + "_emotional", isEmotional);
		e.commit();
	}

	public static boolean isPractical(Context context, ContactRecord contact) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		return ContactCalibrationHelper._cachedPrefs.getBoolean("contact_calibration_" + contact.key + "_practical", false);
	}

	public static void setPractical(Context context, ContactRecord contact, boolean isPractical) 
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putBoolean("contact_calibration_" + contact.key + "_practical", isPractical);
		e.commit();
	}
}