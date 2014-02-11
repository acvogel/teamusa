package com.mbusux.teamusa;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TeamUSAActivity extends Activity implements OnClickListener {
    
    private CountDownTimer timer;
    private TextView timerText;
    private TextView roundText;

    /** Length of one jump rope round. */
    //private final long roundTime = 180000; // 3 mins
    //private final long warmupTime = 60000; // 1 min
    //private final long breakTime = 60000; // 1 min

    private final long roundTime = 20000; 
    private final long warmupTime = 10000;
    private final long breakTime = 10000; 

    /** # of rounds completed */
    private int round = 0; 
    private int nRounds = 3;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((Button) findViewById(R.id.start_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.stop_button)).setOnClickListener(this);
        timerText = (TextView) findViewById(R.id.status);
        roundText = (TextView) findViewById(R.id.round);

        timer = createWarmupTimer();
        roundText.setText("Warmup Round");
    }

    @Override
    public void onClick(View v) {
      switch(v.getId()) {
        case R.id.start_button:
          timer.start();
          break;
        
        case R.id.stop_button:
          break;
      }
    }

    private CountDownTimer createWarmupTimer() {
      updateTimer(warmupTime);
      return new CountDownTimer(warmupTime, 1000) {
        public void onTick(long millisUntilFinished) {
          //timerText.setText(Long.toString(millisUntilFinished / 1000));
          updateTimer(millisUntilFinished);
        }
        public void onFinish() {
          updateTimer(0);
          startBreak();
        }
      };
    }

    private CountDownTimer createBreakTimer() {
      updateTimer(breakTime);
      return new CountDownTimer(breakTime, 1000) {
        public void onTick(long millisUntilFinished) {
          //timerText.setText(Long.toString(millisUntilFinished / 1000));
          updateTimer(millisUntilFinished);
        }
        public void onFinish() {
          updateTimer(0);
          startRound();
        }
      };
    }

    private CountDownTimer createRoundTimer() {
      updateTimer(roundTime);
      return new CountDownTimer(roundTime, 1000) {
        public void onTick(long millisUntilFinished) {
          //timerText.setText(Long.toString(millisUntilFinished / 1000));
          updateTimer(millisUntilFinished);
        }
        public void onFinish() {
          updateTimer(0);
          startBreak();
        }
      };
    }

    private void startBreak() {
      roundText.setText("Break!");
      timer = createBreakTimer();
      timer.start();
    }

    private void startRound() {
      round++;
      if(round <= nRounds) {
        roundText.setText("Round " + round);
        timer = createRoundTimer();
        timer.start();
      } else {
        roundText.setText("Complete!");
      }
    }

    /** Functioned used to format the timerText. */
    private void updateTimer(long millis) {
      timerText.setText(
        String.format("%02d:%02d",
          (int) (millis / (1000*60)) % 60,
          (int) (millis / 1000) % 60
        )
      );
    }

}
