package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Created by Gwen on 2/26/14.
 */
public class IntroActivity extends Activity {

    public static final String RUNBEFORE = "runBefore";

    public IntroActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


      /*  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean skipCheck = this.getIntent().getBooleanExtra("skipCheck", false);

        // if not first use, user goes to main activity
        if (prefs.getBoolean(RUNBEFORE, true) && !skipCheck)
        {
            this.finish();

            Intent launchIntent = new Intent(this, MainActivity.class);
            this.startActivity(launchIntent);

            return;
        } */

        this.setContentView(R.layout.activity_intro);
    }

    private int mCurrentPage = -1;

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mCurrentPage == -1)
            this.mCurrentPage = 0;
        this.goTo(this.mCurrentPage);
    }

    private void goTo(int page) {

        String[] titleValues = IntroActivity.titleValues(this, false);

        ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);

        int sizeStack =  am.getRunningTasks(1).get(0).numActivities;

        Log.e("Cows", "sizeStack = "+sizeStack );

        if (page >= titleValues.length)
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

        WebView introView = (WebView) this.findViewById(R.id.intro);

        introView.getSettings().setJavaScriptEnabled(true);

        Log.e("COWS", "IN onCreate, about to call HTML generator...");

        introView.loadDataWithBaseURL("file:///android_asset/www/", IntroActivity.generateIntro(this, page), "text/html", "utf-8", null);

        this.mCurrentPage = page;

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
                this.goTo(this.mCurrentPage - 1);
                return true;

            case R.id.action_next:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                this.goTo(this.mCurrentPage + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static String generateIntro(Context context, int index)
    {
        StringBuilder buffer = new StringBuilder();

        try
        {
            InputStream html = context.getAssets().open("www/intro.html");

            BufferedReader in = new BufferedReader(new InputStreamReader(html));

            String str = null;

            while ((str=in.readLine()) != null)
            {
                buffer.append(str);
            }

            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String htmlString = buffer.toString();

        String[] titleValues = IntroActivity.titleValues(context, false);
        String[] punValues = IntroActivity.punValues(context);
        String[] contentValues = IntroActivity.contentValues(context, false);

        int max = contentValues.length;

        htmlString = htmlString.replace("{{title}}", titleValues[index]);
        htmlString = htmlString.replace("{{pun}}", punValues[index]);
        htmlString = htmlString.replace("{{content}}", contentValues[index]);
        htmlString = htmlString.replace("{{progress}}", "" + (((index + 1) * 100) / max));
        htmlString = htmlString.replace("{{page}}", (index + 1) + " of " + max);

        Log.e("COWS", "HTML: " + htmlString);
        Log.e("COWS", "CONTENT=" + contentValues[index]);

        return htmlString;
    }

    public static String[] titleValues(Context context, boolean includeAll)
    {
        return context.getResources().getStringArray(R.array.intro_titles);
    }
    private static String[] punValues(Context context)
    {
        // select the size of the pun sub-array to show based on the number of slides with content
        int punNumber = IntroActivity.titleValues(context, false).length;
        String[] punValues = new String[punNumber];
        String[] punBank = context.getResources().getStringArray(R.array.pun_bank);

        /* want to pick a subset of puns */
        int max = punBank.length;
        Random random = new Random();
        int randomNum = random.nextInt(4);

        // Arguments are: sourceArray, sourceStartIndex, destinationArray, destinationStartIndex, numElementsToCopy
        System.arraycopy( punBank, randomNum, punValues, 0, punNumber );

        return punValues;
    }

    private static String[] contentValues(Context context, boolean includeAll)
    {
        return context.getResources().getStringArray(R.array.intro_content);
    }
}
