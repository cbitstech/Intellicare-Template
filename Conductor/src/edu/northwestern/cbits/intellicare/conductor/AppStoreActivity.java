package edu.northwestern.cbits.intellicare.conductor;

import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AppStoreActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_store);
	}
	
	public void onResume()
	{
		super.onResume();
		
		ArrayList<String> apps = new ArrayList<String>();
		
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		apps.add("https://dl0tgz6ee3upo.cloudfront.net/production/apps/icons/000/043/002/retina/9d2675eaa21e1730a72b0c698ee9e11e.png");
		
		final AppStoreActivity me = this;
		
		ListView list = (ListView) this.findViewById(R.id.list);
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_app_store, apps)
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                    convertView = me.getLayoutInflater().inflate(R.layout.row_app_store, parent, false);
                
                UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon);
                
                icon.setCachedImageUri(Uri.parse(this.getItem(position)));
                
                return convertView;
            }
        };
        
        list.setAdapter(adapter);
	}
}
