package pl.janusz.hain.socialmediawatcher;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;

public class DatabaseTwitterManager {

    private SQLiteDatabase sqLiteDatabase;

    public DatabaseTwitterManager(Activity activity) {
        new DatabaseConnector(activity);
        sqLiteDatabase = DatabaseConnector.getSqLiteDatabase();
    }

    public boolean addTwitter(TwitterAccount twitterAccount) {
        long id = sqLiteDatabase.insert(DatabaseTwitterContract.TwitterAccount.TABLE_NAME, null, fromObjectToContentValues(twitterAccount));
        return rowAddedSuccessfuly(id);
    }


    private boolean rowAddedSuccessfuly(long id) {
        if (id != -1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean updateTwitter(TwitterAccount twitterAccount) {
        int rowsAffected = 0;
        String selection = DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_SCREEN_NAME + " LIKE ?";
        String[] selectionArgs = {twitterAccount.getScreenName()};
        rowsAffected = sqLiteDatabase.update(DatabaseTwitterContract.TwitterAccount.TABLE_NAME, fromObjectToContentValues(twitterAccount), selection, selectionArgs);
        return rowsAffected(rowsAffected);
    }


    private ContentValues fromObjectToContentValues(TwitterAccount twitterAccount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_SCREEN_NAME, twitterAccount.getScreenName());
        contentValues.put(DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_NAME, twitterAccount.getName());
        contentValues.put(DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_VALUE, twitterAccount.getValue());
        contentValues.put(DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_DATE_CREATED, getCurrentDateTime());
        return contentValues;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        return date;
    }


    public boolean deleteTwitter(TwitterAccount twitterAccount) {
        String selection = DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(twitterAccount.getId())};
        int rowsAffected = sqLiteDatabase.delete(DatabaseTwitterContract.TwitterAccount.TABLE_NAME, selection, selectionArgs);
        return rowsAffected(rowsAffected);
    }

    private boolean rowsAffected(int rowsAffected) {
        if (rowsAffected > 0) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<TwitterAccount> getTwittersOrderedByValue(int start, int offset) {
        String sortOrder = DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_VALUE + " DESC";
        Cursor cursor = queryGetTwitters(sortOrder, start, offset);
        ArrayList<TwitterAccount> twitterAccounts = readTwittersFromCursor(cursor);
        return twitterAccounts;
    }

    public ArrayList<TwitterAccount> getTwittersOrderedByDataCreated(int start, int offset) {
        String sortOrder = DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_DATE_CREATED + " DESC";
        Cursor cursor = queryGetTwitters(sortOrder, start, offset);
        ArrayList<TwitterAccount> twitterAccounts = readTwittersFromCursor(cursor);
        return twitterAccounts;
    }

    public Observable<ArrayList<TwitterAccount>> getObservableLoadAllTweets() {
        Observable<ArrayList<TwitterAccount>> observableGetAllTwitterAccounts = Observable.create(new Observable.OnSubscribe<ArrayList<TwitterAccount>>() {
            @Override
            public void call(final Subscriber<? super ArrayList<TwitterAccount>> subscriber) {
                subscriber.onNext(getAllTwitterAccounts());
                subscriber.onCompleted();
            }
        });

        return observableGetAllTwitterAccounts;
    }

    private ArrayList<TwitterAccount> getAllTwitterAccounts() {
        return readTwittersFromCursor(queryGetTwitters());
    }


    public boolean twitterAccountExists(TwitterAccount twitterAccount) {
        String selection = DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_SCREEN_NAME + " LIKE ?";
        String[] selectionArgs = {twitterAccount.getScreenName()};
        Cursor cursor = sqLiteDatabase.query(DatabaseTwitterContract.TwitterAccount.TABLE_NAME, projectionOfTwitterAccounts(), selection, selectionArgs, null, null, null);
        return cursor.getCount() > 0;
    }


    private Cursor queryGetTwitters() {
        Cursor cursor = sqLiteDatabase.query(
                DatabaseTwitterContract.TwitterAccount.TABLE_NAME,  // The table to query
                projectionOfTwitterAccounts(),                      // The columns to return
                null,                                               // The columns for the WHERE clause
                null,                                               // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        return cursor;
    }

    private Cursor queryGetTwitters(String sortOrder, int start, int offset) {
        Cursor cursor = sqLiteDatabase.query(
                DatabaseTwitterContract.TwitterAccount.TABLE_NAME,  // The table to query
                projectionOfTwitterAccounts(),                      // The columns to return
                null,                                               // The columns for the WHERE clause
                null,                                               // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                sortOrder,                                          // The sort order
                offset(start, offset)                               // limit
        );
        return cursor;
    }

    private String[] projectionOfTwitterAccounts() {
        String[] projection = {
                DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_ID,
                DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_SCREEN_NAME,
                DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_NAME,
                DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_DATE_CREATED,
                DatabaseTwitterContract.TwitterAccount.COLUMN_NAME_VALUE
        };
        return projection;
    }

    private String offset(int start, int offset) {
        return start + ", " + offset;
    }

    private ArrayList<TwitterAccount> readTwittersFromCursor(Cursor cursor) {
        ArrayList<TwitterAccount> twitterAccounts = new ArrayList<TwitterAccount>();
        if (cursorCount(cursor) > 0) {
            if (cursor.moveToFirst()) {
                do {
                    TwitterAccount twitterAccount = new TwitterAccount(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getInt(4));
                    twitterAccounts.add(twitterAccount);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        return twitterAccounts;
    }

    private int cursorCount(Cursor cursor) {
        return cursor.getCount();
    }
}
