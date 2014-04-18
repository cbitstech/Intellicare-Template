package edu.northwestern.cbits.intellicare.socialforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;

public class ContactCalibrationHelper 
{
	private static Map<String, String> _cache = new HashMap<String, String>();
	private static SharedPreferences _cachedPrefs = null;
	
	public static String getGroup(Context context, String key, boolean isPhone) 
	{
		if (key == null)
			return null;
		
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		String group = ContactCalibrationHelper._cachedPrefs.getString("contact_calibration_" + key + "_group", null);
		
		if (group != null)
			return group;

		if (isPhone)
		{
			String newKey = ContactCalibrationHelper._cache.get(key);
			
			if (newKey == null)
			{
				String numbersOnly = key.replaceAll("[^\\d]", "");
				
				if (numbersOnly.length() == 10)
					numbersOnly = "1" + numbersOnly;
				else if (numbersOnly.length() == 11)
					numbersOnly = numbersOnly.substring(1);
				
				newKey = PhoneNumberUtils.formatNumber(numbersOnly);
				
				ContactCalibrationHelper._cache.put(key, newKey);
			}
			
			key = newKey;
		}
		
		return ContactCalibrationHelper._cachedPrefs.getString("contact_calibration_" + key + "_group", null);
	}

	public static void setGroup(Context context, String key, String group)
	{
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putString("contact_calibration_" + key + "_group", group);
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
				
				String key = contact.name;
				
				boolean isPhone = false;
				
				if ("".equals(key))
				{
					key = contact.number;
					isPhone = true;
				}
				
				String group = ContactCalibrationHelper.getGroup(context, key, isPhone);
				
				if (group != null)
					contact.group = group;
				
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
}