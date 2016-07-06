package info.androidhive.senseworkout.fragments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Amar on 2016-05-18.
 */
public class ProximitySensorFragment extends FragmentActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    public void onSensorChanged(SensorEvent event){

    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
