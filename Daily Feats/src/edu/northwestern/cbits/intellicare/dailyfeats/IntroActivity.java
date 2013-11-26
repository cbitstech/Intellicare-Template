package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.DialogFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class IntroActivity extends ConsentedActivity 
{
	public static final String INTRO_SHOWN = "intro_shown";

	private int mStep = 0;
	private List<ContactRecord> mContacts = new ArrayList<ContactRecord>();
	private HashSet<String> mSelectedContacts = new HashSet<String>();
	private Toast mToast = null;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_intro);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final IntroActivity me = this;
        
        Button back = (Button) this.findViewById(R.id.back_button);
        back.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				if (me.mStep > 0)
					me.mStep -= 1;
				
				me.updateLayout();
			}
        });

        Button next = (Button) this.findViewById(R.id.next_button);
        next.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				if (me.mStep == 1 && prefs.contains(AppConstants.DEPRESSION_LEVEL) == false)
				{
					Toast.makeText(me, R.string.depression_toast, Toast.LENGTH_LONG).show();
					return;
				}
				else if (me.mStep == 4)
				{
			        DialogFragment timeFragment = new TimePickerFragment(new OnDismissListener()
			        {
						public void onDismiss(DialogInterface dialog) 
						{
							me.mStep += 1;
							me.updateLayout();
						}
			        });
			        
			        timeFragment.show(me.getSupportFragmentManager(), "timePicker");

					return;
				}
				else if (me.mStep == 6)
				{
					int size = me.mSelectedContacts.size();
					
					String toast = null;
					
					if (size == 0)
						toast = me.getString(R.string.no_supporters_selected);
					else if (size > 5)
						toast = me.getString(R.string.too_many_supporters_selected, size);
						
					if (toast != null)
					{
						if (me.mToast != null)
							me.mToast.cancel();
						
						me.mToast = Toast.makeText(me, toast, Toast.LENGTH_LONG);
						me.mToast.show();

						return;
					}
					
					JSONArray supporters = new JSONArray();
					
					for (String supporter : me.mSelectedContacts)
						supporters.put(supporter);
					
					Editor e = prefs.edit();
					e.putString(AppConstants.SUPPORTERS, supporters.toString());
					e.commit();
				}
				
				if (me.mStep < (me.getResources().getStringArray(R.array.intro_urls).length - 1))
				{
					me.mStep += 1;
					me.updateLayout();
				}
				else
				{
					Editor e = prefs.edit();
					e.putBoolean(IntroActivity.INTRO_SHOWN, true);
					e.commit();
					
					me.startActivity(new Intent(me, HomeActivity.class));
					me.finish();
				}
			}
        });
        
        RadioGroup depression = (RadioGroup) this.findViewById(R.id.depression_level);
        depression.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			public void onCheckedChanged(RadioGroup group, int id) 
			{
				Editor e = prefs.edit();
				
				switch(id)
				{
					case R.id.depression_question_0:
						e.putInt(AppConstants.DEPRESSION_LEVEL, 1);
						break;
					case R.id.depression_question_1:
						e.putInt(AppConstants.DEPRESSION_LEVEL, 2);
						break;
					case R.id.depression_question_2:
						e.putInt(AppConstants.DEPRESSION_LEVEL, 3);
						break;
					case R.id.depression_question_3:
						e.putInt(AppConstants.DEPRESSION_LEVEL, 4);
						break;
				}
				
				e.commit();
			}
        });
        
        ListView supportersList = (ListView) this.findViewById(R.id.supporters_list);
        
        this.mContacts.addAll(IntroActivity.fetchContactRecords(this));
        		
        supportersList.setAdapter(new ArrayAdapter<ContactRecord>(this, R.layout.row_contact, this.mContacts)
		{
    		public View getView (int position, View convertView, ViewGroup parent)
    		{
    			if (convertView == null)
    			{
    				LayoutInflater inflater = LayoutInflater.from(me);
    				convertView = inflater.inflate(R.layout.row_contact, parent, false);
    			}
    			
    			CheckBox contactName = (CheckBox) convertView.findViewById(R.id.contact_check);

    			final ContactRecord contact = me.mContacts.get(position);

    			contactName.setOnCheckedChangeListener(null);

    			contactName.setChecked(me.mSelectedContacts.contains(contact.name));

    			contactName.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
    			{
					public void onCheckedChanged(CompoundButton view, boolean checked) 
					{
						if (checked)
							me.mSelectedContacts.add(contact.name);
						else
							me.mSelectedContacts.remove(contact.name);
						
						int size = me.mSelectedContacts.size();
						
						if (me.mToast != null)
							me.mToast.cancel();
							
						String toast = me.getString(R.string.supporters_selected, size);
						
						if (size == 1)
							toast = me.getString(R.string.supporter_selected);
						else if (size > 5)
							toast = me.getString(R.string.too_many_supporters_selected, size);
						
						me.mToast = Toast.makeText(me, toast, Toast.LENGTH_SHORT);
						me.mToast.show();
					}
    			});
    			
   				contactName.setText(contact.name);

    			return convertView;
    		}
    	});
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	this.updateLayout();
    }

    private void updateLayout() 
	{
		LinearLayout webLayout = (LinearLayout) this.findViewById(R.id.web_layout);
		LinearLayout moodLayout = (LinearLayout) this.findViewById(R.id.mood_layout);
		LinearLayout supportersLayout = (LinearLayout) this.findViewById(R.id.supporters_layout);
		
		webLayout.setVisibility(View.GONE);
		moodLayout.setVisibility(View.GONE);
		supportersLayout.setVisibility(View.GONE);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Button back = (Button) this.findViewById(R.id.back_button);
        
        if (this.mStep == 0)
        	back.setVisibility(View.INVISIBLE);
        else
        	back.setVisibility(View.VISIBLE);
		
		WebView webView = (WebView) webLayout.findViewById(R.id.web_view);
		
		String[] urls = this.getResources().getStringArray(R.array.intro_urls);
		String[] titles = this.getResources().getStringArray(R.array.intro_titles);

		this.getSupportActionBar().setTitle(titles[this.mStep]);

		switch (this.mStep)
		{
			case 1:
				Editor e = prefs.edit();
				e.remove(AppConstants.DEPRESSION_LEVEL);
				e.commit();
				
				moodLayout.setVisibility(View.VISIBLE);
				break;
			case 3:
				webLayout.setVisibility(View.VISIBLE);
				
				switch (prefs.getInt(AppConstants.DEPRESSION_LEVEL, 0))
				{
					case 4:
						webView.loadUrl("file:///android_asset/help_3_4.html");
						break;
					case 3:
						webView.loadUrl("file:///android_asset/help_3_3.html");
						break;
					default:
						webView.loadUrl("file:///android_asset/help_3_12.html");
						break;
				}

				break;
			case 6:
				supportersLayout.setVisibility(View.VISIBLE);

				break;
			default:
				webLayout.setVisibility(View.VISIBLE);
				webView.loadUrl(urls[this.mStep]);
				break;
		}
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
				}
			}
			
			if (found == false && "".equalsIgnoreCase(numberName) == false && numberName.contains("@") == false)
			{
				ContactRecord contact = new ContactRecord();
				contact.name = numberName;
				contact.number = phoneNumber;
				
				contacts.add(contact);
			}
		}
		
		c.close();

		c = context.getContentResolver().query(Contacts.CONTENT_URI, null, null, null, null);

		while (c.moveToNext())
		{
			String numberName = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));

			boolean found = false;
			
			if (numberName == null)
				numberName = "";
			
			for (ContactRecord contact : contacts)
			{
				if (contact.name.equals(numberName))
					found = true;
			}
			
			if (found == false && "".equalsIgnoreCase(numberName) == false && numberName.contains("@") == false)
			{
				ContactRecord contact = new ContactRecord();
				contact.name = numberName;
				contact.number = "";
				
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
		
    	return normalizedContacts;
    }
}
