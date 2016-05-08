package activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import msu.evan.gyrodata.AccelerometerService;
import msu.evan.gyrodata.Characteristics;
import database.DatabaseViewer;
import database.FileImportExporter;
import msu.evan.gyrodata.ListClickHandler;
import msu.evan.gyrodata.R;
import database.SensorDatabaseHelper;
import adapter.SensorListAdapter;

public class MainActivity extends Activity implements SensorEventListener{

    /* Current goals
    * 1) Decide which sensors to use and start to collect data in the background
    * 2) What are applications of sensors for human life
    * 3) How to maximize battery efficiency
    * */

    private Toolbar mToolbar;
    private ListView sensorList;
    private SensorListAdapter mAdapter;
    Spinner mSpinner;
    private boolean is_service_running;
    List<Sensor> available_sensors_;
    private static int api_version_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Note: Toolbar must be included in the XML doc
        //Setup the action bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        getActionBar().setDisplayShowHomeEnabled(true);

        genList();
        stepCounterDetectorSupport();
        loadButtonSpinners();

        //Start the accelerometer logging service
        //TODO Incorporate the service and logging
        if (available_sensors_.size() != 0) {
            startService(new Intent(this, AccelerometerService.class));
            is_service_running = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu for this respective activity using the toolbar and respective layout
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void genList(){
        //Generate list of available_sensors_ sensors within the phone
        available_sensors_ = ((SensorManager) getSystemService(SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
        List<Characteristics> toAdapter = new ArrayList<Characteristics>();

        for (int x = 1; x < available_sensors_.size(); x++)
        {
            if (available_sensors_.get(x).getName() != null)
                toAdapter.add(new Characteristics(available_sensors_.get(x).getVersion(), available_sensors_.get(x).getStringType(), available_sensors_.get(x).getType(), available_sensors_.get(x).getPower(), available_sensors_.get(x).getFifoMaxEventCount()));
        }

        /*For use with regular ArrayAdapter
        String[] availableSensorNames = new String[available_sensors_.size()];
        for (int i = 0; i < available_sensors_.size(); i++)
        {
            availableSensorNames[i] = available_sensors_.get(i).getName();
        }*/

        //Adapter for the list of sensors
        mAdapter = new SensorListAdapter(this, toAdapter);
        sensorList = (ListView)findViewById(R.id.sList);
        sensorList.setAdapter(mAdapter);
        sensorList.setFastScrollEnabled(true);

        sensorList.setOnItemClickListener(new ListClickHandler());
    }

    public boolean stepCounterDetectorSupport() {
        api_version_ = Build.VERSION.SDK_INT; // 4.4+ support
        PackageManager pm = getApplication().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
            && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR))
            return true;
        else
            return false;
    }

    public void loadButtonSpinners() {
        mSpinner = (Spinner) findViewById(R.id.spinner);
        //ArrayAdapter to incorporate data in the mSpinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sensorSpinnerOptions, android.R.layout.simple_spinner_item);
        //Layout when choice options open
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Incorporate the mSpinner
        mSpinner.setAdapter(adapter);
    }


    public void startSensorActivity(View v) {
        Intent intent;
        Toast.makeText(MainActivity.this, mSpinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
        String current = mSpinner.getSelectedItem().toString();
        if (current.equals("Gyroscope")) {
            intent = new Intent(this, GyroscopeActivity.class);
            startActivity(intent);
        }
        else if (current.equals("Accelerometer")) {
            intent = new Intent(this, AccelerometerActivity.class);
            startActivity(intent);
        }
        else if (current.equals("Light")) {
            intent = new Intent(this, LightSensorActivity.class);
            startActivity(intent);
        }
        else if (current.equals("Plotter")) {
            intent = new Intent(this, PlotterActivity.class);
            startActivity(intent);
        }
        else
            Toast.makeText(MainActivity.this, "No sensor selected!" , Toast.LENGTH_LONG).show();
    }

    public void viewDataList(View v) {
        Intent intent = new Intent(this, DatabaseViewer.class);
        startActivity(intent);
    }

    public void deleteData(View v) {
        boolean deleted = getApplicationContext().deleteDatabase(SensorDatabaseHelper.DATABASE_NAME);
        if (deleted) {
            Toast.makeText(this, "Sensor Database Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchService(View v) {
        if (is_service_running) {
            stopService(new Intent(this, AccelerometerService.class));
            Toast.makeText(this, "Stopping Service", Toast.LENGTH_LONG).show();
            is_service_running = false;
        }
        else{
            startService(new Intent(this, AccelerometerService.class));
            Toast.makeText(this, "Starting Service", Toast.LENGTH_LONG).show();
            is_service_running = true;
        }
    }

    public void exportDatabase (View v) {
       boolean exported = FileImportExporter.export(getApplicationContext());
        if (exported)
            Toast.makeText(this, "Exported", Toast.LENGTH_LONG).show();
    }


    //List of must includes for implementing SensorEventListener
    @Override
    protected void onResume() { super.onResume();}
    @Override
    protected void onPause() { super.onPause(); }
    @Override
    protected void onStop() {super.onStop();}

    //Required to generate the list of sensors.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {}
}
