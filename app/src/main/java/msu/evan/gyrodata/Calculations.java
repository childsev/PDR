package msu.evan.gyrodata;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * Created by Evan on 4/27/2016.
 */
public class Calculations {

    public static float[] getCompassDirection(SensorEvent a_event, SensorEvent m_event) {
        //Returns the direction of the Android device as an integer for use as a compass direction

        /*
        * Returns the orientation of the device at a given time
        * Input: array of 3 values from accelerometer event
        *        array of 3 values from magnetometer event
        * Output: Azimuth, Pitch, roll of device
        */

        float[] gravity = a_event.values.clone();
        float[] geomagnetic = m_event.values.clone();
        float[] R = new float[9];
        float[] I = new float[9];
        float[] direction_radians = new float[3];
        SensorManager.getRotationMatrix(R, I, gravity, geomagnetic); //Fills the R array with the rotation matrix

        //Calculates compass heading direction
        SensorManager.getOrientation(R, direction_radians); //Places the orientations in the direction_radians

        //This is the actual compass heading. Use a reference for calculating in other locations
        //int azimuth = (int) (Math.toDegrees(direction_radians[0]) + 360) % 360;


        return direction_radians;
    }
}
