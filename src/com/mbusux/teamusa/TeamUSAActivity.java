package com.mbusux.teamusa;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.PreferenceFragment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

public class TeamUSAActivity extends Activity implements OnClickListener, TextToSpeech.OnInitListener {
    
    private CountDownTimer timer;
    private TextView timerText;
    private TextView roundText;

    private TextToSpeech tts;

    /** Length of one jump rope round. */
    private final long roundTime = 180000; // 3 mins
    private final long warmupTime = 60000; // 1 min
    private final long breakTime = 60000; // 1 min

    //private final long roundTime = 20000; 
    //private final long warmupTime = 10000;
    //private final long breakTime = 10000; 

    private final long roundWarning = 5000;
    private final long breakWarning = 5000; 

    private long timeLeft = 0; // time left on the current timer.

    /** # of rounds completed */
    private int round = 0; 
    private int nRounds = 4;

    private MediaPlayer mp;
    private MediaPlayer victory;
    private MediaPlayer complete;

    private boolean playMusic = true;

    private boolean isPaused = false;

    private enum State {START, WARMUP, BREAK, ROUND}
    private State state = State.START;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((Button) findViewById(R.id.start_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.music_toggle)).setOnClickListener(this);
        timerText = (TextView) findViewById(R.id.status);
        roundText = (TextView) findViewById(R.id.round);
        tts = new TextToSpeech(this, this);

        ToggleButton musicToggle = (ToggleButton) findViewById(R.id.music_toggle);
        musicToggle.setChecked(true);
        musicToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            handleMusicToggle(isChecked);
          }
        });

        ToggleButton pauseToggle = (ToggleButton) findViewById(R.id.pause_toggle);
        pauseToggle.setChecked(false);
        pauseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            handlePauseToggle(isChecked);
          }
        });
        

        timer = createWarmupTimer(warmupTime);
        roundText.setText("Team USA Fitness Challenge");
        mp = MediaPlayer.create(this, R.raw.punchout);
        mp.setLooping(true);
        victory = MediaPlayer.create(this, R.raw.victory);
        complete = MediaPlayer.create(this, R.raw.complete);
    }

    @Override
    public void onClick(View v) {
      switch(v.getId()) {
        case R.id.start_button:
          state = State.WARMUP;
          if (playMusic) {
            mp.start();
          }
          speak("Warmup round");
          roundText.setText("Warmup Round");
          timer.start();
          round = 0;
          break;
      }
    }

    private CountDownTimer createWarmupTimer(long duration) {
      updateTimer(duration);
      return new CountDownTimer(duration, 1000) {
        public void onTick(long millisUntilFinished) {
          updateTimer(millisUntilFinished);
          if(millisUntilFinished <= roundWarning + 1000) {
            int seconds =  (int) millisUntilFinished / 1000;
            speak(String.format("%d", seconds));
          }
        }
        public void onFinish() {
          if(playMusic) {
            mp.stop();
            mp.release(); 
          }
          mp = MediaPlayer.create(TeamUSAActivity.this, R.raw.punchout_round); // setup the next sound file
          mp.setLooping(true);
          //mp.prepareAsync();
          updateTimer(0);
          victory.seekTo(0);
          victory.start();
          startBreak();
        }
      };
    }

    private CountDownTimer createBreakTimer(long duration) {
      updateTimer(duration);
      return new CountDownTimer(duration, 1000) {
        public void onTick(long millisUntilFinished) {
          updateTimer(millisUntilFinished);
          if(millisUntilFinished <= breakWarning + 1000) {
            int seconds =  (int) millisUntilFinished / 1000;
            speak(String.format("%d", seconds));
          }
        }
        public void onFinish() {
          updateTimer(0);
          startRound();
        }
      };
    }

    private CountDownTimer createRoundTimer(long duration) {
      updateTimer(duration);
      return new CountDownTimer(duration, 1000) {
        public void onTick(long millisUntilFinished) {
          updateTimer(millisUntilFinished);
          if(millisUntilFinished <= roundWarning + 900) {
            int seconds =  (int) millisUntilFinished / 1000;
            speak(String.format("%d", seconds));
          }
        }
        public void onFinish() {
          updateTimer(0);
          if (playMusic) {
            mp.stop();
            mp.prepareAsync();
          }
          if(isCompleted()) {
            victory.seekTo(0);
            victory.start();
            startBreak();
          } else {
            completeWorkout();
          }
        }
      };
    }

    private void completeWorkout() {
      state = State.START;
      roundText.setText("Complete!");
      speak("Workout complete. This is how champions are made.");
      round = 0;
      if(playMusic) {
        complete.start();
      }
    }

    private void startBreak() {
      state = State.BREAK;
      roundText.setText("Break!");
      timer = createBreakTimer(breakTime);
      timer.start();
    }

    private boolean isCompleted() {
      return round < nRounds;
    }

    private void startRound() {
      round++;
      speak("Round " + round);
      roundText.setText("Round " + round);
      timer = createRoundTimer(roundTime);
      timer.start();
      state = State.ROUND;
      //mp.setDataSource(this, R.raw.punchout_round);
      //try {
      //  mp.prepare();
      //} catch (Exception e) {}
      mp.seekTo(0);
      if (playMusic) {
        mp.start();
      }
    }

    /** Functioned used to format the timerText. */
    private void updateTimer(long millis) {
      timeLeft = millis;
      timerText.setText(
        String.format("%02d:%02d",
          (int) (millis / (1000*60)) % 60,
          (int) (millis / 1000) % 60
        )
      );
    }

  @Override
  public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      int result = tts.setLanguage(Locale.US);
      if (result == TextToSpeech.LANG_MISSING_DATA
          || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e("TTS", "This Language is not supported");
      } else {
        speak("Welcome to Team USA Fitness Challenge");
      }
    } else {
      Log.e("TTS", "Initilization Failed!");
    }
  }

  public void speak(String str) {
    tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
  }

  @Override
  public void onDestroy() {
    if (tts != null) {
      tts.stop();
      tts.shutdown();
    }
    if (mp != null) {
      mp.release();
    }
    if (victory != null) {
      victory.release();
    }
    if (complete != null) {
      complete.release();
    }
    super.onDestroy();
  }

  private void handleMusicToggle(boolean setting) {
    playMusic = setting;
    if (setting) { // start the music
      if (!isPaused && (state == State.ROUND || state == state.WARMUP)) { // restart the music if we're in the middle of a round
        mp.start();
      }
    } else { // pause the music, if necessary
      if(state == State.ROUND || state == State.WARMUP) 
        mp.pause();
    }
  }

  private void handlePauseToggle(boolean setting) {
    // will need to store elapsed time and also the program state.
    isPaused = setting;
    if (isPaused) {
      timer.cancel();
      if (playMusic && (state == State.ROUND || state == State.WARMUP)) {
        mp.pause();
      }
    } else { // resume
      switch (state) {
        case ROUND:
          timer = createRoundTimer(timeLeft);
          if (playMusic) {
            mp.start();
          }
          timer.start();
          break;
        case BREAK:
          timer = createBreakTimer(timeLeft); 
          timer.start();
          break;
        case WARMUP:
          timer = createWarmupTimer(timeLeft);
          if (playMusic) {
            mp.start();
          }
          timer.start();
          break;
        case START: break; // pause doesn't do anything here
      }
    }
  }

  public void onToggleClicked(View view) {
    boolean setting = ((ToggleButton) view).isChecked();
  }

  public static class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences); 
    }
  }

}
