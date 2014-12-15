package com.tehran.treasure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.analytics.tracking.android.EasyTracker;
import com.tehran.util.Utils;

public class SplashActivity extends Activity {
    final private Context context = this;
    // final private String tag = this.getClass().getName();

    /**
     * The thread to process splash screen events
     */
    private Thread mSplashThread;

    @Override
    protected void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this); // Google Analytic
    }

    @Override
    protected void onStop() {
        super.onStop();

        EasyTracker.getInstance().activityStop(this); // Google Analytic
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Splash screen view
        setContentView(R.layout.activity_splash);

        final SplashActivity sPlashScreen = this;

        Utils.playSound(context, R.raw.start);

        // The thread to wait for splash screen events
        mSplashThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        // Wait given period of time or exit on touch
                        wait(3000);
                    }
                } catch (InterruptedException ex) {
                }

                finish();

                // Run next activity
                Intent intent = new Intent();
                intent.setClass(sPlashScreen, MainActivity.class);
                startActivity(intent);
                // stop();
            }
        };

        mSplashThread.start();

    }

    /**
     * Processes splash screen touch events
     */
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        if (evt.getAction() == MotionEvent.ACTION_DOWN) {
            synchronized (mSplashThread) {
                mSplashThread.notifyAll();
            }
        }
        return true;
    }

}