package pl.janusz.hain.socialmediawatcher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SocialMediaWatcher.db";

    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sqLiteDatabase = db;
        createTwitterAccountTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        sqLiteDatabase = db;
        deleteThenCreateTwitterAccountTable();
    }

    private void deleteThenCreateTwitterAccountTable() {
        deleteTwitterAccountTable();
        createTwitterAccountTable();
    }

    private void createTwitterAccountTable() {
        sqLiteDatabase.execSQL(DatabaseTwitterContract.TwitterAccount.SQL_CREATE_ENTRIES);
    }

    private void deleteTwitterAccountTable() {
        sqLiteDatabase.execSQL(DatabaseTwitterContract.TwitterAccount.SQL_DELETE_ENTRIES);
    }


}
