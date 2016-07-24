package info.androidhive.senseworkout.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Locale;

import info.androidhive.senseworkout.R;

public class TimerWindow extends AppCompatActivity {

    ArrayList list = new ArrayList();
    final ArrayList<String> heartbeatValues = new ArrayList<String>();
    /*What acceleration difference would we assume as a rapid movement? */
    private final float shakeThreshold = 3f;
    long millisRemaining = 10000;
    TextView mTextField;
    CountDownTimer countDownTimer = null;
    boolean isPaused = true;
    boolean first = true;

    Vibrator v;

    private int counter = 0;
    private int counterpause = 0;

    int heartIndex = 0;
    int index = 0;
    String string;
    private AlertDialog mAboutDialog;
    /* The connection to the hardware */
    private SensorManager mySensorManager;
    /* Here we store the current values of acceleration, one for each axis */
    private float xAccel;
    private float yAccel;
    private float zAccel;
    /* And here the previous ones */
    private float xPreviousAccel;
    private float yPreviousAccel;
    private float zPreviousAccel;
    /* Used to suppress the first shaking */
    private boolean firstUpdate = true;
    /* Has a shaking motion been started (one direction) */
    private boolean shakeInitiated = false;
    /* The SensorEventListener lets us wire up to the real hardware events */

    private Sensor accelerometer;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private float totalMAccel;

    final static public ArrayList<Entry> entries = new ArrayList<>();
    private static int diagramIndex;
    private int diagramCount = 0;
    private long currentTime;
    private long lastTime;

    private final SensorEventListener mySensorEventListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            counter++;
            updateAccelParameters(se.values[0], se.values[1], se.values[2]);
            if ((!shakeInitiated) && isAccelerationChanged()) {                                      // (2)
                shakeInitiated = true;
            } else if ((shakeInitiated) && isAccelerationChanged()) { // (3)
                if (isPaused && (counter - counterpause) >= 50) {
                    v.vibrate(50);
                    final FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start);
                    if (start != null) {
                        changeIcon(start);
                    }
                    counterpause = counter;
                    startTimer(list);

                }
                if (!isPaused && (counter - counterpause) >= 50) {
                    v.vibrate(50);
                    final FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start);
                    if (start != null) {
                        changeIcon(start);
                    }
                    System.out.println("XXX: SecondsGone: " + (counter - counterpause));
                    counterpause = counter;
                    countDownTimer.cancel();
                    isPaused = true;

                }

            } else if ((shakeInitiated) && (!isAccelerationChanged())) {                           // (4)
                shakeInitiated = false;
            }

            if (!shakeInitiated && !isPaused) {
                if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    mGravity = se.values.clone();
                    float x = mGravity[0];
                    float y = mGravity[1];
                    float z = mGravity[2];
                    mAccelLast = mAccelCurrent;
                    mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
                    float delta = mAccelCurrent - mAccelLast;
                    mAccel = mAccel * 0.9f + delta;
                    // Make this higher or lower according to how much
                    // motion you want to detect
                    totalMAccel += mAccel;
                    diagramCount++;
                    currentTime = System.currentTimeMillis(); // current time
                    long difference = currentTime - lastTime;
                    lastTime = currentTime;
                    if (difference >= 1000) { //Saves every second
                        float medelMAccel = totalMAccel / diagramCount;
                        if (medelMAccel < 0) {
                            entries.add(new Entry(0, diagramIndex));
                        } else {
                            entries.add(new Entry(medelMAccel, diagramIndex));
                        }
                        diagramIndex++;
                        System.out.println(medelMAccel + " och index " + diagramIndex);
                        totalMAccel = 0;
                        diagramCount = 0;

                    }


                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* can be ignored in this example */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);


        Bundle b = this.getIntent().getExtras();
        list = b.getParcelableArrayList("WO");


        TextView mTextField = new TextView(this);
        mTextField = (TextView) findViewById(R.id.text);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // (1)

        mySensorManager.registerListener(mySensorEventListener, mySensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL); // (2)
        accelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        entries.clear();

        /*Adds heartbeat values*/
        String[] values = new String[]{"84", "85", "84", //
                "86", "85", "85", "87", "86"};
        for (int i = 0; i < values.length; ++i) {
            heartbeatValues.add(values[i]);
        }

        final FloatingActionButton start = (FloatingActionButton) findViewById(R.id.start);
        if (start != null) {
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeIcon(start);

                    if (isPaused) {
                        isPaused = false;
                        startTimer(list);

                    } else if (!isPaused) {
                        isPaused = true;
                        countDownTimer.cancel();
                    }


                }
            });
        }

        FloatingActionButton speak = (FloatingActionButton) findViewById(R.id.speak);
        if (speak != null) {
            speak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPaused) {
                        isPaused = true;
                        countDownTimer.cancel();
                    }

                    promptSpeechInput();

                }
            });
        }

        final FloatingActionButton pause = (FloatingActionButton) findViewById(R.id.pause);
        if (pause != null) {
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeIcon(pause);
                    if (index >= 2) {
                        index = index - 2;
                    } else {
                        index = 0;
                    }
                    countDownTimer.cancel();
                    isPaused = false;
                    first = true;
                    startTimer(list);

                }
            });
        }
        FloatingActionButton cancel = (FloatingActionButton) findViewById(R.id.cancel);
        if (cancel != null) {

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPaused) {
                        countDownTimer.cancel();
                        isPaused = true;
                    }
                    Context context = view.getContext();
                    mAboutDialog = new AlertDialog.Builder(context)
                            .setTitle("Quit your workout")
                            .setMessage("Are you sure you want to quit your workout?")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(TimerWindow.this, SimpleTabsActivity.class));

                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })

                            .show();


                }
            });
        }
    }

    private void changeIcon(FloatingActionButton button) {
        if (isPaused) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause, getTheme()));
            } else {
                button.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
            }
        } else if (!isPaused) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play, getTheme()));
            } else {
                button.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
            }
        }
    }

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private TextView txtSpeechInput;

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    String voice = result.get(0);
                    if (voice.equals("paus")) {
                        if (!isPaused) {

                            isPaused = true;
                            countDownTimer.cancel();
                        }
                    } else if (voice.equals("start") || voice.equals("starts")) {

                        isPaused = false;
                        startTimer(list);

                    } else if (voice.equals("stopp") || voice.equals("stop") || voice.equals("cancel")) {
                        Context context = TimerWindow.this;

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder = new AlertDialog.Builder(context);

                        builder.setTitle("Quit your workout");
                        builder.setMessage("Are you sure you want to quit your workout?");
                        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(TimerWindow.this, SimpleTabsActivity.class));

                            }
                        });
                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        mAboutDialog = builder.show();

                    } else {
                        if (!isPaused) {
                            isPaused = true;
                            countDownTimer.cancel();
                        }
                    }
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Store the acceleration values given by the sensor */
    private void updateAccelParameters(float xNewAccel, float yNewAccel,
                                       float zNewAccel) {
                /* we have to suppress the first change of acceleration, it results from first values being initialized with 0 */
        if (firstUpdate) {
            xPreviousAccel = xNewAccel;
            yPreviousAccel = yNewAccel;
            zPreviousAccel = zNewAccel;
            firstUpdate = false;
        } else {
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }
        xAccel = xNewAccel;
        yAccel = yNewAccel;
        zAccel = zNewAccel;
    }

    /* If the values of acceleration have changed on at least two axises, we are probably in a shake motion */
    private boolean isAccelerationChanged() {
        float deltaX = Math.abs(xPreviousAccel - xAccel);
        float deltaY = Math.abs(yPreviousAccel - yAccel);
        float deltaZ = Math.abs(zPreviousAccel - zAccel);
        return (deltaX > shakeThreshold && deltaY > shakeThreshold)
                || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
                || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }

    private void startTimer(final ArrayList list) {
        TextView heartTextField = new TextView(this); //Elin
        heartTextField = (TextView) findViewById(R.id.heartbeat);
        final TextView finalheartTextField = heartTextField;//Elin

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        TextView mTextField = new TextView(this);
        mTextField = (TextView) findViewById(R.id.text);

        if (first) {
            string = (String) list.get(index);
            millisRemaining = (int) list.get(index + 1);
            first = false;
        }


        final TextView finalMTextField = mTextField;
        countDownTimer = new CountDownTimer(millisRemaining, 1000) {


            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                if (millisUntilFinished <= 6000 && index != 2) {
                    v.vibrate(100);
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 1000);
                }
                if (heartIndex < heartbeatValues.size()) {
                    finalheartTextField.setText(heartbeatValues.get(heartIndex));
                    heartIndex++;//Elin
                } else {
                    heartIndex = 0;
                    finalheartTextField.setText(heartbeatValues.get(heartIndex));
                }

                finalMTextField.setText(string + " " + millisUntilFinished / 1000 + " s");
            }


            @Override
            public void onFinish() {
                index = index + 2;
                if (index >= list.size()) {
                    finalMTextField.setText("You have now finished your exercise, Great work!");
                } else {
                    v.vibrate(500);
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 5000);
                    first = true;
                    startTimer(list);

                }

            }
        };

        countDownTimer.start();
        isPaused = false;
    }

}