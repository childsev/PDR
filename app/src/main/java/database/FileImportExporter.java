package database;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Evan on 2/12/2016.
 */
public final class FileImportExporter {

    public static boolean export(Context context) {
        File sdDirectory = Environment.getExternalStorageDirectory();
        File internalDirectory = Environment.getDataDirectory();

        FileChannel source = null;
        FileChannel destination = null;

        String databaseToExport = "/data/" + "msu.evan.gyrodata" + "/databases/" + SensorDatabaseHelper.DATABASE_NAME;
        String exportDBPath = SensorDatabaseHelper.DATABASE_NAME;

        File activeDB = new File(internalDirectory, databaseToExport);
        File exportedDB = new File(sdDirectory, exportDBPath);

        try {
            source = new FileInputStream(activeDB).getChannel();
            destination = new FileOutputStream(exportedDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
