package msu.evan.gyrodata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AccelerometerService extends SensorServiceExtender implements SensorEventListener {
    //Used as indicator on Receive to flush the data
    public static final String ACTION_FLUSH_SENSOR = "msu.evan.gyrodata.FLUSH";

    SensorManager sm;
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_FLUSH_SENSOR.equals(intent.getAction())) {
                Log.i("AccelerometerService", "Flushing out Accelerometer FIFO");
                //Flushes the current sensoreventlisteners
                sm.flush(AccelerometerService.this);
            }
        }
    };

}
