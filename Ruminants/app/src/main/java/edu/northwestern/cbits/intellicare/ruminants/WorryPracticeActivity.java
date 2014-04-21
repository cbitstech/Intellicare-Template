package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;

public class WorryPracticeActivity extends Activity  {

    // modal dialogue with instructions
    public void showInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.wpt_instructions);
        builder.setTitle(R.string.wpt_instructions_title);

        final WorryPracticeActivity me = this;

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {   //send user to next dialog
                me.showTimePicker();
            }
        });

        if (!timerHasStarted) {
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate(R.menu.menu_wpt, menu);

        return true;
    }

    // strategies, play/pause in action bar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_strategies:

                Intent didacticIntent =  new Intent(this, DidacticActivity.class);
                this.startActivity(didacticIntent);

            case R.id.action_start_stop:

                final WorryPracticeActivity me = this;

                if (me.countDownTimer != null)
                {
                    if (!timerHasStarted) {
                        me.countDownTimer.start();
                        me.timerHasStarted = true;
                        item.setIcon(R.drawable.ic_action_stop);
                    }
                    else {
                        countDownTimer.cancel();
                        timerHasStarted = false;
                        item.setIcon(R.drawable.ic_action_play);
                    }
                }
        }

        return super.onOptionsItemSelected(item);
    }

    // modal dialogue after timer is done
    public void wptOver() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.wpt_complete);
        builder.setTitle(R.string.wpt_complete_title);

        final WorryPracticeActivity me = this;

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            Intent mainIntent =  new Intent(me, MainActivity.class);

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(mainIntent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showTimePicker(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.time_picker_title);

        //sets timer to last time selected by user
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final WorryPracticeActivity me = this;

        int selectedDuration = prefs.getInt(WorryPracticeActivity.LAST_SELECTED_DURATION, 0);

        // time choices are 8, 5 and 3 min expressed as seconds
        builder.setSingleChoiceItems(R.array.time_choices_wpt, selectedDuration, new DialogInterface.OnClickListener()
        {
            public void onClick (DialogInterface dialog, int which)
            {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putInt(WorryPracticeActivity.LAST_SELECTED_DURATION, which);
                edit.commit();
            }
        });

        // dismiss dialog, pass time choice to initialize timer start value
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                int index = prefs.getInt(WorryPracticeActivity.LAST_SELECTED_DURATION, 0);

                me.startTime = Integer.parseInt(me.getResources().getStringArray(R.array.time_intervals_wpt)[index]) * 1000;

                final TextView minute = (TextView) me.findViewById(R.id.timer_minute);
                final TextView second = (TextView) me.findViewById(R.id.timer_second);

                me.countDownTimer = new CountDownTimer(me.startTime, interval)
                {
                    @Override
                    public void onFinish() {

                        ContentValues values = new ContentValues();

                        values.put(RuminantsContentProvider.WPT_USE_TIMESTAMP, System.currentTimeMillis());
                        me.getContentResolver().insert(RuminantsContentProvider.WPT_USE_URI, values);

                        minute.setText(R.string.timeup);
                        second.setText("");

                        // getSystemService(VIBRATOR_SERVICE);
                        // vibrate(1000);

                        wptOver();
                    }
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // convert seconds to display as m:ss

                        long seconds = millisUntilFinished / 1000;

                        long minutes = seconds / 60;
                        seconds = seconds % 60;

                        /*
                        if (minutes < 10)
                            minute.setText("0" + minutes);
                        else
                            minute.setText("" + minutes);
                            */

                        if (seconds < 10)
                            second.setText("0" + seconds);
                        else
                            second.setText("" + seconds);

                        minute.setText( minutes + ":");
                    }


                };

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private static String LAST_SELECTED_DURATION = "last_selected_duration";

    protected void onResume() {
        super.onResume();
        this.showInstructions();

    }

    private CountDownTimer countDownTimer = null;
    private boolean timerHasStarted = false;

    private long startTime = 0;
    private final long interval = 1 * 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worry_practice_tool);

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

}