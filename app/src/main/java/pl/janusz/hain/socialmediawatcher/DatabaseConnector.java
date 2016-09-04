package pl.janusz.hain.socialmediawatcher;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

/**
 * <br>
 * Provides easy database access across fragments without needing of creating new connection in each of them.<br>
 * <br>
 * Constructor takes {@link Activity} as param to get {@link android.content.Context Application Context} from it.<br>
 * Parameters are static, every new object initializes them again if connection to database is closed.<br>
 */

public class DatabaseConnector {
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase sqLiteDatabase;

    public DatabaseConnector(Activity activity) {
        if (activity != null)
            if (paramsAreNull() || !sqLiteDatabase.isOpen()) {
                databaseHelper = new DatabaseHelper(activity.getApplicationContext());
                sqLiteDatabase = databaseHelper.getWritableDatabase();
            }
    }

    private boolean paramsAreNull() {
        if (databaseHelper == null || sqLiteDatabase == null) {
            return true;
        } else {
            return false;
        }
    }

    public static SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }
}
