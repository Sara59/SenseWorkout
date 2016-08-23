package info.androidhive.senseworkout.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import info.androidhive.senseworkout.R;
import info.androidhive.senseworkout.activity.SimpleTabsActivity;
import info.androidhive.senseworkout.activity.TimerWindow;


public class WorkoutPageFragment extends Fragment implements View.OnClickListener {

    Button btnStartWorkout;
    private final int WORKOUT = 0;
    private final int PERFORMANCE = 1;

    TextView txt_help_gest;
    TextView txt_help_gest2;
    TextView show_more1;
    TextView show_more2;
    TextView title;
    TextView title2;
    ArrayList list1 = new ArrayList();
    ArrayList list2 = new ArrayList();

    private AlertDialog dialog;


    private Context context;

    public WorkoutPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        list1.add("Plankan");
        list1.add(15000);
        list1.add("Next Exercise in: ");
        list1.add(5000);
        list1.add("Upphopp");
        list1.add(150000);

        list2.add("Trädet");
        list2.add(15000);
        list2.add("Next Exercise in: ");
        list2.add(5000);
        list2.add("Djup squat");
        list2.add(150000);


        context = getContext();


        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE); // (1)
        mySensorManager.registerListener(mySensorEventListener, mySensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL); // (2)

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout_layout, container, false);

        txtSpeechInput = (TextView) view.findViewById(R.id.txtSpeechInput);

        FloatingActionButton speak = (FloatingActionButton) view.findViewById(R.id.change);
        if (speak != null) {
            speak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    promptSpeechInput();

                }
            });
        }

        btnStartWorkout = (Button) view.findViewById(R.id.btnStartWorkout);
        btnStartWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), TimerWindow.class));

            }
        });

        txt_help_gest = (TextView) view.findViewById(R.id.txt_help_gest);
        // hide until its title is clicked
        txt_help_gest.setVisibility(View.GONE);

        TextView buttonInTv = (TextView) view.findViewById(R.id.buttonInTv);
        buttonInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWO(list1);
            }
        });

        title = (TextView) view.findViewById(R.id.help_title_gest);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_contents(title);
            }
        });

        show_more1 = (TextView) view.findViewById(R.id.show_more1);
        show_more1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_contents(title);
            }
        });


        txt_help_gest2 = (TextView) view.findViewById(R.id.txt_help_gest2);
        // hide until its title is clicked
        txt_help_gest2.setVisibility(View.GONE);

        TextView buttonInTv2 = (TextView) view.findViewById(R.id.buttonInTv2);
        buttonInTv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWO(list2);
            }
        });

        title2 = (TextView) view.findViewById(R.id.help_title_gest2);
        title2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_contents(title2);
            }
        });

        show_more2 = (TextView) view.findViewById(R.id.show_more2);
        show_more2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle_contents(title2);
            }
        });


        Context context = view.getContext();
        dialog = new AlertDialog.Builder(context)
                .setMessage("Hi! \n In this app you can use voice control! \n \"Change\" - To shift between tabs. \n \"Start workout n\" - To start a specific workout \n \n During workout you can say: \n \"Start\" - To start \n \"Pause\" - To pause \n \"Stop\" - To stop \n \n Thank you for using SenseWorkout!")
                .setTitle("Welcome to SenseWorkout")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();


        return view;
    }

    public void startWO(ArrayList list) {
        Intent i = new Intent(getActivity(), TimerWindow.class);
        Bundle b = new Bundle();
        b.putParcelableArrayList("WO", list);
        i.putExtras(b);
        startActivityForResult(i, 1);
    }

    public void toggle_contents(View v) {
        if (v.equals(title)) {
            txt_help_gest.setVisibility(txt_help_gest.isShown()
                    ? View.GONE
                    : View.VISIBLE);
        }
        if (v.equals(title2)) {
            txt_help_gest2.setVisibility(txt_help_gest2.isShown()
                    ? View.GONE
                    : View.VISIBLE);
        }
    }

    public void onClick(View view) {

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
            Toast.makeText(getActivity(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private TextView txtSpeechInput;

    /**
     * Receiving speech input
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    String voice = result.get(0);
                    if (voice.equals("byt") || voice.equals("performance") || voice.equals("byta") || voice.equals("change")) {
                        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.viewpager);
                        vp.setCurrentItem(PERFORMANCE);
                    } else if (voice.equals("start first") || voice.equals("quickstart first") || voice.equals("starts first")
                            || voice.equals("first") || voice.equals("first workout") || voice.equals("start first workout")
                            || voice.equals("start workout one") || voice.equals("workout ett")) {
                        startWO(list1);
                    } else if (voice.equals("start second") || voice.equals("quickstart second") || voice.equals("starts second")
                            || voice.equals("second") || voice.equals("second workout") || voice.equals("start second workout")
                            || voice.equals("start workout two") || voice.equals("workout två")) {
                        startWO(list2);
                    }

                }
                break;
            }

        }
    }

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
    private int count = 0;
    private int counterpause = 0;
    private final float shakeThreshold = 1.5f;


    private final SensorEventListener mySensorEventListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            count++;
            updateAccelParameters(se.values[0], se.values[1], se.values[2]);
            if ((!shakeInitiated) && isAccelerationChanged()) {                                      // (2)
                shakeInitiated = true;
            } else if ((shakeInitiated) && isAccelerationChanged()) { // (3)
                if ((count - counterpause) >= 50) {
                    count = counterpause;
                    ViewPager vp = (ViewPager) getActivity().findViewById(R.id.viewpager);
                    vp.setCurrentItem(PERFORMANCE);


                }

            } else if ((shakeInitiated) && (!isAccelerationChanged())) {                           // (4)
                shakeInitiated = false;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* can be ignored in this example */
        }
    };

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
}
