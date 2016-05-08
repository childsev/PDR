package msu.evan.gyrodata;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;


public abstract class SensorObject extends Activity implements SensorEventListener {

    protected List<TextView> t;
    //protected int sensorType;
    protected SensorManager sm;
    protected Sensor sensor;
    protected int batch_delay;
    protected final int MICROSECONDS_PER_MINUTE = 60000000; //Check this value

    public SensorObject () {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Default constructor, no input. Register service and wait....
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        /*
        * BATCHING!
        * http://developer.android.com/reference/android/hardware/SensorManager.html#registerListener%28android.hardware.SensorEventListener,%20android.hardware.Sensor,%20int,%20int%29
        * */
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, batch_delay);

        //sm.registerListener(this, sensor, batch_delay);
        super.onResume();
    }

    @Override
    protected void onStop() {
        //release sensor to rest of device
        // sm.unregisterListener(this);
        super.onStop();
    }

    @Override
    public abstract void onSensorChanged(SensorEvent event);

    @Override
    public abstract void onAccuracyChanged(Sensor sensor, int accuracy);


    protected void initSensor(int type, int delay){
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(type);
        this.batch_delay = delay;
        Log.w("Max fifo size: ", "" + sensor.getFifoMaxEventCount());
    }

}
