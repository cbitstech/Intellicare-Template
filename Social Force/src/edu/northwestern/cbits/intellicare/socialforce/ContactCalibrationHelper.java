package edu.northwestern.cbits.intellicare.socialforce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;

public class ContactCalibrationHelper 
{
	private static SharedPreferences _cachedPrefs = null;
	private static long _lastContactFetch = 0;
	private static List<ContactRecord> _cachedRecords = null;
	
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
		if (ContactCalibrationHelper._cachedPrefs == null)
			ContactCalibrationHelper._cachedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		
		Editor e = ContactCalibrationHelper._cachedPrefs.edit();
		e.putInt("contact_calibration_" + key + "_level", level);
		e.commit();
	}

    public static List<ContactRecord> fetchContactRecords(Context context)
    {
    	long now = System.currentTimeMillis();
    	
    	if (now - ContactCalibrationHelper._lastContactFetch < 60000)
    		return ContactCalibrationHelper._cachedRecords ;
    	
    	ContactCalibrationHelper._lastContactFetch = now;
    	
    	ArrayList<ContactRecord> contacts = new ArrayList<ContactRecord>();
    	
		Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
		
		HashMap<String, String> nameCache = new HashMap<String, String>();

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
			
			if (phoneNumber.trim().length() > 0)
			{
				if (nameCache.containsKey(phoneNumber) == false)
				{
					nameCache.put(phoneNumber, "");

					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
					Cursor contactsCursor = context.getContentResolver().query(uri, null, null, null, null);

					while (contactsCursor.moveToNext())
					{
						nameCache.put(phoneNumber, contactsCursor.getString(contactsCursor.getColumnIndex(CommonDataKinds.Identity.DISPLAY_NAME)));
					}

					contactsCursor.close();
				}

				if (found == false && nameCache.get(phoneNumber).length() > 0)
				{

					ContactRecord contact = new ContactRecord();
					contact.name = nameCache.get(phoneNumber);
					contact.number = phoneNumber;
					
					contact.key = contact.name;
					
					if ("".equals(contact.key))
						contact.key = contact.number;
					
					contact.level = ContactCalibrationHelper.getLevel(context, contact.key);
	
					contacts.add(contact);
				}
				
			}
		}
		
		c.close();
		
		c = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "date");

		while (c.moveToNext())
		{
			String numberName = c.getString(c.getColumnIndex("person"));
			String phoneNumber = PhoneNumberUtils.formatNumber(c.getString(c.getColumnIndex("address")));

			if (numberName == null)
				numberName = phoneNumber;

			boolean found = false;

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
			
			if (nameCache.containsKey(phoneNumber) == false)
			{
				nameCache.put(phoneNumber, "");

				Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
				Cursor contactsCursor = context.getContentResolver().query(uri, null, null, null, null);

				while (contactsCursor.moveToNext())
				{
					nameCache.put(phoneNumber, contactsCursor.getString(contactsCursor.getColumnIndex(CommonDataKinds.Identity.DISPLAY_NAME)));
				}

				contactsCursor.close();
			}

			if (found == false && nameCache.get(phoneNumber).length() > 0)
			{
				ContactRecord contact = new ContactRecord();
				contact.name = nameCache.get(phoneNumber);
				contact.number = phoneNumber;
				
				contact.key = contact.name;
				
				if ("".equals(contact.key))
					contact.key = contact.number;
				
				contact.level = ContactCalibrationHelper.getLevel(context, contact.key);

				contacts.add(contact);
			}
		}

		c.close();

		c = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date");

		while (c.moveToNext())
		{
			String numberName = c.getString(c.getColumnIndex("person"));
			String phoneNumber = PhoneNumberUtils.formatNumber(c.getString(c.getColumnIndex("address")));

			if (numberName == null)
				numberName = phoneNumber;

			// TODO: Pull out of code (above as well) as function...
			
			boolean found = false;

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
			

			if (nameCache.containsKey(phoneNumber) == false)
			{
				nameCache.put(phoneNumber, "");

				Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
				Cursor contactsCursor = context.getContentResolver().query(uri, null, null, null, null);

				while (contactsCursor.moveToNext())
				{
					nameCache.put(phoneNumber, contactsCursor.getString(contactsCursor.getColumnIndex(CommonDataKinds.Identity.DISPLAY_NAME)));
				}

				contactsCursor.close();
			}

			if (found == false && nameCache.get(phoneNumber).length() > 0)
			{
				ContactRecord contact = new ContactRecord();
				contact.name = nameCache.get(phoneNumber);
				contact.number = phoneNumber;
				
				contact.key = contact.name;
				
				if ("".equals(contact.key))
					contact.key = contact.number;
				
				contact.level = ContactCalibrationHelper.getLevel(context, contact.key);

				contacts.add(contact);
			}
		}

		c.close();
		
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
		
		ContactCalibrationHelper._cachedRecords = normalizedContacts;
		
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