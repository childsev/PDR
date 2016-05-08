package activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import msu.evan.gyrodata.R;
import msu.evan.gyrodata.SensorObject;


public class LightSensorActivity extends SensorObject {

    /*Info about Light Sensors
    * SensorEvent Output[0]: SI Lux units
    * Ambient light level */

    TextView X;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_sensor);

        X = ((TextView) findViewById(R.id.LightView)); // Light Sensor Output

        initSensor(Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        X.setText(getString(R.string.light_sensor_output_string, event.values[0]));
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
