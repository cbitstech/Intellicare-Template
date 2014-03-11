package edu.northwestern.cbits.intellicare.ruminants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showTimePicker(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.time_picker_title);

        //sets timer to last time selected by user
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final WorryPracticeActivity me = this;

        int selectedDuration = prefs.getInt(WorryPracticeActivity.LAST_SELECTED_DURATION, -1);

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
                int index = prefs.getInt(WorryPracticeActivity.LAST_SELECTED_DURATION, -1);

                me.startTime = Integer.parseInt(me.getResources().getStringArray(R.array.time_choices_wpt)[index]) * 1000;

                final TextView minute = (TextView) me.findViewById(R.id.timer_minute);
                final TextView second = (TextView) me.findViewById(R.id.timer_second);

                me.countDownTimer = new CountDownTimer(me.startTime, interval)
                {
                    @Override
                    public void onFinish() {
                        minute.setText(R.string.timeup);
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

        final WorryPracticeActivity me = this;

        final Button startB = (Button) this.findViewById(R.id.start_button);
        startB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (me.countDownTimer != null)
                {
                    if (!timerHasStarted) {
                        me.countDownTimer.start();
                        me.timerHasStarted = true;
                        startB.setText(R.string.stop_wpt);
                    }
                    else {
                        countDownTimer.cancel();
                        timerHasStarted = false;
                        startB.setText(R.string.start_wpt);
                    }
                }
            }
        });
    }
}