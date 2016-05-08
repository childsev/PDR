package database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class SensorDatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    //Database version
    public static final int DB_VERSION = 1;
    public static final String DATABASE_NAME = "SensorData.db";

    public SensorDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    /*
    * Strings used to create the SQLITE database tables
    * Strings are hardcoded rather than automatically generated as of yet, considering
    *   that only trying to collect data from the few sensors
    * TODO: Automatically generated Tables: Pass in sensor reference.
    * */
    private static final String CREATE_ACCELEROMETER_DATA_TABLE =
            "CREATE TABLE " + DatabaseContract.AccelerometerEntry.TABLE_NAME + " ( " +
                    DatabaseContract.AccelerometerEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseContract.AccelerometerEntry.COLUMN_NAME_TIMESTAMP + " LONG" + ", " +
                    DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_X + " FLOAT" + ", " +
                    DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Y + " FLOAT" + ", " +
                    DatabaseContract.AccelerometerEntry.COLUMN_NAME_ACCELEROMETER_Z  + " FLOAT" + " );";

    private static final String CREATE_MAGNETOMETER_DATA_TABLE =
            "CREATE TABLE " + DatabaseContract.MagnetometerEntry.TABLE_NAME + " ( " +
                    DatabaseContract.MagnetometerEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseContract.MagnetometerEntry.COLUMN_NAME_TIMESTAMP + " LONG" + ", " +
                    DatabaseContract.MagnetometerEntry.COLUMN_NAME_X  + " FLOAT " + ", " +
                    DatabaseContract.MagnetometerEntry.COLUMN_NAME_Y + " FLOAT " + ", " +
                    DatabaseContract.MagnetometerEntry.COLUMN_NAME_Z + " FLOAT " + ", " +
                    DatabaseContract.MagnetometerEntry.COMPASS + " INTEGER " + " );";

    private static final String CREATE_PEDOMETER_DATA_TABLE =
            "CREATE TABLE " + DatabaseContract.PedometerEntry.TABLE_NAME + " ( " +
                    DatabaseContract.PedometerEntry.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseContract.PedometerEntry.COLUMN_NAME_TIMESTAMP + " LONG " + " );";

    /*
    * Strings to delete the sensor's respective table from the Database
    * Currently only called when the Database is upgraded or destroyed
    * TODO: Automatically generate list of strings for deletion
    * */
    private static final String DELETE_ACCELEROMETER_DATA_TABLE =
            "DROP TABLE IF EXISTS " + DatabaseContract.AccelerometerEntry.TABLE_NAME;
    private static final String DELETE_MAGNETOMETER_DATA_TABLE =
            "DROP TABLE IF EXISTS " + DatabaseContract.MagnetometerEntry.TABLE_NAME;
    private static final String DELETE_PEDOMETER_TABLE =
            "DROP TABLE IF EXISTS " + DatabaseContract.PedometerEntry.TABLE_NAME;


    //If a previous database DNE, then called
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCELEROMETER_DATA_TABLE);
        db.execSQL(CREATE_MAGNETOMETER_DATA_TABLE);
        db.execSQL(CREATE_PEDOMETER_DATA_TABLE);
        Log.w("SensorDataBaseHelper", "A new SensorDatabase has been generated");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Logcat that the database is being upgraded
        Log.w("SensorDatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion + ". This will destroy all old data.");
        db.execSQL(DELETE_ACCELEROMETER_DATA_TABLE);
        db.execSQL(DELETE_MAGNETOMETER_DATA_TABLE);
        db.execSQL(DELETE_PEDOMETER_TABLE);


        //RECREATE THE TABLE
        onCreate(db);
        Log.w("SensorDatabaseHelper", "Your database has successfully been upgraded. " +
                "Your Data Has been destroyed. Sorry :(");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public Cursor getData() {
        Cursor data = this.getWritableDatabase().rawQuery("select * from" + DatabaseContract.AccelerometerEntry.TABLE_NAME, null);
        return data;
    }
}