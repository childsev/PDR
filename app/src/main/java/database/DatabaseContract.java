package database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    public DatabaseContract() {}

    //identify the names of each column within the database
    public static abstract class AccelerometerEntry implements BaseColumns {
        public static final String KEY_ID = " _id";
        public static final String TABLE_NAME = "AccelerometerData";
        public static final String COLUMN_NAME_TIMESTAMP = "Timestamp";
        public static final String COLUMN_NAME_ACCELEROMETER_X = "X";
        public static final String COLUMN_NAME_ACCELEROMETER_Y = "Y";
        public static final String COLUMN_NAME_ACCELEROMETER_Z = "Z";
    }

    public static abstract class PedometerEntry implements BaseColumns {
        public static final String KEY_ID = "_id";
        public static final String TABLE_NAME = "PedometerData";
        public static final String COLUMN_NAME_TIMESTAMP = "Timestamp";
    }

    //identify the names of each column within the database
    public static abstract class MagnetometerEntry implements BaseColumns {
        public static final String KEY_ID = " _id";
        public static final String TABLE_NAME = "MagnetometerData";
        public static final String COLUMN_NAME_TIMESTAMP = "Timestamp";
        public static final String COLUMN_NAME_X = "X";
        public static final String COLUMN_NAME_Y = "Y";
        public static final String COLUMN_NAME_Z = "Z";
        public static final String COMPASS = "Compass";
    }

}
