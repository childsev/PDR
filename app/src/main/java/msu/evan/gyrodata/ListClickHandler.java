package msu.evan.gyrodata;

import android.content.Intent;
import android.hardware.Sensor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import activity.AccelerometerActivity;
import activity.GyroscopeActivity;
import activity.LightSensorActivity;

/**
 * Created by Evan on 12/17/2015.
 */
public class ListClickHandler implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Characteristics z = (Characteristics) parent.getItemAtPosition(position);
        Intent intent;
        Log.v("Selected Sensor", Integer.toString(z.numberType));

        switch (z.numberType) {
            case Sensor.TYPE_ACCELEROMETER:
                intent = new Intent(view.getContext(), AccelerometerActivity.class);
                view.getContext().startActivity(intent);
                break;
            case Sensor.TYPE_GYROSCOPE:
                intent = new Intent(view.getContext(), GyroscopeActivity.class);
                view.getContext().startActivity(intent);
                break;
            case Sensor.TYPE_LIGHT:
                intent = new Intent(view.getContext(), LightSensorActivity.class);
                view.getContext().startActivity(intent);
            default:
                break;
        }
    }
}
