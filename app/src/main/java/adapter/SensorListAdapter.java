package adapter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Formatter;
import java.util.List;

import msu.evan.gyrodata.Characteristics;
import msu.evan.gyrodata.R;

/**
 * Created by Evan on 12/7/2015.
 */
public class    SensorListAdapter extends ArrayAdapter{

    private LayoutInflater inflater;

    public SensorListAdapter(Activity activity, List<Characteristics> items)
    {
        super(activity, R.layout.sensorlistitem, items);
        inflater = activity.getWindow().getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       /*//Row Position, Layout View, and Parent
        //What layout object to put in, What Window to put it in, False to only attach it to the current view. Don't change the root XML file
        View v = inflater.inflate(R.layout.sensorlistitem, parent,false);
        //v.setTag();
        return v;*/

        Characteristics sensor = (Characteristics) getItem(position);
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.sensorlistitem, parent,false);
        }
        TextView sName = (TextView) convertView.findViewById(R.id.textview_sensor_list_item);
        ImageView im = (ImageView) convertView.findViewById(R.id.sensor_image_view_default);

        //Determine sensor icon
        if ((sensor.name.equals(Sensor.STRING_TYPE_MAGNETIC_FIELD)) || (sensor.name.equals(Sensor.STRING_TYPE_MAGNETIC_FIELD_UNCALIBRATED)))
            im.setImageResource(R.drawable.ic_language_black_48dp);
        else if(sensor.name.equals(Sensor.STRING_TYPE_PROXIMITY))
            im.setImageResource(R.drawable.ic_visibility_black_48dp);
        else if(sensor.name.equals(Sensor.STRING_TYPE_PROXIMITY))
            im.setImageResource(R.drawable.ic_touch_app_black_24dp);
        else if(sensor.name.equals(Sensor.STRING_TYPE_LIGHT))
            im.setImageResource(R.drawable.ic_brightness_5_black_24dp);
        else if(sensor.name.equals(Sensor.STRING_TYPE_STEP_COUNTER) || sensor.name.equals(Sensor.STRING_TYPE_STEP_DETECTOR))
            im.setImageResource(R.drawable.ic_directions_walk_black_24dp);
        else
            im.setImageResource(R.drawable.sensor_image);

        sName.setGravity(Gravity.LEFT);
        //sName.setText((R.id.textview_sensor_list_item), sensor.name, sensor.power_usage, sensor.max_fifo_size)
        sName.setText(sensor.name + "\nPower Usage (mA): " + sensor.power_usage + "\nFIFO " + sensor.max_fifo_size);
        return convertView;
    }
}
