package edu.northwestern.cbits.intellicare.ruminants;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

/**
 * Created by Gwen on 2/26/14.
 */
public class IntroActivity extends ConsentedActivity {
/*
    // public static final int DISPLAY_SHOW_TITLE = 0;
    public static final String RUNBEFORE = "runBefore";

    public IntroActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
       super.onCreate(savedInstanceState);
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean skipCheck = this.getIntent().getBooleanExtra("skipCheck", false);

        // if not first use, user goes to main activity
        if (prefs.getBoolean(RUNBEFORE, true) && !skipCheck)
        {
            this.finish();

            Intent launchIntent = new Intent(this, MainActivity.class);
            this.startActivity(launchIntent);

            return;
        }

        this.setContentView(R.layout.activity_content);
    }

    private int mCurrentPage = -1;

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mCurrentPage == -1)
            this.mCurrentPage = 0;
        this.goTo(this.mCurrentPage);
    }

    private void generateIntro(Context context, int index)
    {
        String[] contentValues = IntroActivity.contentValues(context, false);
        TextView content = (TextView) this.findViewById(R.id.content);
        //TextView pageNumber = (TextView) this.findViewById(R.id.pageNumber);

        content.setText(contentValues[index]);
        //pageNumber.setText((index + 1) + " of " + contentValues.length);

        setTitle("Page " + (index + 1) + " of " + contentValues.length);
        //setSubtitle("");
    }

    public static String[] contentValues(Context context, boolean includeAll)
    {
        return context.getResources().getStringArray(R.array.intro_content);
    }

    private void goTo(int page) {

        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);

        int sizeStack =  am.getRunningTasks(1).get(0).numActivities;

        Log.e("Cows", "sizeStack = "+sizeStack );


        if (page >= 10)
        {
            Intent launchIntent = new Intent(this, MainActivity.class);
            this.startActivity(launchIntent);

            return;

        }
        else if (page < 0)
        {   if (sizeStack == 1)
            {
            Toast.makeText(this, R.string.nope, Toast.LENGTH_LONG).show();
            page = 0;}

            else {
                this.finish();

                return;
                }
        }

        this.mCurrentPage = page;

        this.generateIntro(this, page);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_activity_intro, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_previous:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                /*if (this.mCurrentPage == -1)
                {
                    invalidateOptionsMenu();
                    item.setVisible(false);
                    item.setTitle("why");
                }
                this.goTo(this.mCurrentPage - 1);
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                if (this.mCurrentPage == 8){
                    item.setTitle("Finish");
                }
                this.goTo(this.mCurrentPage + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
}
