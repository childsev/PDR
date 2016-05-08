package database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import msu.evan.gyrodata.R;

public class DatabaseViewer extends Activity {

    SimpleCursorAdapter adapter;
    Cursor cursor;
    private Toolbar mToolbar;
    TextView[] list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database_viewer_layout);

        //Setup the action bar
        //Remember this only works because it is including in the XML
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(mToolbar);
        getActionBar().setDisplayShowHomeEnabled(true);



        SensorDatabaseHelper dbHelper = new SensorDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String columnsToBind[] = new String[] {"_id", DatabaseContract.AccelerometerEntry.COLUMN_NAME_TIMESTAMP, DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_X, DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Y, DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Z};
        cursor = db.query(DatabaseContract.AccelerometerEntry.TABLE_NAME, columnsToBind, null, null, null, null, null);

        //Design columns and binding
        int dataBindViews [] = new int[] {R.id.id, R.id.dbTimestampEntry, R.id.dbXEntry, R.id.dbYEntry, R.id.dbZEntry};
        adapter = new SimpleCursorAdapter(this, R.layout.database_list_layout, cursor, columnsToBind, dataBindViews, 0);

        ListView list =  (ListView)findViewById(R.id.tList);
        //Add in header for distinction
        list.setAdapter(adapter);
        list.setFastScrollEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu for this respective activity using the toolbar and respective layout
        getMenuInflater().inflate(R.menu.menu_db_viewer, menu);
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
        if (id == R.id.action_export_db) {
            exportDatabase();
        }

        return super.onOptionsItemSelected(item);
    }

    public void exportDatabase () {
        boolean exported = FileImportExporter.export(getApplicationContext());
        if (exported)
            Toast.makeText(this, "Exported", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cursor.close();
    }
}
