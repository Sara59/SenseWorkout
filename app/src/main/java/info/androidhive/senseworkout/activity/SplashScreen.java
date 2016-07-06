package info.androidhive.senseworkout.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import info.androidhive.senseworkout.R;

public class SplashScreen extends Activity {
    private static int SPLASH_TIMEOUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
            /*
            * Splashscreen with timer
            */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            //Executed when timer runs out -> start app
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(SplashScreen.this, SimpleTabsActivity.class);
                    //Start activity
                    startActivity(intent);
                    //close activity
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        }, SPLASH_TIMEOUT);
    }
}
