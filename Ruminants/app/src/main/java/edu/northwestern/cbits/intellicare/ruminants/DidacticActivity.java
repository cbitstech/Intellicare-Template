package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.content.ContentValues;
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
public class DidacticActivity extends Activity {

    private static final int NUM_PAGES = 5;

    public static int DIDACTIC_COMPLETE = 0;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;
    private String[] mContentSet = null;

    public DidacticActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_didactic);

        this.mContentSet = DidacticActivity.chooseRandomContentSet(this);
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

        if (page >= this.mContentSet.length)
        {
            ContentValues values = new ContentValues();

            values.put(RuminantsContentProvider.DIDACTIC_TIMESTAMP, System.currentTimeMillis());
            this.getContentResolver().insert(RuminantsContentProvider.DIDACTIC_USE_URI, values);

            Intent launchIntent = new Intent(this, MainActivity.class);
            this.startActivity(launchIntent);

            return;
        }
        else if (page < 0)
        {
            Toast.makeText(this, R.string.nope, Toast.LENGTH_LONG).show();
            page = 0;
        }

        WebView didacticView = (WebView) this.findViewById(R.id.didactic);

        didacticView.getSettings().setJavaScriptEnabled(true);

        Log.e("COWS", "IN onCreate, about to call HTML generator...");

        didacticView.loadDataWithBaseURL("file:///android_asset/www/", this.generateDidactic(page), "text/html", "utf-8", null);

        this.mCurrentPage = page;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_activity_didactic, menu);

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

    public static String[][] didacticContentSets(Context context) {

        String[] one = context.getResources().getStringArray(R.array.didactic_content1);
        String[] two = context.getResources().getStringArray(R.array.didactic_content2);
        String[] three = context.getResources().getStringArray(R.array.didactic_content3);
        String[] four = context.getResources().getStringArray(R.array.didactic_content4);
        String[] five = context.getResources().getStringArray(R.array.didactic_content5);
        String[] six = context.getResources().getStringArray(R.array.didactic_content6);
        String[] seven = context.getResources().getStringArray(R.array.didactic_content7);
        String[] eight = context.getResources().getStringArray(R.array.didactic_content8);
        String[] nine = context.getResources().getStringArray(R.array.didactic_content9);

        String[][] didacticContentSet = { one, two, three, four, five, six, seven, eight, nine };

        return didacticContentSet;

    }

    public static String[] chooseContentSet(String[][] didacticContentSet){

        Random random = new Random() ;
        int randomNum = random.nextInt(didacticContentSet.length);

        String[] contentSet = didacticContentSet[randomNum];

        return contentSet;
    }

    public static String[] chooseRandomContentSet(Context context)
    {
        return chooseContentSet(didacticContentSets(context));
    }

    private String generateDidactic(int index)
    {
        StringBuilder buffer = new StringBuilder();

        try
        {
            InputStream html = this.getAssets().open("www/didactic.html");

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

        String[] punValues = punValues(this);


        int max = this.mContentSet.length;

        htmlString = htmlString.replace("{{pun}}", punValues[index]);
        htmlString = htmlString.replace("{{content}}", this.mContentSet[index]);
        htmlString = htmlString.replace("{{progress}}", "" + (((index + 1) * 100) / max));
        htmlString = htmlString.replace("{{page}}", (index + 1) + " of " + max);

        Log.e("COWS", "HTML: " + htmlString);

        return htmlString;
    }

    private static String[] punValues(Context context)
    {
        String[] punValues = new String[5];

        String[] punBank = context.getResources().getStringArray(R.array.pun_bank);

        /* want to pick a subset of puns */
        int max = punBank.length;
        Random random = new Random();
        int randomNum = random.nextInt(max - 5); // ensures that the range won't exceed the max index of the pun bank

        // Arguments are: sourceArray, sourceStartIndex, destinationArray, destinationStartIndex, numElementsToCopy
        System.arraycopy( punBank, randomNum, punValues, 0, 5 );

        return punValues;
    }

}
