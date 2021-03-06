package info.androidhive.senseworkout.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;

import info.androidhive.senseworkout.R;
import info.androidhive.senseworkout.activity.TimerWindow;


public class PerformancePageFragment extends Fragment {
    private final int WORKOUT = 0;
    private final int PERFORMANCE = 1;

    public PerformancePageFragment() {
        // Required empty public constructor
    }
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context= getContext();

        mySensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE); // (1)
        mySensorManager.registerListener(mySensorEventListener, mySensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL); // (2)

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_performance_layout, container, false);

        txtSpeechInput = (TextView) view.findViewById(R.id.txtSpeechInput);

        FloatingActionButton speak = (FloatingActionButton) view.findViewById(R.id.perfchange);
        if (speak != null) {
            speak.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    promptSpeechInput();

                }
            });
        }
        /* */
        LineChart lineChart = (LineChart) view.findViewById(R.id.chart);
        // creating list of entry
        ArrayList<Entry> diagramEntries = TimerWindow.entries;
        LineDataSet dataset = new LineDataSet(diagramEntries, "Movement");
        // creating labels
        ArrayList<String> labels = new ArrayList<String>();
        for (int i = 0; i < TimerWindow.entries.size(); i++) {
            labels.add(Integer.toString(i));
        }

        LineData data = new LineData(labels, dataset);
        lineChart.setData(data); // set the data and list of lables into chart
        lineChart.setDescription(" ");  // set the description
        dataset.setDrawCubic(true);

        /*Simulering
        LineChart lineChart = (LineChart) view.findViewById(R.id.chart);
        // creating list of entry
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0));
        entries.add(new Entry(1, 1));
        entries.add(new Entry(2, 2));
        entries.add(new Entry(3, 3));
        entries.add(new Entry(4, 4));
        entries.add(new Entry(3, 5));
        entries.add(new Entry(2, 6));
        entries.add(new Entry(0, 7));
        entries.add(new Entry(1, 8));
        entries.add(new Entry(3, 9));
        entries.add(new Entry(2, 10));
        entries.add(new Entry(5, 11));
        entries.add(new Entry(2, 12));
        entries.add(new Entry(1, 13));
        entries.add(new Entry(2, 14));
        entries.add(new Entry(1, 15));
        entries.add(new Entry(0, 16));
        entries.add(new Entry(1, 17));
        entries.add(new Entry(2, 18));
        entries.add(new Entry(3, 19));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");
        // creating labels
        ArrayList<String> labels = new ArrayList<String>();
        for(int i=0; i<20;i++){
            labels.add(Integer.toString(i));
        }
        LineData data = new LineData(labels, dataset);
        lineChart.setData(data); // set the data and list of lables into chart
        lineChart.setDescription("Description");  // set the description
        dataset.setDrawCubic(true);
        */
        return view;


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
     * */
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
                    if (voice.equals("byt") || voice.equals("workout") || voice.equals("byta") || voice.equals("change") || voice.equals("work out")){
                        ViewPager vp=(ViewPager) getActivity().findViewById(R.id.viewpager);
                        vp.setCurrentItem(WORKOUT);
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
    private int counter = 0;
    private int counterpause = 0;
    private final float shakeThreshold = 1.5f;

    private final SensorEventListener mySensorEventListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            counter++;
            updateAccelParameters(se.values[0], se.values[1], se.values[2]);
            if ((!shakeInitiated) && isAccelerationChanged()) {                                      // (2)
                shakeInitiated = true;
            } else if ((shakeInitiated) && isAccelerationChanged()) { // (3)
                if ((counter-counterpause) >= 50) {
                    counter = counterpause;
                    ViewPager vp=(ViewPager) getActivity().findViewById(R.id.viewpager);
                    vp.setCurrentItem(WORKOUT);


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
