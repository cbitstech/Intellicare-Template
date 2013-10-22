package edu.northwestern.cbits.intellicare.dailyfeats;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import edu.northwestern.cbits.intellicare.dailyfeats.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SetupActivity extends FragmentActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.middle_top_screen);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
            .setOnVisibilityChangeListener(
                    new SystemUiHider.OnVisibilityChangeListener() {
                        // Cached values.
                        int mControlsHeight;
                        int mShortAnimTime;

                        @Override
                        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                        public void onVisibilityChange(boolean visible) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                                // If the ViewPropertyAnimator API is available
                                // (Honeycomb MR2 and later), use it to animate the
                                // in-layout UI controls at the bottom of the
                                // screen.
                                if (mControlsHeight == 0) {
                                    mControlsHeight = controlsView.getHeight();
                                }
                                if (mShortAnimTime == 0) {
                                    mShortAnimTime = getResources().getInteger(
                                            android.R.integer.config_shortAnimTime);
                                }
                                controlsView.animate()
                                        .translationY(visible ? 0 : mControlsHeight)
                                        .setDuration(mShortAnimTime);
                            } else {
                                // If the ViewPropertyAnimator APIs aren't
                                // available, simply show or hide the in-layout UI
                                // controls.
                                controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                            }

                            if (visible && AUTO_HIDE) {
                                // Schedule a hide().
                                delayedHide(AUTO_HIDE_DELAY_MILLIS);
                            }
                        }
                    }
            );

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (TOGGLE_ON_CLICK) {
                mSystemUiHider.toggle();
            } else {
                mSystemUiHider.show();
            }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.continue_button).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.back_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Everything above is android full-screen defaults.
     * This is where Gabe starts writing code.
     */

    private SharedPreferences prefs;
    private int currentSetupStep = 0;


    @Override
    protected void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        /**
         * Handles the within-activity navigation
         * & the initial render.
         */

        final Button backBtn = (Button) this.findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                decStep();
                showCurrentStep();
            }
        });

        final Button continueBtn = (Button) this.findViewById(R.id.continue_button);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incStep();
                showCurrentStep();
            }
        });

        showCurrentStep();

    }

    /**
     * Displays the appropriate elements & text based on the step
     * within the setup process on which the user is on.
     */
    private void showCurrentStep() {
        int currentStep = getCurrentStep();

        Log.d("ShowCurrentStep", "Current step is "+currentStep);

        // When done with setup, go back to the Startup Activity,
        // Which decides what to do next.
        if ( currentStep > AppConstants.setupConclusion) {
            Log.d("ShowCurrentStep", "Setup is complete, relaunching Startup Activity");
            Intent i = new Intent(SetupActivity.this, StartupActivity.class);
            startActivity(i);
            this.finish();
        }

        // These five elements will start off hidden,
        // and will be made visible only if so desired in the
        // current step.
        TextView mainText       = (TextView)    this.findViewById(R.id.middle_top_screen);
        RadioGroup levelSelect  = (RadioGroup)  this.findViewById(R.id.depression_level_question);
        Button supportersBtn    = (Button)      this.findViewById(R.id.choose_supporters);
        Button continueBtn      = (Button)      this.findViewById(R.id.continue_button);
        Button backBtn          = (Button)      this.findViewById(R.id.back_button);
        Button reminderTimeBtn  = (Button)      this.findViewById(R.id.reminder_time_button);

        mainText.setVisibility(View.GONE);
        levelSelect.setVisibility(View.GONE);
        supportersBtn.setVisibility(View.GONE);
        continueBtn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        reminderTimeBtn.setVisibility(View.GONE);

        switch (currentStep) {
            case AppConstants.setupWelcome:
                mainText.setText(getString(R.string.setup_1_welcome));
                mainText.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.GONE);
                break;
            case AppConstants.setupStep2:
                //Display Depression Level Question:
                levelSelect.setVisibility(View.VISIBLE);
                continueBtn.setVisibility(View.GONE);
                break;
            case AppConstants.setupStep3:
                mainText.setText(getString(R.string.setup_3));
                mainText.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupStep4:
                mainText.setText(getString(R.string.setup_4_p1)+" "+getString(R.string.setup_4_p2));
                mainText.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupStep5:
                mainText.setText(getString(R.string.setup_5));
                mainText.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupStep6:
                mainText.setText(getString(R.string.setup_6));
                mainText.setVisibility(View.VISIBLE);
                reminderTimeBtn.setText( getReminderTimeString() );
                reminderTimeBtn.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupStep7:
                mainText.setText(getString(R.string.setup_7));
                mainText.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupStep8:
                mainText.setText(getString(R.string.setup_8));
                mainText.setVisibility(View.VISIBLE);
                supportersBtn.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupStep9:
                mainText.setText(getString(R.string.setup_9));
                mainText.setVisibility(View.VISIBLE);
                break;
            case AppConstants.setupConclusion:
                mainText.setText(getString(R.string.setup_10_conclusion));
                mainText.setVisibility(View.VISIBLE);
                break;
        }
    }



    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    /**
     * Incrementer, Decrementer, and Getter used internally
     * for managing which step of the setup process the user is on.
     */

    private void incStep() {
        currentSetupStep += 1;
        prefs.edit().putInt(AppConstants.currentSetupKey, currentSetupStep).commit();
    }

    private void decStep() {
        currentSetupStep -= 1;
        prefs.edit().putInt(AppConstants.currentSetupKey, currentSetupStep).commit();
    }

    private int getCurrentStep() {

        if (currentSetupStep == 0)
        {
            currentSetupStep = Math.max(1,prefs.getInt(AppConstants.currentSetupKey, AppConstants.setupWelcome));
            Log.d("getCurrentStep", "Current Step Initialized To "+currentSetupStep);
        }
        return currentSetupStep;
    }

    /**
     *  getReminderTimeString
     *  duplicated between Setup Activity and HomeActivity
     *  out of expediency, and not sure how to share functions that depend on Android Context
     *  between Activities
     *  -Gabe
     **/
    private String getReminderTimeString() {

        String amPM;
        String minutes;
        int reminderHour = prefs.getInt(AppConstants.reminderHourKey,    AppConstants.defaultReminderHour);
        int reminderMins = prefs.getInt(AppConstants.reminderMinutesKey, AppConstants.defaultReminderMinutes);

        if (reminderHour > 12) {
            amPM = "PM";
            reminderHour = reminderHour - 12;
        }
        else {
            amPM = "AM";
        }

        if (reminderMins < 10) {
            minutes = "0"+String.valueOf(reminderMins);
        }
        else {
            minutes = String.valueOf(reminderMins);
        }

        return String.valueOf(reminderHour)+":"+minutes+" "+amPM;
    }

    private void setChecklistLevel(int level) {
        Log.d("SetupActivity", "setting checklist level to "+level);
        prefs.edit().putInt(AppConstants.checklistLevelKey, level).commit();
    }


    /**
     * Click handler used in step 2 of setup, to record and store
     * the user's current depression level, which corresponds to the
     * level of the checklist they are given.
     */
    public void onDepressionLevelClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_level_1:
                if (checked)
                    setChecklistLevel(1);
                    break;
            case R.id.radio_level_2:
                if (checked)
                    setChecklistLevel(2);
                    break;
            case R.id.radio_level_3:
                if (checked)
                    setChecklistLevel(3);
                    break;
            case R.id.radio_level_4:
                if (checked)
                    setChecklistLevel(4);
                    break;
        }

        incStep();
        showCurrentStep();

    }

    /**
     * Click handler used in step 8 of setup, to
     * hand off control to the Android contacts picker
     * when the user is asked to choose a set of supporters.
     *
     * Currently just a placeholder for what will be
     * the actual Contacts Picker Code.
     */
    public void onSupportersButtonClicked(View view) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Contacts Picker");
        alert
            .setMessage("Here we should use the contacts picker")
            .setCancelable(false)
            .setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            SetupActivity.this.incStep();
                            SetupActivity.this.showCurrentStep();
                        }
                    })
            .setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

        // create final & show dialog
        AlertDialog alertDialog = alert.create();
        alertDialog.show();

    }

}
