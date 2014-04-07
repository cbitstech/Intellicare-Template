package edu.northwestern.cbits.intellicare.ruminants;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

/**
 * Created by Gwen on 2/26/14.
 */
public class DidacticActivity extends ConsentedActivity {

    private String[] mContentSet = null;

    public DidacticActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_content);

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

    private void generateDidactic(int index)
    {
        String[] contentValues = this.mContentSet;
        TextView content = (TextView) this.findViewById(R.id.content);
        TextView pageNumber = (TextView) this.findViewById(R.id.pageNumber);

        content.setText(contentValues[index]);
        pageNumber.setText((index + 1) + " of " + contentValues.length);

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
            this.finish();

            return;
        }


        this.mCurrentPage = page;
        this.generateDidactic(page);

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

}
