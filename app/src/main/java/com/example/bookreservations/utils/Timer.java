package com.example.bookreservations.utils;

import android.os.Build;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.bookreservations.R;

import java.util.Locale;

public class Timer {
    private static long START_TIME_IN_MILLIS;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private long endTime;
    private boolean isTimerRunning;
    private View rootView;
    private JsonAPIparser parser;

    private EditText timeInput;
    private TextView mTextViewCountDown;
    private Button mButtonPause;
    private Button mButtonStart;
    private Button mButtonSubmitTime;

    public Timer(View root, JsonAPIparser parser, long timeLeftInMillis) {
        START_TIME_IN_MILLIS = timeLeftInMillis;
        this.timeLeftInMillis = timeLeftInMillis;
        this.rootView = root;
        this.parser = parser;
        setRootView();
    }

    public Timer(View root, JsonAPIparser parser) {
        this(root, parser, 30000);
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMillis;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFinish() {
                parser.jsonParse();
                isTimerRunning = false;
                updateTimer();
                startTimer();
            }
        }.start();

        isTimerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        updateButtons();
    }

    private void updateTimer() {
        timeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        updateButtons();
    }

    public void setRootView() {
        mTextViewCountDown = rootView.findViewById(R.id.countdown_text);
        mButtonStart = rootView.findViewById(R.id.button_start);
        mButtonPause = rootView.findViewById(R.id.button_pause);
        timeInput = rootView.findViewById(R.id.time_input);
        mButtonSubmitTime = rootView.findViewById(R.id.submit_time);

        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning)
                    pauseTimer();
                else
                    startTimer();
            }
        });

        mButtonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTimer();
            }
        });

        mButtonSubmitTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_to_submit = timeInput.getText().toString();
                if (text_to_submit.trim().length() == 0)
                    START_TIME_IN_MILLIS = 30000;
                else
                    try {
                        START_TIME_IN_MILLIS = Integer.parseInt(text_to_submit) * 1000;
                    } catch (NumberFormatException e) {
                        START_TIME_IN_MILLIS = 30000;
                    }
                if (isTimerRunning)
                    pauseTimer();
                updateTimer();
            }
        });
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateButtons() {
        if (isTimerRunning) {
            mButtonPause.setVisibility(View.INVISIBLE);
            mButtonStart.setText("Pause");
        } else {
            mButtonStart.setText("Start");

            if (timeLeftInMillis < 1000) {
                mButtonStart.setVisibility(View.INVISIBLE);
            } else {
                mButtonStart.setVisibility(View.VISIBLE);
            }

            if (timeLeftInMillis < START_TIME_IN_MILLIS) {
                mButtonPause.setVisibility(View.VISIBLE);
            } else {
                mButtonPause.setVisibility(View.INVISIBLE);
            }
        }
    }

    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public static long getStartTimeInMillis() {
        return START_TIME_IN_MILLIS;
    }
}
