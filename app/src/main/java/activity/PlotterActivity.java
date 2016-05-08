package activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;

import msu.evan.gyrodata.Coordinates;
import database.DatabaseContract;
import msu.evan.gyrodata.R;
import database.SensorDatabaseHelper;

public class PlotterActivity extends Activity {

    /*TODO: New way to calculate using the acceleration
     * h = center of mass height
     * To calculate Q, have user walk 10 m, calculate the difference between real distance and estimated (calculated) distance
     * Q = Real/Calculated distance
     * Step = Q * 2 * sqrt(2 * leg length * h - pow(h,2))
     */
    public static final double STEP_LENGTH = 0.75; //0.75 m default step
    ArrayList<Coordinates> point_list;
    private XYPlot location_plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotter);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        location_plot = (XYPlot) findViewById(R.id.plot);

        startArray(0,0);
        nums();
        renderGraph();
    }

    protected ArrayList<Coordinates> nums () {
        //Database open/collection
        Cursor[] cursor = new Cursor[2];

        //Use the database query to generate points for each step.
        //Calculate time closest to the pedometer step, then
        SensorDatabaseHelper dbHelper = new SensorDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        //Cursor Projections. Columns to include in the result set
        String pedColumnsToBind[] = new String[]{"_id",
                DatabaseContract.PedometerEntry.COLUMN_NAME_TIMESTAMP};
        String magnetColumnsToBind[] = new String[]{"_id",
                DatabaseContract.MagnetometerEntry.COLUMN_NAME_TIMESTAMP,
                DatabaseContract.MagnetometerEntry.COLUMN_NAME_X,
                DatabaseContract.MagnetometerEntry.COLUMN_NAME_Y,
                DatabaseContract.MagnetometerEntry.COLUMN_NAME_Z,
                DatabaseContract.MagnetometerEntry.COMPASS};

        //Cursor args
        String whereArgs[] = null; //Array of selection arguments to replace wildcards in 'where'
        String groupBy = null; //Defines how result rows will be groups
        String having = null; //Which row groups to include if groupBy specified
        String order = null; //Defined order of the returned rows

        cursor[0] = db.query(DatabaseContract.PedometerEntry.TABLE_NAME, pedColumnsToBind, null, whereArgs, groupBy, having, order);
        cursor[1] = db.query(DatabaseContract.MagnetometerEntry.TABLE_NAME, magnetColumnsToBind, null, whereArgs, groupBy, having, order);



        //For every pedometer event, parse the magnetometer for its values
        if ((cursor[0] != null) && (cursor[1] != null)) { //If there are pedometer events & compass events
            //Indices for retrieving points from the cursor
            int pedomIndex = cursor[0].getColumnIndex(DatabaseContract.PedometerEntry.COLUMN_NAME_TIMESTAMP);
            int xIndex = cursor[1].getColumnIndex(DatabaseContract.MagnetometerEntry.COLUMN_NAME_X);
            int yIndex = cursor[1].getColumnIndex(DatabaseContract.MagnetometerEntry.COLUMN_NAME_Y);
            int zIndex = cursor[1].getColumnIndex(DatabaseContract.MagnetometerEntry.COLUMN_NAME_Z);
            int azimuthIndex = cursor[1].getColumnIndex(DatabaseContract.MagnetometerEntry.COMPASS);

            //Because the pedometer and the magnetometer update at the same refresh rate
            // Can link the two events together despite not having identical times. Within ~50 ns?
            cursor[0].moveToFirst(); //start at first index. AKA Pedometer timestamp
            for (int i = 0; i < cursor[0].getCount(); i++) { //For every step
                if (!cursor[1].isLast()) {
                    cursor[1].moveToNext();
                }

                float x_multiplier = cursor[1].getFloat(xIndex); //X magnet value
                float y_multiplier = cursor[1].getFloat(yIndex);
                float z_multiplier = cursor[1].getFloat(zIndex);

                float time = cursor[0].getFloat(pedomIndex);
                float azimuth = cursor[1].getFloat(azimuthIndex);

                double dx = STEP_LENGTH * Math.cos(azimuth);
                double dy = STEP_LENGTH * Math.sin(azimuth);
               // String log = String.format("Xm, Ym, Zm, Delta X, Delta Y : %1$.2f, %2$.2f, %3$.2f, %4$.2f, %5$.2f", x_multiplier, y_multiplier, z_multiplier, dx, dy);
               // Log.i("Plotter", log);

                if (point_list.size() != 0) {
                    Coordinates lastPoint = point_list.get(point_list.size() -  1);
                    double x = lastPoint.getX() + dx;
                    x = Math.floor(x * 100) / 100;
                    double y = lastPoint.getY() + dy;
                    y = Math.floor(y * 100) / 100;

                    Log.i(this.getClass().getName(), String.format("Last X, Y, Step Length, Time: %1$.2f, %2$.2f, %3$.2f, %4$.2f", x, y, STEP_LENGTH, time));
                    Coordinates newPoint = new Coordinates(x, y);

                    point_list.add(newPoint);
                }
                else {
                    Coordinates newPoint = new Coordinates(dx, dy);
                    Log.i(this.getClass().getName(), "Last X, : There was no points.");

                    point_list.add(newPoint);
                }
            }
        }

        return point_list;
    }

    private void startArray(double x, double y) {
        point_list = new ArrayList<>();
        point_list.add(new Coordinates(x,y));
    }

    private class PlotTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }

    /*TODO: GENERATING ACCCURATE DATA TRACKING USING THE SENSOR DATA
    * Note: Theta = angle between xy
    * Phi = angle between xz
    *
    * (Delta) X = [(Compass X)*cos(theta)] + [(compass y) * sin(theta)sin(phi)] + [(compass z) * sin(phi)cos(theta)]
    * (Delta) Y = [(Compass Z) * sin(theta)] + [(Compass Y) * cos(theta)]
    *
    * Generated Step location:
    * Triangle: Start at (x,y) Length x1, height y1, Hypotenuse: STEP_LENGTH
    * */

    private XYSeries generatePoints() {
        SimpleXYSeries mySeries = new SimpleXYSeries("Location");
        for(Coordinates point:point_list) {
            mySeries.addLast(point.getX(), point.getX());
        }

        return mySeries;
    }

    private void renderGraph() {
        LineAndPointFormatter locationFormatter = new LineAndPointFormatter();
        PointLabelFormatter pf = new PointLabelFormatter();
        pf.getTextPaint().setTextSize(20);
        locationFormatter.setPointLabelFormatter(pf);
        locationFormatter.configure(getApplicationContext(), R.xml.line_point_formatter_with_labels);

        location_plot.addSeries(generatePoints(), locationFormatter);
    }

}