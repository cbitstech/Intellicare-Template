package edu.northwestern.cbits.intellicare.dailyfeats;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class ChangeReminderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_reminder);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.change_reminder, menu);
        return true;
    }

//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//
//    }

}
