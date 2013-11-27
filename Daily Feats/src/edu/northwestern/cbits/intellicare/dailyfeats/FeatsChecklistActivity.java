package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class FeatsChecklistActivity extends ConsentedActivity
{
    private ArrayList<ContentValues> mCurrentFeats = null;
    private int mCurrentFeatsLevel = -1;
    
    private HashSet<String> mCompletedFeats = new HashSet<String>();

    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_complete_checklist);
    }

    protected void onResume() 
    {
        super.onResume();
        
        final FeatsChecklistActivity me = this;

        this.mCurrentFeats = this.feats();

        ArrayAdapter<ContentValues> featsAdapter = new ArrayAdapter<ContentValues>(this, R.layout.row_feat_checkbox, this.mCurrentFeats) 
		{
            public View getView (int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.row_feat_checkbox, null);
                }

                ContentValues feat = getItem(position);

                CheckBox check = (CheckBox) convertView.findViewById(R.id.feat_checkbox);
                
                check.setText(feat.getAsString("feat_name"));
//                check.setChecked(f.isCompleted());
                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    public void onCheckedChanged(CompoundButton button, boolean checked)
                    {
                    	if (checked)
                    		me.mCompletedFeats.add(button.getText().toString());
                    	else
                    		me.mCompletedFeats.remove(button.getText().toString());
                    }
                });

                return convertView;
            }
        };

        final ListView featsList = (ListView) this.findViewById(R.id.feats_checklist);
        featsList.setAdapter(featsAdapter);
    }
    
    private ArrayList<ContentValues> feats() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String selection = "feat_level = ?";
    	String[] args = { "3" };
    	
        this.mCurrentFeatsLevel = prefs.getInt(getString(R.string.currentFeatsLevelKey), 2);
        
        ArrayList<ContentValues> feats = new ArrayList<ContentValues>();
        
        if (this.mCurrentFeatsLevel <= 2) 
        {
        	selection = "feat_level <= ?";
        	args[0] = "2";
        }
        else 
        {
        	if (this.mCurrentFeatsLevel == 4) 
        	{
            	selection = "feat_level >= ?";
            	args[0] = "3";
        	}
        }

        Cursor c = this.getContentResolver().query(FeatsProvider.FEATS_URI, null, selection, args, "feat_level DESC, feat_name");
        
        while (c.moveToNext())
        {
        	ContentValues feat = new ContentValues();
        	feat.put("feat_name", c.getString(c.getColumnIndex("feat_name")));
        	feat.put("feat_level", c.getInt(c.getColumnIndex("feat_level")));
        	
        	feats.add(feat);
        }
        
        c.close();
        
        return feats;
    }

    /* private void shareList( HashMap<String, Object> saveResults ) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String username = prefs.getString("username", "Corky Templeshire");
        StringBuilder shareText = new StringBuilder();
        shareText.append("<h2>Today, "+username+":</h2>");
        shareText.append("<ul>");

        Iterator<Feat> it = this.mCurrentFeats.iterator();
        Feat f;
        while(it.hasNext()) {
            f = (Feat) it.next();
            if(f.isCompleted())
            {
                shareText.append("<li>"+AppConstants.shareFeatText.get(f.getFeatName())+"</li>");
            }
        }
        shareText.append("</ul>");

        Log.d("onShare", "Share Text: "+shareText.toString());
    } */

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_feats_checklist, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_save:
				Log.e("DF", "SAVED ITEMS: " + this.mCompletedFeats);
				
				long now = System.currentTimeMillis();
				
				for (String feat : this.mCompletedFeats)
				{
					ContentValues response = new ContentValues();
					
					response.put("recorded", now);
					response.put("depression_level", this.mCurrentFeatsLevel);
					response.put("feat", feat);
					
					this.getContentResolver().insert(FeatsProvider.RESPONSES_URI, response);
				}

                this.finish();
				
				break;
		}

		return true;
	}

}