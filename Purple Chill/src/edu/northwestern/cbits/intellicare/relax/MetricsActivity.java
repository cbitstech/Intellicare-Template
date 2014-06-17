package edu.northwestern.cbits.intellicare.relax;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

/**
 * Created by Gwen on 6/17/2014.
 */
public class MetricsActivity extends ConsentedActivity {


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_metrics);
    }

    public void onResume() {

        super.onResume();

        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> playCounts = new ArrayList<String>();
        final ArrayList<String> stressRatings = new ArrayList<String>();
        final ArrayList<String> urls = new ArrayList<String>();

      //  final int titlesId = this.getIntent().getIntExtra(GroupActivity.USE, -1);
      //  final int mediaId = this.getIntent().getIntExtra(GroupActivity.GROUP_MEDIA, -1);
     //   final int timesId = this.getIntent().getIntExtra(GroupActivity.GROUP_TIMES, -1);
     //   final int groupsId = this.getIntent().getIntExtra(GroupActivity.GROUP_DESCRIPTIONS, -1);

        if (titlesId != -1 && mediaId != -1 && timesId != -1)
        {
            String[] mediaFiles = this.getResources().getStringArray(mediaId);
            String[] mediaTitles = this.getResources().getStringArray(titlesId);
            String[] mediaTimes = this.getResources().getStringArray(timesId);
            String[] mediaDescs = this.getResources().getStringArray(groupsId);

       /*     for (int i = 0; i < mediaFiles.length; i++)
            {
                titles.add(mediaTitles[i]);
                recordings.add(mediaFiles[i]);
                times.add(GroupActivity.formatTime(mediaTimes[i]));
                descriptions.add(mediaDescs[i]);
            } */
        }


        this.getSupportActionBar().setTitle(R.string.metrics_title);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_metrics, titles)
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.row_recording, parent, false);
                }

                TextView title = (TextView) convertView.findViewById(R.id.track_title);
                title.setText(titles.get(position));

                TextView playCount = (TextView) convertView.findViewById(R.id.track_play_count);
                playCount.setText(playCounts.get(position));

                //from content provider
                String url = urls.get(position);

                return convertView;
            }
        };

    }

    protected void onPause()
    {
        super.onPause();

    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.getMenuInflater().inflate(R.menu.menu_metrics, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_sort)
        {
            //sort
        }

        return true;
    }
}
