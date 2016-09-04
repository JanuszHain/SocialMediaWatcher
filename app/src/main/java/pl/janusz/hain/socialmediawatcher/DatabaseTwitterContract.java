package pl.janusz.hain.socialmediawatcher;

/**
 * Provides commands that are used in constructing a database.
 */

public class DatabaseTwitterContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public DatabaseTwitterContract() {
    }

    public static abstract class TwitterAccount {
        public static final String TABLE_NAME = "TwitterAccount";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SCREEN_NAME = "screen_name";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_DATE_CREATED = "date_created";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_SCREEN_NAME + TEXT_TYPE + "NOT NULL UNIQUE" + COMMA_SEP +
                        COLUMN_NAME_NAME + TEXT_TYPE + "NOT NULL" + COMMA_SEP +
                        COLUMN_NAME_VALUE + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_DATE_CREATED + TEXT_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
}
