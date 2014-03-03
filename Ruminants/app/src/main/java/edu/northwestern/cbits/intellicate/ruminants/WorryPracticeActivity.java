package edu.northwestern.cbits.intellicate.ruminants;

public class WorryPracticeActivity extends Activity implements OnClickListener {

    private CountDownTimer countDownTimer;
    private boolean timerHasStarted = false;
    private Button startB;
    public TextView text;
    private final long startTime = 180 * 1000;
    private final long interval = 1 * 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worry_practice_tool);
        startB = (Button) this.findViewById(R.id.start_stop_button);
        startB.setOnClickListener(this);
        text = (TextView) this.findViewById(R.id.timer);
        countDownTimer = new MyCountDownTimer(startTime, interval);
        text.setText(text.getText() + String.valueOf(startTime / 1000));
    }

    @Override
    public void onClick(View v) {
        if (!timerHasStarted) {
            countDownTimer.start();
            timerHasStarted = true;
            startB.setText(R.string.stop_wpt);
        } else {
            countDownTimer.cancel();
            timerHasStarted = false;
            startB.setText(R.string.start_wpt);
        }
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            text.setText(R.string.timeup);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            text.setText("" + millisUntilFinished / 1000);
        }
    }

}