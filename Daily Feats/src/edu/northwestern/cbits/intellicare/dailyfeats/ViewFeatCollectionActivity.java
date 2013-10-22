package edu.northwestern.cbits.intellicare.dailyfeats;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ViewFeatCollectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feat_collection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_feat_collection, menu);
        return true;
    }
    
}
