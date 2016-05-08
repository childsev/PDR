package msu.evan.gyrodata;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import database.DatabaseContract;
import database.SensorDatabaseHelper;

/**
 * Created by Evan on 2/5/2016.
 *
 * Service adaptation of the SensorObject Class
 * No longer requires activity to be active for the application to track data
 */
public abstract class SensorServiceExtender extends Service implements SensorEventListener {
    //Used as indicator on Receive to flush the data
    public static final String ACTION_FLUSH_SENSOR = "msu.evan.gyrodata.FLUSH";

    //Database stuff
    SensorDatabaseHelper DBHelper;
    protected SQLiteDatabase dataDb;
    protected int stepCount = 0;

    //Time related
    //Original for logging. Divided by 1E6 to convert to nanosecond(~)
    long ot;
    protected int alarm_update_interval = Integer.MAX_VALUE; //default
    protected int batch_delay;
    protected final int MICROSECONDS_PER_MINUTE = 60000000;
    protected long last_update_times[] = new long[3];

    protected SensorManager sm;
    private Sensor mAccelerometer, mStepDetector, mMagnetometer;
    //Accel, step, magnet
    protected boolean is_Sensor_Running[] = new boolean[3];

    protected boolean isMagnetUpdated = false;
    protected boolean isAccelUpdated = false;
    SensorEvent pairEvent; //Used to store the last sensor event in the case that the 2 do not update at the same time
    public final double TWO_PI = Math.PI * 2;

    /* TODO: Rather than trying to incorporate the sensors later when location calculated,
     *      trigger booleans every time each sensor updates
     *      if both have been updated, log the event to the database tables
     *      Could combine the tables in that instance
     */

    protected final IBinder mBinder = new Binder();
    protected PendingIntent sensorUpdatePIntent;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver updateReceiver;

    /*1) Initializes database to store sensor data in
    * 2) Creates the intent filter to flush data after being stored
    * 3) Initializes the sensors to be collected
    * 4) Sets up listener for sensor events
    * 5) Starts hardware batching or informs Logger that batch not supported */
    @Override
    public void onCreate() {
        Log.i("AccelerometerService", "Creating service"); //Verbose log
        super.onCreate();

        //Database
        initDb();

        //Filters/Receiver
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_FLUSH_SENSOR);
        registerReceiver(updateReceiver, mIntentFilter);

        //Check validity of sensors. Initiate if possible
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        is_Sensor_Running[0] = initSensor(Sensor.TYPE_ACCELEROMETER, batch_delay);
        is_Sensor_Running[1] = initSensor(Sensor.TYPE_MAGNETIC_FIELD, batch_delay);
        is_Sensor_Running[2] = initSensor(Sensor.TYPE_STEP_DETECTOR, batch_delay);

        //If all sensors are valid
        if (is_Sensor_Running[0] && is_Sensor_Running[1] && is_Sensor_Running[2]) {

            //Returns a PI that will start a service
            //Will start service in current context, request code = 1,
            // Intent = Which service to start; No flags
            sensorUpdatePIntent = PendingIntent.getService(this, 1, new Intent(ACTION_FLUSH_SENSOR), 0);


            final int updateInterval[] = {maxReportInterval(mAccelerometer), maxReportInterval(mMagnetometer), maxReportInterval(mStepDetector)};
            //Location of listener, which sensor, delay between event updates,
            // delay between updating to disk from FIFO
            for (int i = 0; i < last_update_times.length; i++) {
                last_update_times[i] = 0;
                Log.v(this.getClass().getSimpleName(), "last update time" + last_update_times[i]);
            }

            sm.registerListener(this, mAccelerometer, 1000000 /* 1 second/ */, updateInterval[0] /*updateInterval*/);
            sm.registerListener(this, mMagnetometer, 1000000, updateInterval[1]);
            sm.registerListener(this, mStepDetector, 1000000, updateInterval[2]);

            for (int index = 0; index < updateInterval.length; index++) {
                int i = updateInterval[index];
                if (i > 0) {
                    //Sensor is capable of batching. Batching will be used
                    Log.i(this.getClass().getSimpleName(), "Application will retrieve batched data every "
                            + i + " ms");
                    if (i < alarm_update_interval) {
                        setUpdateClock(i);
                        alarm_update_interval = i;
                    }
                    Log.i(this.getClass().getSimpleName(), "Alarm will activate every " + i + " ms.");
                } else {
                    Log.e(this.getClass().getSimpleName(),
                            "Looks like this Android device doesn't support batching. " + "\tIndex: " + index);
                }
            }
        } /* END IF check for sensor validity. IF FALSE, No service functions will run." */
    }


    /*Was initially designed when only one sensor type was used
    * Now a rather shitty function because it just parses until it sets as a type
    * May as well just activate sensors another route
    *
    * On the other hand, this makes sure only those desired are initialized*/
    protected boolean initSensor(int type, int delay){
        this.batch_delay = delay; //TODO: Redundant code // FIXME: 2/26/2016

        if ((sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) && (type == Sensor.TYPE_ACCELEROMETER )) {
            mAccelerometer = sm.getDefaultSensor(type);
            return true;
        }
        if ((type == Sensor.TYPE_STEP_DETECTOR) && (sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null)) {
            mStepDetector = sm.getDefaultSensor(type);
            return true;
        }
        if ((type == Sensor.TYPE_MAGNETIC_FIELD) && (sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)) {
            mMagnetometer = sm.getDefaultSensor(type);
            return true;
        }

        return false;
    }

    protected void initDb() {
        //Initialize data collection storage
        DBHelper = new SensorDatabaseHelper(this);
        dataDb = DBHelper.getWritableDatabase(); //Gain +w access
    }

    private int maxReportInterval(Sensor sensor) {
        final int MAX_FIFO = sensor.getFifoMaxEventCount();
        Log.i("MAX_FIFO", sensor.getName() +  " Max fifo size: " + MAX_FIFO);
        //Really just a guess here at the proper interval. Should adjust based on trials
        //Guessed 3 events per update. Might count as one "block" in FIFO
        //If not, hardware is updating 3x more often than it needs to!
        if ((MAX_FIFO > 0) &&
                ((sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                || (sensor.getType() ==Sensor.TYPE_ACCELEROMETER))) {
            return (MAX_FIFO / 3) * 1000; //*1000 because value is ms
        }
        else if (MAX_FIFO > 0){
            return MAX_FIFO * 1000;
        }
        //No FIFO. No batching. Vast excess power usage
        return 0;
    }

    //Alarm to prevent FIFO data dropping
    //Wakelock alarm that persists through the phone sleeping
    //Does not persist through power cycle
    private void setUpdateClock(int interval) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        //PIntent merely contains a string to indicate that the FIFO should be flushed.
        //Prevents completely filling FIFO or having data counted twice in separate retrievals
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval,
                interval, sensorUpdatePIntent);
    }

    //Binds the service to the device.
    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(this.getClass().getSimpleName(), "Binding service");
        return mBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Open database for each sensor to insert their respective updates
        dataDb = DBHelper.getWritableDatabase();
        //Log.i(this.getClass().getSimpleName(), "" + event.sensor.getName() + "\t " + event.timestamp + "\tLast Update Time: " + last_update_times[0]);
        //TODO: Undo this comment to get accel logs back again
        if ((event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) &&
                ((event.timestamp - last_update_times[0]) > 1000)) {

            if (isMagnetUpdated) {
                /*TODO: NEW WAY OF UPDATING THE TABLES*/
                isAccelUpdated = false;
                isMagnetUpdated = false;

                putSensorUpdates(event, pairEvent);
            }
            else {
                isAccelUpdated = true;
                pairEvent = event; //Set the stored event to be the event from this sensor. When the magnetometer updates, this event will be used as reference for that point
            }

        }
        else if ((event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) &&
                ((event.timestamp - last_update_times[1]) > 1000)){

            if (isAccelUpdated) {
                isAccelUpdated = false;
                isMagnetUpdated = false;

                putSensorUpdates(pairEvent, event);
            }
            else {
                isMagnetUpdated = true;
                pairEvent = event;
            }
        }
        else if ((event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) &&
                ((event.timestamp - last_update_times[2]) > 1000)) {

            stepCount++;
            Log.i(getPackageName(), stepCount + " steps");

            ContentValues update = new ContentValues();
            update.put(DatabaseContract.PedometerEntry.COLUMN_NAME_TIMESTAMP, event.timestamp - ot);
            dataDb.insert(DatabaseContract.PedometerEntry.TABLE_NAME, null, update);

            //Reset update tracker
            last_update_times[2] = event.timestamp;
        }

        dataDb.close();
    }

    protected void putSensorUpdates(SensorEvent a_event, SensorEvent m_event) {

        float[] direction_radians = Calculations.getCompassDirection(a_event, m_event);
        int azimuth = (int) (Math.toDegrees(direction_radians[0]) + 360) % 360;

        //Cleanup accelerometer data for final input
        //Calculates the net acceleration, subtracts gravity to leave only user influence acceleration
        double resultant_acceleration = Math.sqrt(
                Math.pow(a_event.values[0], 2) +
                Math.pow(a_event.values[1], 2) +
                Math.pow(a_event.values[2], 2)) - SensorManager.GRAVITY_EARTH;

        /*
            Cleanup the magnetometer data for the final input
            Reference "Applications of Magnetic Sensors for Low Cost Compass Systems"
            By Michael J. Caruso; Honeywell, SSEC
         */
        double x_adjusted = m_event.values[0] * Math.cos(direction_radians[1])
                + m_event.values[1] * Math.sin(direction_radians[2]) * Math.sin(direction_radians[1])
                - m_event.values[2] * Math.cos(direction_radians[2] * Math.sin(direction_radians[1]));
        double y_adjusted = m_event.values[1] * Math.cos(direction_radians[2])
                + m_event.values[2] * Math.sin(direction_radians[2]);

        //THESE ARE THE VALUES YOU NEED TO USE
        String log = String.format("Current azimuth, dY, dX: %1$d, %2$.5f, %3$.5f", azimuth, y_adjusted, x_adjusted);
        Log.v("ServiceExtender", log);

        //Map values to update
        ContentValues update = new ContentValues();
        //Column for each value. Update object is the row container
        //NOTE: timestamp is in units of nanoseconds, since BOOT. NOT CURRENT TIME
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_TIMESTAMP, a_event.timestamp);
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_X, a_event.values[0]);
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Y, a_event.values[1]);
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Z, a_event.values[2]);

        dataDb.insert(DatabaseContract.AccelerometerEntry.TABLE_NAME, null, update);

        //Reset update tracker
        last_update_times[0] = a_event.timestamp;
        //FIXME: Eliminate the use of the last_update_time tracker. Only update on the interval when the two sensors have recorded data

        //Magnetometer
        update.clear();
        update.put(DatabaseContract.MagnetometerEntry.COLUMN_NAME_TIMESTAMP, m_event.timestamp - ot);
        update.put(DatabaseContract.MagnetometerEntry.COLUMN_NAME_X, m_event.values[0]);
        update.put(DatabaseContract.MagnetometerEntry.COLUMN_NAME_Y, m_event.values[1]);
        update.put(DatabaseContract.MagnetometerEntry.COLUMN_NAME_Z, m_event.values[2]);
        update.put(DatabaseContract.MagnetometerEntry.COMPASS, azimuth);

        dataDb.insert(DatabaseContract.MagnetometerEntry.TABLE_NAME, null, update);

        //Reset update tracker
        last_update_times[1] = m_event.timestamp;

        pairEvent = null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        Log.i(this.getClass().getName(), "Destroying the service.");
        super.onDestroy();

        AlarmManager alarmManagerCanceller = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManagerCanceller.cancel(sensorUpdatePIntent); //cancel the flushing operation for FIFO
        sm.unregisterListener(this); //stop listening to sensor in foreground or background
    }
}