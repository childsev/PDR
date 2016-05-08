package activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import msu.evan.gyrodata.R;
import msu.evan.gyrodata.SensorObject;


public class GyroscopeActivity extends SensorObject {

    /*Info about Gyroscopes:
    * SensorEvent outputs[0,1,2]: radians/second
    * Angular Speed
    * Meaning: Rate of Rotation of Device (Positive in CCW)*/

    TextView DataDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        DataDisplay = ((TextView) findViewById(R.id.gyroData)); //X Accel
        DataDisplay.setText(getString(R.string.gyroscope_sensor_output_string, "X", null, "Y", null, "Z", null));
        initSensor(Sensor.TYPE_GYROSCOPE, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Update Orientations
        //Uses formatter from android
        DataDisplay.setText(getString(R.string.gyroscope_sensor_output_string,
                "X", event.values[0],
                "Y", event.values[1],
                "Z", event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        //release sensor to rest of device
        sm.unregisterListener(this);
        super.onStop();
    }
}
