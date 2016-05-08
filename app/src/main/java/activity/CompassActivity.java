package activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;

import msu.evan.gyrodata.Calculations;
import msu.evan.gyrodata.R;

public class CompassActivity extends Activity implements SensorEventListener{

    private SensorManager sm;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private SensorEvent lastAccelEvent;
    private SensorEvent lastMagnetEvent;
    private boolean isMagnetUpdated;
    private boolean isAccelUpdated;
    private float[] currentOrientation;
    private int directionInDegrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //Establish sensors
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Set default values
        lastAccelEvent = null;
        lastMagnetEvent = null;
        isMagnetUpdated = false;
        isAccelUpdated = false;
        currentOrientation = new float[3];
        directionInDegrees = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Register listener
        sm.registerListener(this, mAccelerometer, sm.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, mMagnetometer, sm.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sm.unregisterListener(this, mAccelerometer);
        sm.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            isAccelUpdated = true;
            lastAccelEvent = event; //Set the stored event to be the event from this sensor. When the magnetometer updates, this event will be used as reference for that point
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            isMagnetUpdated = true;
            lastMagnetEvent = event;
        }

        if (isAccelUpdated && isMagnetUpdated) {
            currentOrientation = Calculations.getCompassDirection(lastAccelEvent, lastMagnetEvent);
            directionInDegrees = (int) (Math.toDegrees(currentOrientation[0]) + 360) % 360;

            isAccelUpdated = false;
            isMagnetUpdated = false;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
