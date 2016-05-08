package activity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import database.DatabaseContract;
import msu.evan.gyrodata.R;
import database.SensorDatabaseHelper;
import msu.evan.gyrodata.SensorObject;

public class AccelerometerActivity extends SensorObject {

    TextView DataDisplay,T;
    GraphView graph;
    LineGraphSeries<DataPoint>[] dataLine = new LineGraphSeries[3];
    SensorDatabaseHelper DBHelper;
    public SQLiteDatabase dataDb;
    private long ot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        initSeries();
        initGraph();
        initTextFields();
        initDb();
        //batch_delay = SensorManager.SENSOR_DELAY_NORMAL ;
        batch_delay = MICROSECONDS_PER_MINUTE;
        initSensor(Sensor.TYPE_ACCELEROMETER, batch_delay);
        ot = System.currentTimeMillis();
    }

    protected void initSeries() {
        //Acceleration data to DataPoint series to plot on graph
        for (int x = 0; x < dataLine.length; x++)
        {
            dataLine[x] = new LineGraphSeries<DataPoint>();
        }

        dataLine[0].setTitle("X Acceleration");
        dataLine[0].setColor(Color.RED);

        dataLine[1].setTitle("Y Acceleration");
        dataLine[1].setColor(Color.BLUE);

        dataLine[2].setTitle("Z Acceleration");
        dataLine[2].setColor(Color.MAGENTA);
    }

    protected void initGraph()    {
        graph = (GraphView) findViewById(R.id.sensorGraph);

        //Restrict range of the gravitational acceleration to be displayed
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(false);
        graph.getViewport().setMaxY(20.0); //Default max Y
        graph.getViewport().setMinY(-20.0);

        //Manage reference names, tags
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);

        //Add acceleration data to the graph
        for (int x = 0; x < dataLine.length; x++)
        {
            graph.addSeries(dataLine[x]);
        }

        //Set Title for the graph
        graph.setTitle("Acceleration of Device (m/s^2)");
        graph.setTitleColor(Color.MAGENTA);
    }

    protected void initTextFields() {
        DataDisplay = ((TextView) findViewById(R.id.AccelData)); //X Gyro
        DataDisplay.setText(getString(R.string.accelerometer_sensor_output_string, "X", null, "Y", null, "Z", null));
        T = ((TextView) findViewById(R.id.timestamp)); //Timestamp
        T.setText(null);
    }

    protected void initDb() {
        //Initialize data collection storage
        DBHelper = new SensorDatabaseHelper(this);
        //dataDb = DBHelper.getReadableDatabase();
        dataDb = DBHelper.getWritableDatabase(); //Gain +w access
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Display the acceleration data as top of screen
        DataDisplay.setText(getString(R.string.accelerometer_sensor_output_string,
                "X", event.values[0],
                "Y", event.values[1],
                "Z", event.values[2]));

        //T.setText("Current Time: " + Float.toString(event.timestamp) + "ns");
        T.setText(getString(R.string.timestamp_string, System.currentTimeMillis()-ot) + "\n" + sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getName());

        //Map values to update Database
        ContentValues update = new ContentValues();
        //Column for each value. Update object is the row container
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_TIMESTAMP, System.currentTimeMillis()-ot);
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_X, event.values[0]);
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Y, event.values[1]);
        update.put(DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Z, event.values[2]);

        //Insert information to database
        long rowToInsert;
        //Table Name, Skip row if ContentValues is null, ContentValues Object
//        rowToInsert = dataDb.insert(DatabaseContract.AccelerometerEntry.TABLE_NAME, null, update);
        dataDb.insert(DatabaseContract.AccelerometerEntry.TABLE_NAME, null, update);


        //Add data from sensor update to graph
        for (int x = 0; x < dataLine.length; x++)
        {
            dataLine[x].appendData(new DataPoint(System.currentTimeMillis()-ot, event.values[x]), true, 50);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        sm.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}